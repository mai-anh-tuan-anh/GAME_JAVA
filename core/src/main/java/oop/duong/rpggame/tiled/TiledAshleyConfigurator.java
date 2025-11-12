package oop.duong.rpggame.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import oop.duong.rpggame.asset.AtlasAsset;
import oop.duong.rpggame.component.Graphic;
import oop.duong.rpggame.component.Transform;
import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.asset.AssetService;

public class TiledAshleyConfigurator {
    private final Engine engine;
    private final AssetService assetService;

    public  TiledAshleyConfigurator(Engine engine, AssetService assetService) {
        this.engine = engine;
        this.assetService = assetService;
    }

    public void onLoadObject (TiledMapTileMapObject tileMapObject) {
        Entity entity = this.engine.createEntity();
        TiledMapTile tile = tileMapObject.getTile();
        TextureRegion textureRegion = getTextureRegion(tile);
        float sortOffsetY = tile.getProperties().get("sortOffsetY", 0, Integer.class);
        sortOffsetY *= RPGGame.UNIT_SCALE;
        int z = tile.getProperties().get("z", 1, Integer.class);

        entity.add(new Graphic(Color.WHITE.cpy(), textureRegion));
        addEntityTransform( tileMapObject.getX(), tileMapObject.getY(), z,
            textureRegion.getRegionWidth(), textureRegion.getRegionHeight(),
            tileMapObject.getScaleX(), tileMapObject.getScaleY(),
            sortOffsetY,
            entity);


        this.engine.addEntity(entity);
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

        position.scl(RPGGame.UNIT_SCALE);
        size.scl(RPGGame.UNIT_SCALE);

        entity.add(new Transform(position, z, size, scaling, 0f, sortOffsetY));
    }

    private TextureRegion getTextureRegion(TiledMapTile tile) {
        String atlasAssetStr = tile.getProperties().get("atlasAsset", AtlasAsset.OBJECTS.name(),String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        TextureAtlas textureAtlas = this.assetService.get(atlasAsset);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion((atlasKey + "/" + atlasKey));
        if (region != null) {
            return region;
        }

        return tile.getTextureRegion();

    }
}
