package oop.duong.rpggame.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public enum MapAsset implements Asset<TiledMap> {
    MAIN("main.tmx");
    private final AssetDescriptor<TiledMap> descriptor;

    MapAsset(String mapName) {
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        // TODO:
        parameters.projectFilePath = "maps/MAP.tmx" + mapName; // Remember to add map name;
        this.descriptor = new AssetDescriptor<>("maps/" + mapName, TiledMap.class);
    }

    @Override
    public AssetDescriptor<TiledMap> getDescriptor() {
        return this.descriptor;
    }
}
