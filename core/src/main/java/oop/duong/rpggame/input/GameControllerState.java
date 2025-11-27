package oop.duong.rpggame.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import oop.duong.rpggame.component.Controller;

public class GameControllerState implements ControllerState {

    private final Engine engine;

    public GameControllerState(Engine engine) {
        this.engine = engine;
    }

    private ImmutableArray<Entity> getControllerEntities() {
        return engine.getEntitiesFor(Family.all(Controller.class).get());
    }

    @Override
    public void keyDown(Command command) {
        ImmutableArray<Entity> controllerEntities = getControllerEntities();
        for (Entity entity : controllerEntities) {
            Controller.MAPPER.get(entity).getPressedCommands().add(command);
        }
    }

    @Override
    public void keyUp(Command command) {
        ImmutableArray<Entity> controllerEntities = getControllerEntities();
        for (Entity entity : controllerEntities) {
            Controller.MAPPER.get(entity).getReleasedCommands().add(command);
        }
    }
}
