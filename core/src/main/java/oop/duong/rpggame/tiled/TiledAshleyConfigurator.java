package oop.duong.rpggame.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.asset.AssetService;
import oop.duong.rpggame.asset.AtlasAsset;
import oop.duong.rpggame.component.*;
import oop.duong.rpggame.component.Animation2D.AnimationType;

/**
 * Chuyển dữ liệu từ Tiled (tile layer, object layer)
 * thành các Entity + Body Box2D.
 *
 * - onLoadTile   : được gọi cho từng tile trong tile layer
 * - onLoadObject : được gọi cho từng object trong layer "objects"
 *
 * Code này dựa sát theo project mẫu, nhưng lược bỏ các phần
 * Player / Attack / Life / Trigger chưa cần tới.
 */
public class TiledAshleyConfigurator {

    // scale mặc định cho collider của tile
    private static final Vector2 DEFAULT_PHYSIC_SCALING = new Vector2(1f, 1f);

    private final Engine engine;
    private final World physicWorld;
    private final AssetService assetService;

    // tạm dùng lại để tránh tạo nhiều object
    private final MapObjects tmpMapObjects = new MapObjects();

    public TiledAshleyConfigurator(Engine engine, World physicWorld, AssetService assetService) {
        this.engine = engine;
        this.physicWorld = physicWorld;
        this.assetService = assetService;
    }

    // ==================== TILE COLLISION (ENVIRONMENT) ====================

    /**
     * Được TiledService gọi cho mỗi cell trong tile layer.
     * Nếu tile có collision objects trong tileset → tạo body tĩnh "environment".
     *
     * @param tile tile tại ô (x,y)
     * @param x    tọa độ cell X (tính theo đơn vị tile)
     * @param y    tọa độ cell Y (tính theo đơn vị tile)
     */
    public void onLoadTile(TiledMapTile tile, float x, float y) {
        if (tile == null || tile.getObjects().getCount() == 0) {
            // tile không có collider trong tileset → bỏ qua
            return;
        }

        // vị trí world (từ cell x,y → pixel → world units)
        Vector2 worldPos = new Vector2(
            x * tile.getTextureRegion().getRegionWidth() * RPGGame.UNIT_SCALE,
            y * tile.getTextureRegion().getRegionHeight() * RPGGame.UNIT_SCALE
        );

        createBody(
            tile.getObjects(),         // collider được định nghĩa trong tileset
            worldPos,                  // vị trí gốc của cell
            DEFAULT_PHYSIC_SCALING,    // scale (1,1)
            BodyDef.BodyType.StaticBody,
            Vector2.Zero,              // relativeTo = (0,0)
            "environment"              // userData của body → dùng để cleanup khi đổi map
        );
    }

    // ==================== OBJECTS (ENTITY + BODY) ====================

    /**
     * Được gọi cho mỗi object trong layer "objects".
     * Tạo Entity + Transform + Graphic + Move + Controller + Physic.
     */
    public void onLoadObject(TiledMapTileMapObject tileMapObject) {
        Entity entity = this.engine.createEntity();

        TiledMapTile tile = tileMapObject.getTile();
        TextureRegion textureRegion = getTextureRegion(tile);

        float sortOffsetY = tile.getProperties().get("sortOffsetY", 0, Integer.class);
        sortOffsetY *= RPGGame.UNIT_SCALE;
        int z = tile.getProperties().get("z", 1, Integer.class);

        // Graphic
        entity.add(new Graphic(Color.WHITE.cpy(), textureRegion));

        // Transform (vị trí, kích thước, scale)
        addEntityTransform(
            tileMapObject.getX(),
            tileMapObject.getY(),
            z,
            textureRegion.getRegionWidth(),
            textureRegion.getRegionHeight(),
            tileMapObject.getScaleX(),
            tileMapObject.getScaleY(),
            sortOffsetY,
            entity
        );

        // Move + Controller + Animation
        addEntityController(tileMapObject, entity);
        addEntityCameraFollow(tileMapObject, entity);
        addEntityMove(tile, entity);
        addEntityAnimation(tile, entity);

        // Facing & FSM như trước
        entity.add(new Facing(Facing.FacingDirection.DOWN));
        entity.add(new Fsm(entity));

        // Physic body (nếu tile có collision objects trong tileset)
        addEntityPhysic(tile, entity);

        this.engine.addEntity(entity);
    }

    // ==================== COMPONENT HELPERS ====================

