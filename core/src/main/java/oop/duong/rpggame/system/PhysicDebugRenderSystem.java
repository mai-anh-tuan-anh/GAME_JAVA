package oop.duong.rpggame.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

/**
 * Vẽ debug collider Box2D (đường viền xanh/đỏ).
 * Chỉ dùng để phát triển / làm báo cáo.
 */
public class PhysicDebugRenderSystem extends EntitySystem implements Disposable {

    private final World world;
    private final Camera camera;
    private final Box2DDebugRenderer debugRenderer;

    private boolean enabled = true;

    public PhysicDebugRenderSystem(World world, Camera camera) {
        this.world = world;
        this.camera = camera;
        this.debugRenderer = new Box2DDebugRenderer();
    }

    @Override
    public void update(float deltaTime) {
        if (!enabled) {
            return;
        }
        debugRenderer.render(world, camera.combined);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void dispose() {
        debugRenderer.dispose();
    }
}
