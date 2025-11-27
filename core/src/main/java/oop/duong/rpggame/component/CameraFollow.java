package oop.duong.rpggame.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Đánh dấu entity được camera follow.
 * Thường chỉ có Player gắn component này.
 */
public class CameraFollow implements Component {
    public static final ComponentMapper<CameraFollow> MAPPER =
        ComponentMapper.getFor(CameraFollow.class);
}
