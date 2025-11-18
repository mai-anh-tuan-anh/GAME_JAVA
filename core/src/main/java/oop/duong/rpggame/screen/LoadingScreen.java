package oop.duong.rpggame.screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;

import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.asset.AssetService;
import oop.duong.rpggame.asset.AtlasAsset;

public class LoadingScreen extends ScreenAdapter {

    private final RPGGame game;
    private final AssetService assetService;

    public LoadingScreen(RPGGame game) {
        this.game = game;
        this.assetService = game.getAssetService();
    }

    /**
     * Queues all required assets for loading.
     */
    @Override
    public void show() {
        for (AtlasAsset atlasAsset : AtlasAsset.values()) {
            assetService.queue(atlasAsset);
        }

    }

    /**
     * Updates asset loading progress and transitions to menu when complete.
     */
    @Override
    public void render(float delta) {
        if (assetService.update()) {
            Gdx.app.debug("LoadingScreen", "Finished loading assets");
            createScreens();
            this.game.removeScreen(this);
            this.dispose();

        }
    }

    private void createScreens() {
        this.game.addScreen(new GameScreen(this.game));
    }
}
