package oop.duong.rpggame.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import oop.duong.rpggame.component.Physic;
import oop.duong.rpggame.component.Transform;

/**
 * System update thế giới Box2D với fixed timestep + nội suy vị trí.
 * Bám sát PhysicSystem của project mẫu, nhưng tạm thời
 * chưa xử lý Player/Trigger cho đỡ phức tạp.
 */
public class PhysicSystem extends IteratingSystem implements EntityListener {

    private final World world;
    private final float interval;   // bước thời gian cố định (vd 1/60f)
    private float accumulator = 0f; // tích lũy delta

    public PhysicSystem(World world, float interval) {
        // Chạy trên mọi entity có cả Physic + Transform
        super(Family.all(Physic.class, Transform.class).get());
        this.world = world;
        this.interval = interval;

        // Ta sẽ tự clear forces sau mỗi frame
        this.world.setAutoClearForces(false);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        // Đăng ký listener để dọn body khi entity bị remove
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        engine.removeEntityListener(this);
    }

    /**
     * Fixed timestep + interpolation.
     */
    @Override
    public void update(float deltaTime) {
        accumulator += deltaTime;

        // Lặp nhiều lần nếu frame quá dài
        while (accumulator >= interval) {
            accumulator -= interval;

            // Lưu vị trí cũ trước khi step
            for (int i = 0; i < getEntities().size(); ++i) {
                Entity e = getEntities().get(i);
                Physic physic = Physic.MAPPER.get(e);
                physic.getPrevPosition().set(physic.getBody().getPosition());
            }

            // Step Box2D world
            world.step(interval, 6, 2);
        }

        // Clear forces (vì setAutoClearForces(false))
        world.clearForces();

        // Nội suy Transform.position giữa prevPosition và body.position
        float alpha = accumulator / interval;
        for (int i = 0; i < getEntities().size(); ++i) {
            interpolateEntity(getEntities().get(i), alpha);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Không cần dùng – mọi việc đã làm trong update()
    }

    private void interpolateEntity(Entity entity, float alpha) {
        Physic physic = Physic.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        float bx = physic.getBody().getPosition().x;
        float by = physic.getBody().getPosition().y;
        float px = physic.getPrevPosition().x;
        float py = physic.getPrevPosition().y;

        transform.getPosition().set(
            MathUtils.lerp(px, bx, alpha),
            MathUtils.lerp(py, by, alpha)
        );
    }

    @Override
    public void entityAdded(Entity entity) {
        // không cần làm gì khi thêm
    }

    @Override
    public void entityRemoved(Entity entity) {
        // Khi entity có Physic bị remove, hủy luôn body trong world
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null) {
            Body body = physic.getBody();
            body.getWorld().destroyBody(body);
        }
    }
}
