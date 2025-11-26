package oop.duong.rpggame.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Component “dán” một Body Box2D vào Entity của Ashley.
 * - body        : body thật trong thế giới vật lý.
 * - prevPosition: vị trí của body ở bước mô phỏng trước
 *                 (dùng để nội suy trong PhysicSystem).
 */
public class Physic implements Component {
    public static final ComponentMapper<Physic> MAPPER = ComponentMapper.getFor(Physic.class);

    private final Body body;
    private final Vector2 prevPosition;

    public Physic(Body body) {
        this.body = body;
        // Lưu vị trí hiện tại làm “vị trí trước đó” ban đầu
        this.prevPosition = new Vector2(body.getPosition());
    }

    public Body getBody() {
        return body;
    }

    /**
     * Vị trí body ở bước mô phỏng trước.
     * PhysicSystem sẽ cập nhật giá trị này mỗi lần step().
     */
    public Vector2 getPrevPosition() {
        return prevPosition;
    }
}
