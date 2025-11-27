package oop.duong.rpggame.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.asset.MapAsset;
import oop.duong.rpggame.input.GameControllerState;
import oop.duong.rpggame.input.KeyboardController;
import oop.duong.rpggame.system.*;
import oop.duong.rpggame.tiled.TiledAshleyConfigurator;
import oop.duong.rpggame.tiled.TiledService;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;



import java.util.function.Consumer;


/** First screen of the application. Displayed after the application is created. */
public class GameScreen extends ScreenAdapter {
    private final Engine  engine;
    private final TiledService tiledService;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;
    private final KeyboardController keyboardController;
    private final RPGGame game;
    private final World physicWorld;
    private final Stage stage;
    private final Viewport uiViewport;




    public GameScreen(RPGGame game) {
        this.game = game;
        this.engine = new Engine();
        this.physicWorld = new World(new Vector2(0f, 0f), false); // autoClearForces = false
        this.physicWorld.setAutoClearForces(false);
        this.tiledService = new TiledService(game.getAssetService(), this.physicWorld);
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(
            this.engine,
            this.physicWorld,
            game.getAssetService()
        );

        this.keyboardController = new KeyboardController(GameControllerState.class,engine);
        this.uiViewport = new FitViewport(320f, 180f);
        this.stage = new Stage(uiViewport, game.getBatch());

        this.engine.addSystem(new ControllerSystem());
        this.engine.addSystem(new PhysicMoveSystem());

        this.engine.addSystem(new FsmSystem());
        this.engine.addSystem(new FacingSystem());
        float fixedTimeStep = 1f / 60f;
        this.engine.addSystem(new PhysicSystem(this.physicWorld, fixedTimeStep));
        this.engine.addSystem(new AnimationSystem(game.getAssetService()));
        this.engine.addSystem(new CameraSystem(game.getCamera()));
        this.engine.addSystem(new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera()));
        this.engine.addSystem(new PhysicDebugRenderSystem(this.physicWorld, game.getCamera()));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        game.setInputProcessors(keyboardController, stage);
        keyboardController.setActiveState(GameControllerState.class);

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer   = this.engine.getSystem(CameraSystem.class)::setMap;
        this.tiledService.setMapChangeConsumer(renderConsumer.andThen(cameraConsumer));

        this.tiledService.setLoadTileConsumer(this.tiledAshleyConfigurator::onLoadTile);
        this.tiledService.setLoadObjectConsumer(this.tiledAshleyConfigurator::onLoadObject);


        TiledMap tiledMap = this.tiledService.loadMap(MapAsset.MAIN);
        this.tiledService.setMap(tiledMap);

    }

    @Override
    public void hide() {
        this.engine.removeAllEntities();
        this.stage.clear();
    }

    @Override
    public void render(float delta) {

        delta = Math.min(delta,1 / 30f);
        this.engine.update(delta);

        if(Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            System.out.println("W was just pressed");
        }

        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void  dispose() {
        for(EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable disposableSystem) {
                disposableSystem.dispose();
            }
        }
        this.physicWorld.dispose();
        this.stage.dispose();
    }
}

