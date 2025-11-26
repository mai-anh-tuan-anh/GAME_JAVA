package oop.duong.rpggame.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import oop.duong.rpggame.component.Move;
import oop.duong.rpggame.component.Physic;

/**
 * Điều khiển chuyển động của entity có Physic + Move.
 *
 * - Nếu direction = (0,0) hoặc isRooted = true → dừng hẳn body.
 * - Nếu có direction → setLinearVelocity theo hướng đó.
 *
 * Đây là bản giống project mẫu (rút gọn và thêm chú thích).
 */
public class PhysicMoveSystem extends IteratingSystem {

    private static final Vector2 TMP = new Vector2();

    public PhysicMoveSystem() {
        super(Family.all(Physic.class, Move.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Move move = Move.MAPPER.get(entity);
        Physic physic = Physic.MAPPER.get(entity);

        Body body = physic.getBody();

        // Nếu đứng yên hoặc bị khoá (rooted)
        if (move.isRooted() || move.getDirection().isZero(0.01f)) {
            body.setLinearVelocity(0f, 0f);
            return;
        }

        // Tính vận tốc theo hướng normalized
        TMP.set(move.getDirection()).nor();
        float speed = move.getMaxSpeed();

        body.setLinearVelocity(
            TMP.x * speed,
            TMP.y * speed
        );
    }
}
