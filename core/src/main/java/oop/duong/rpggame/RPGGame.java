package oop.duong.rpggame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Application; // Thêm cái này để dùng Application.LOG_DEBUG
import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import oop.duong.rpggame.asset.AssetService;
import oop.duong.rpggame.asset.AtlasAsset;
import oop.duong.rpggame.screen.GameScreen;

import java.util.HashMap;
import java.util.Map;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RPGGame extends Game {
    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 9f;
    public static final float UNIT_SCALE = 1f/16f;

    private Batch batch;
    private OrthographicCamera  camera;
    private Viewport viewport;
    private AssetService assetService;
    private GLProfiler glProfiler;
    private FPSLogger fpsLogger;
    private InputMultiplexer inputMultiplexer;

    private final Map<Class<? extends Screen>, Screen> screenCache = new HashMap<>();

    @Override
    public void create() {
        // --- BẮT ĐẦU CODE DÒ LỖI ---
        System.out.println("--------------------------------------------------");
        System.out.println("[1] TÔI ĐANG ĐỨNG TẠI: " + com.badlogic.gdx.Gdx.files.getLocalStoragePath());

        System.out.println("[2] KIỂM TRA THƯ MỤC 'maps':");
        com.badlogic.gdx.files.FileHandle mapsFolder = com.badlogic.gdx.Gdx.files.internal("maps");

        if (mapsFolder.exists()) {
            System.out.println("    -> Tìm thấy thư mục 'maps'! Bên trong có:");
            for (com.badlogic.gdx.files.FileHandle file : mapsFolder.list()) {
                System.out.println("       - " + file.name());
            }
        } else {
            System.out.println("    -> ❌ KHÔNG TÌM THẤY thư mục 'maps' (Kiểm tra lại Working Directory!)");
        }

        System.out.println("[3] KIỂM TRA FILE CỤ THỂ 'maps/MAP.tmx':");
        boolean exists = com.badlogic.gdx.Gdx.files.internal("maps/MAP.tmx").exists();
        System.out.println("    -> Kết quả: " + (exists ? "✅ TÌM THẤY!" : "❌ KHÔNG THẤY (Mù tịt)"));
        System.out.println("--------------------------------------------------");
        // --- KẾT THÚC CODE DÒ LỖI ---

        // ... Các code cũ của bạn (setLogLevel, batch...) để nguyên ở dưới ...
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        // ...
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);

        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        this.assetService = new AssetService(new InternalFileHandleResolver());

        assetService.load(AtlasAsset.OBJECTS);

        // QUAN TRỌNG: Bắt buộc game phải đứng đợi ở đây cho đến khi load xong 100%
        // Nếu không có dòng này, game chạy tiếp xuống dưới khi file chưa kịp đọc -> Crash
        assetService.finishLoading();

        this.glProfiler = new GLProfiler(Gdx.graphics);
        this.glProfiler.enable();
        this.fpsLogger = new FPSLogger();

        addScreen(new GameScreen(this)); // <-- Thêm dòng này

        setScreen(GameScreen.class);

    }

    @Override
    public void resize (int width, int height) {
        viewport.update(width, height, true);
        super.resize(width, height);
    }

    public void addScreen(Screen screen) {
        screenCache.put(screen.getClass(), screen);
    }

    public void removeScreen(Screen screen) {
        screenCache.remove(screen.getClass());
    }

    public void setScreen(Class<? extends Screen> screenClass) {
        Screen screen = screenCache.get(screenClass);
        if (screen == null) {
            throw new GdxRuntimeException("No Screen with class " + screenClass + " found in  the screen cache");
        }
        super.setScreen(screen);

    }

    @Override
    public void render() {
        glProfiler.reset();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();

        Gdx.graphics.setTitle("RPG Game - Draw Calls: " + glProfiler.getDrawCalls());
        fpsLogger.log();
    }



    @Override
    public void dispose() {
        screenCache.values().forEach(Screen::dispose);
        screenCache.clear();
        this.batch.dispose();
        this.assetService.debugDiagnostics();
        this.assetService.dispose();
    }

    public Batch getBatch() {
        return batch;
    }

    public AssetService getAssetService() {
        return assetService;
    }
    public Viewport getViewport() {
        return viewport;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void setInputProcessors(InputProcessor... processors) {
        inputMultiplexer.clear();
        if (processors == null) return;

        for (InputProcessor processor : processors) {
            inputMultiplexer.addProcessor(processor);
        }
    }

}

