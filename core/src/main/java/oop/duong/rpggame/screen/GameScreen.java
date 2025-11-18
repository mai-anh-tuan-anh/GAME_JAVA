package oop.duong.rpggame.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.asset.AssetService;
import oop.duong.rpggame.asset.MapAsset;
import oop.duong.rpggame.input.GameControllerState;
import oop.duong.rpggame.input.KeyboardController;
import oop.duong.rpggame.system.ControllerSystem;
import oop.duong.rpggame.system.MoveSystem;
import oop.duong.rpggame.system.RenderSystem;
import oop.duong.rpggame.tiled.TiledAshleyConfigurator;
import oop.duong.rpggame.tiled.TiledService;

import java.util.function.Consumer;


/** First screen of the application. Displayed after the application is created. */
public class GameScreen extends ScreenAdapter {
    private final Engine  engine;
    private final TiledService tiledService;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;
    private final KeyboardController keyboardController;
    private final RPGGame game;


    public GameScreen(RPGGame game) {
        this.game = game;
        this.tiledService = new TiledService(game.getAssetService());
        this.engine = new Engine();
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(this.engine, game.getAssetService());
        this.keyboardController = new KeyboardController(GameControllerState.class,engine);

        this.engine.addSystem(new ControllerSystem());
        this.engine.addSystem(new MoveSystem());
        this.engine.addSystem(new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera()));

    }

    @Override
    public void show() {
        game.setInputProcessors(keyboardController);
        keyboardController.setActiveState(GameControllerState.class);

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        this.tiledService.setMapChangeConsumer(renderConsumer);
        this.tiledService.setLoadObjectConsumer(this.tiledAshleyConfigurator::onLoadObject);


        TiledMap tiledMap = this.tiledService.loadMap(MapAsset.MAIN);
        this.tiledService.setMap(tiledMap);
    }

    @Override
    public void hide() {
        this.engine.removeAllEntities();
    }

    @Override
    public void render(float delta) {

        delta = Math.min(delta,1 / 30f);
        this.engine.update(delta);
        if(Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            System.out.println("W was just pressed");
        }
    }

    @Override
    public void  dispose() {
        for(EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable disposableSystem) {
                disposableSystem.dispose();
            }

        }


    }

}

