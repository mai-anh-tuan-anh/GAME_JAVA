package oop.duong.rpggame.screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;

import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.asset.AssetService;
import oop.duong.rpggame.asset.AtlasAsset;
import oop.duong.rpggame.asset.SkinAsset;
import oop.duong.rpggame.asset.SoundAsset;

public class LoadingScreen extends ScreenAdapter {

    private final RPGGame game;
    private final AssetService assetService;

    public LoadingScreen(RPGGame game) {
        this.game = game;
        this.assetService = game.getAssetService();
    }

    @Override
    public void show() {
        for (AtlasAsset atlasAsset : AtlasAsset.values()) {
            assetService.queue(atlasAsset);
        }
        assetService.queue(SkinAsset.DEFAULT);

        for (SoundAsset sound : SoundAsset.values()) {
            assetService.queue(sound);
        }
    }

    @Override
    public void render(float delta) {
        if (this.assetService.update()) {
            Gdx.app.debug("LoadingScreen", "Finished loading assets");
            createScreens();
            this.game.removeScreen(this);
            this.dispose();
            this.game.setScreen(MenuScreen.class);
        }
    }

    private void createScreens() {
        this.game.addScreen(new MenuScreen(this.game));
        this.game.addScreen(new GameScreen(this.game));
    }
}
