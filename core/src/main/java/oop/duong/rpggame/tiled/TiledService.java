package oop.duong.rpggame.tiled;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.asset.AssetService;
import oop.duong.rpggame.asset.MapAsset;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * TiledService xử lý:
 * - Load map từ AssetService
 * - Xóa body cũ khi đổi map
 * - Duyệt tile layer → loadTileConsumer
 * - Duyệt object layer → loadObjectConsumer
 * - Duyệt trigger layer → loadTriggerConsumer
 * - Tạo “boundary” (tường bao quanh map)
 *
 * Đây là phiên bản rút gọn và đơn giản hoá từ project mẫu.
 */
public class TiledService {

    private final AssetService assetService;
    private final World physicWorld;

    private TiledMap currentMap;

    // Callback tương ứng từng loại layer
    private Consumer<TiledMap> mapChangeConsumer;
    private Consumer<TiledMapTileMapObject> loadObjectConsumer;
    private BiConsumer<String, MapObject> loadTriggerConsumer;
    private LoadTileConsumer loadTileConsumer;

    public TiledService(AssetService assetService, World physicWorld) {
        this.assetService = assetService;
        this.physicWorld = physicWorld;
    }

    public TiledMap loadMap(MapAsset mapAsset) {
        TiledMap tiledMap = this.assetService.load(mapAsset);
        tiledMap.getProperties().put("mapAsset", mapAsset);
        return tiledMap;
    }

    public void setMap(TiledMap tiledMap) {
        // Xóa map cũ (nếu có)
        if (this.currentMap != null) {
            this.assetService.unload(this.currentMap.getProperties().get("mapAsset", MapAsset.class));

            // Các body có userData = "environment" sẽ được xóa khi chuyển map
            Array<Body> bodies = new Array<>();
            physicWorld.getBodies(bodies);
            for (Body body : bodies) {
                if ("environment".equals(body.getUserData())) {
                    physicWorld.destroyBody(body);
                }
            }
        }

        this.currentMap = tiledMap;

        // Tải tile, object, trigger
        loadMapObjects(tiledMap);

        // callback map changed
        if (this.mapChangeConsumer != null) {
            this.mapChangeConsumer.accept(tiledMap);
        }
    }

    public void setMapChangeConsumer(Consumer<TiledMap> consumer) {
        this.mapChangeConsumer = consumer;
    }

    public void setLoadObjectConsumer(Consumer<TiledMapTileMapObject> consumer) {
        this.loadObjectConsumer = consumer;
    }

    public void setLoadTriggerConsumer(BiConsumer<String, MapObject> consumer) {
        this.loadTriggerConsumer = consumer;
    }

    public void setLoadTileConsumer(LoadTileConsumer consumer) {
        this.loadTileConsumer = consumer;
    }

    // ========== Đọc toàn bộ layer ==========

    private void loadMapObjects(TiledMap map) {
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TiledMapTileLayer tileLayer) {
                loadTileLayer(tileLayer);
            } else if ("objects".equals(layer.getName())) {
                loadObjectLayer(layer);
            } else if ("trigger".equals(layer.getName())) {
                loadTriggerLayer(layer);
            }
        }

        // tạo tường bao quanh map
        spawnMapBoundary(map);
    }

    // ========== Load từng loại layer ==========

    private void loadTileLayer(TiledMapTileLayer tileLayer) {
        if (loadTileConsumer == null) return;

        for (int y = 0; y < tileLayer.getHeight(); y++) {
            for (int x = 0; x < tileLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell != null) {
                    loadTileConsumer.accept(cell.getTile(), x, y);
                }
            }
        }
    }

    private void loadObjectLayer(MapLayer objectLayer) {
        if (loadObjectConsumer == null) return;

        for (MapObject obj : objectLayer.getObjects()) {
            if (obj instanceof TiledMapTileMapObject tileObj) {
                loadObjectConsumer.accept(tileObj);
            }
        }
    }

    private void loadTriggerLayer(MapLayer triggerLayer) {
        if (loadTriggerConsumer == null) return;

        for (MapObject obj : triggerLayer.getObjects()) {
            loadTriggerConsumer.accept(obj.getName(), obj);
        }
    }

    /**
     * Tạo tường bao quanh map.
     */
    private void spawnMapBoundary(TiledMap tiledMap) {
        int width = tiledMap.getProperties().get("width", 0, Integer.class);
        int height = tiledMap.getProperties().get("height", 0, Integer.class);
        int tileW = tiledMap.getProperties().get("tilewidth", 0, Integer.class);
        int tileH = tiledMap.getProperties().get("tileheight", 0, Integer.class);

        float mapW = width * tileW * RPGGame.UNIT_SCALE;
        float mapH = height * tileH * RPGGame.UNIT_SCALE;
        float halfW = mapW * 0.5f;
        float halfH = mapH * 0.5f;
        float thickness = 0.5f;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        Body body = physicWorld.createBody(def);
        body.setUserData("environment");

        PolygonShape shape = new PolygonShape();

        // left
        shape.setAsBox(thickness, halfH, new Vector2(-thickness, halfH), 0);
        body.createFixture(shape, 0);

        // right
        shape.setAsBox(thickness, halfH, new Vector2(mapW + thickness, halfH), 0);
        body.createFixture(shape, 0);

        // bottom
        shape.setAsBox(halfW, thickness, new Vector2(halfW, -thickness), 0);
        body.createFixture(shape, 0);

        // top
        shape.setAsBox(halfW, thickness, new Vector2(halfW, mapH + thickness), 0);
        body.createFixture(shape, 0);

        shape.dispose();
    }

    @FunctionalInterface
    public interface LoadTileConsumer {
        void accept(TiledMapTile tile, float x, float y);
    }
}
