package oop.duong.rpggame.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.asset.AssetService;
import oop.duong.rpggame.asset.MapAsset;
import oop.duong.rpggame.system.RenderSystem;
import oop.duong.rpggame.tiled.TiledAshleyConfigurator;
import oop.duong.rpggame.tiled.TiledService;

import java.util.function.Consumer;


/** First screen of the application. Displayed after the application is created. */
public class GameScreen extends ScreenAdapter {
    private final RPGGame game;
    private final Batch  batch;
    private final AssetService assetService;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final Engine  engine;
    private final TiledService tiledService;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;



    public GameScreen(RPGGame game) {
        this.game = game;
        this.assetService = game.getAssetService();
        this.viewport = game.getViewport();
        this.camera = game.getCamera();
        this.batch = game.getBatch();
        this.tiledService = new TiledService(this.assetService);
        this.engine = new Engine();
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(this.engine,this.assetService);
        this.engine.addSystem(new RenderSystem(this.batch, this.viewport, this.camera));

    }

    @Override
    public void show() {

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        this.tiledService.setMapChangeConsumer(renderConsumer);
        this.tiledService.setLoadObjectConsumer(this.tiledAshleyConfigurator::onLoadObject);
        TiledMap tiledMap = this.tiledService.loadMap(MapAsset.MAIN);
        this.tiledService.setMap(tiledMap);;
    }

    @Override
    public void hide() {
        this.engine.removeAllEntities();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta,1 / 30f);
        this.engine.update(delta);

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

