package oop.duong.rpggame.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapLoader;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.ray3k.stripe.FreeTypeSkinLoader;

import java.util.logging.FileHandler;

public class AssetService implements Disposable {
    private final AssetManager assetManager;

    public AssetService(FileHandleResolver fileHandleResolver) {
        this.assetManager = new AssetManager(fileHandleResolver);
        this.assetManager.setLoader(TiledMap.class, new TmxMapLoader());
        this.assetManager.setLoader(Skin.class, new FreeTypeSkinLoader(fileHandleResolver));
    }

    public <T> T load(Asset<T>  asset) {
        this.assetManager.load(asset.getDescriptor());
        this.assetManager.finishLoading();
        return this.assetManager.get(asset.getDescriptor());
    }

    public <T> void queue(Asset<T>  asset) {
        this.assetManager.load(asset.getDescriptor());
    }

    public <T> T get(Asset<T>  asset) {
        return this.assetManager.get(asset.getDescriptor());
    }

    public <T> void unload(Asset<T> asset) {
        this.assetManager.unload(asset.getDescriptor().fileName);
    }

    public boolean update() {
        return this.assetManager.update();
    }

    public void debugDiagnostics() {
        Gdx.app.debug("AssetService", this.assetManager.getDiagnostics());
    }

    public void finishLoading() {
        this.assetManager.finishLoading();
    }

    @Override
    public void dispose() {
        this.assetManager.dispose();
    }


}
