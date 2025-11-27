package oop.duong.rpggame.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.asset.SkinAsset;
import oop.duong.rpggame.ui.model.MenuViewModel;
import oop.duong.rpggame.ui.view.MenuView;

public class MenuScreen extends ScreenAdapter{

    private final RPGGame game;
    private final Stage stage;
    private final Skin skin;
    private final Viewport uiViewport;

    public MenuScreen(RPGGame game){
        this.game = game;
        this.uiViewport = new FitViewport(800f, 450f);
        this.stage = new Stage(uiViewport, game.getBatch());
        this.skin = game.getAssetService().get(SkinAsset.DEFAULT);
    }
    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        this.game.setInputProcessors(stage);

        this.stage.addActor(new MenuView(stage, skin, new MenuViewModel(game)));
    }

    @Override
    public void hide() {
        this.stage.clear();
    }

    @Override
    public void render(float delta) {
        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
