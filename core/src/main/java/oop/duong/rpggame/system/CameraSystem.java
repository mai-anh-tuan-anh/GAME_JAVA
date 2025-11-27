package oop.duong.rpggame.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.component.CameraFollow;
import oop.duong.rpggame.component.Transform;

/**
 * Camera follow entity có CameraFollow component.
 * - Smooth follow bằng lerp
 * - Giới hạn trong map
 */
public class CameraSystem extends IteratingSystem {

    private static final float CAM_OFFSET_Y = 1f;

    private final Camera camera;
    private final float smoothingFactor = 4f;
    private final Vector2 target = new Vector2();

    private float mapW;
    private float mapH;

    public CameraSystem(Camera camera) {
        super(Family.all(CameraFollow.class, Transform.class).get());
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);

        calcTargetPosition(transform.getPosition());

        float lerp = smoothingFactor * deltaTime;
        camera.position.x = MathUtils.lerp(camera.position.x, target.x, lerp);
        camera.position.y = MathUtils.lerp(camera.position.y, target.y, lerp);
    }

    private void calcTargetPosition(Vector2 pos) {
        float tx = pos.x;
        float ty = pos.y + CAM_OFFSET_Y;

        float halfW = camera.viewportWidth * 0.5f;
        float halfH = camera.viewportHeight * 0.5f;

        if (mapW > camera.viewportWidth) {
            tx = MathUtils.clamp(tx, halfW, mapW - halfW);
        }
        if (mapH > camera.viewportHeight) {
            ty = MathUtils.clamp(ty, halfH, mapH - halfH);
        }

        target.set(tx, ty);
    }

    public void setMap(TiledMap map) {
        int width = map.getProperties().get("width", Integer.class);
        int height = map.getProperties().get("height", Integer.class);
        int tileW = map.getProperties().get("tilewidth", Integer.class);
        int tileH = map.getProperties().get("tileheight", Integer.class);

        mapW = width * tileW * RPGGame.UNIT_SCALE;
        mapH = height * tileH * RPGGame.UNIT_SCALE;

        if (getEntities().size() > 0) {
            processEntity(getEntities().first(), 0);
        }
    }
}