    private void addEntityAnimation(TiledMapTile tile, Entity entity) {
        String animationStr = tile.getProperties().get("animation", "", String.class);
        if (animationStr.isBlank()) {
            return;
        }

        AnimationType animationType = AnimationType.valueOf(animationStr);
        String atlasAssetStr = tile.getProperties().get("atlasAsset", AtlasAsset.OBJECTS.name(), String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        float speed = tile.getProperties().get("animationSpeed", 0f, Float.class);

        entity.add(new Animation2D(atlasAsset, atlasKey, animationType, PlayMode.LOOP, speed));
    }

    private void addEntityMove(TiledMapTile tile, Entity entity) {
        float speed = tile.getProperties().get("speed", 0f, Float.class);
        if (speed == 0f) {
            return;
        }
        entity.add(new Move(speed));
    }

    private void addEntityController(TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean controller = tileMapObject.getProperties().get("controller", false, Boolean.class);
        if (!controller) {
            return;
        }
        entity.add(new Controller());
    }
    private void addEntityCameraFollow(TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean cameraFollow = tileMapObject.getProperties().get("camFollow", false, Boolean.class);
        if (cameraFollow) {
            entity.add(new CameraFollow());
        }
    }

    private static void addEntityTransform(
        float x, float y, int z,
        float w, float h,
        float scaleX, float scaleY,
        float sortOffsetY,
        Entity entity
    ) {
        Vector2 position = new Vector2(x, y);
        Vector2 size = new Vector2(w, h);
        Vector2 scaling = new Vector2(scaleX, scaleY);

        // từ pixel sang world units
        position.scl(RPGGame.UNIT_SCALE);
        size.scl(RPGGame.UNIT_SCALE);

        entity.add(new Transform(position, z, size, scaling, 0f, sortOffsetY));
    }

    private TextureRegion getTextureRegion(TiledMapTile tile) {
        String atlasAssetStr = tile.getProperties().get("atlasAsset", AtlasAsset.OBJECTS.name(), String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        TextureAtlas textureAtlas = this.assetService.get(atlasAsset);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion(atlasKey + "/" + atlasKey);
        if (region != null) {
            return region;
        }

        // fallback: dùng region của Tiled editor
        return tile.getTextureRegion();
    }

    // ==================== PHYSICS HELPERS ====================

    /**
     * Tạo Physic component + Body cho entity nếu tile có collision objects.
     */
    private void addEntityPhysic(TiledMapTile tile, Entity entity) {
        if (tile.getObjects().getCount() == 0) {
            // tile này không có collider trong tileset
            return;
        }

        Transform transform = Transform.MAPPER.get(entity);
        if (transform == null) {
            throw new GdxRuntimeException("Entity phải có Transform trước khi thêm Physic");
        }

        Body body = createBody(
            tile.getObjects(),
            transform.getPosition(),   // vị trí entity trong world units
            transform.getScaling(),    // scale sprite → scale collider
            getObjectBodyType(tile),
            Vector2.Zero,
            entity                     // userData để contact listener nhận ra Entity
        );

        entity.add(new Physic(body));
    }

    /**
     * Xác định kiểu body dựa trên property của tile.
     * - type = "Prop" → StaticBody
     * - bodyType (nếu có) → DynamicBody / KinematicBody / StaticBody
     * - default = DynamicBody
     */
    private BodyDef.BodyType getObjectBodyType(TiledMapTile tile) {
        String classType = tile.getProperties().get("type", "", String.class);
        if ("Prop".equals(classType)) {
            return BodyDef.BodyType.StaticBody;
        }

        String bodyTypeStr = tile.getProperties().get("bodyType", "DynamicBody", String.class);
        return BodyDef.BodyType.valueOf(bodyTypeStr);
    }

    /**
     * Tạo body trong Box2D world + fixture cho mỗi MapObject.
     */
    private Body createBody(
        MapObjects mapObjects,
        Vector2 position,
        Vector2 scaling,
        BodyDef.BodyType bodyType,
        Vector2 relativeTo,
        Object userData
    ) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        Body body = this.physicWorld.createBody(bodyDef);
        body.setUserData(userData);

        for (MapObject object : mapObjects) {
            FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(object, scaling, relativeTo);
            Fixture fixture = body.createFixture(fixtureDef);
            fixture.setUserData(object.getName());
            fixtureDef.shape.dispose();
        }

        return body;
    }
}
