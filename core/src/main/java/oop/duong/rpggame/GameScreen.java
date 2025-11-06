package oop.duong.rpggame;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import oop.duong.rpggame.asset.AssetService;
import oop.duong.rpggame.asset.MapAsset;
import system.RenderSystem;

import static oop.duong.rpggame.RPGGame.UNIT_SCALE;



/** First screen of the application. Displayed after the application is created. */
public class GameScreen extends ScreenAdapter {
    private final RPGGame game;
    private final Batch  batch;
    private final AssetService assetService;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final Engine  engine;



    public GameScreen(RPGGame game) {
        this.game = game;
        this.assetService = game.getAssetService();
        this.viewport = game.getViewport();
        this.camera = game.getCamera();
        this.batch = game.getBatch();

        this.engine = new Engine();
        this.engine.addSystem(new RenderSystem(this.batch, this.viewport));

    }

    @Override
    public void show() {
        this.assetService.load(MapAsset.MAIN);
        this.engine.getSystem(RenderSystem.class).setMap(this.assetService.get(MapAsset.MAIN));
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

