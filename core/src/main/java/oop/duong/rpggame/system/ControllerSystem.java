package oop.duong.rpggame.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import oop.duong.rpggame.asset.SoundAsset;
import oop.duong.rpggame.audio.AudioService;
import oop.duong.rpggame.component.Controller;
import oop.duong.rpggame.component.Move;
import oop.duong.rpggame.input.Command;

public class ControllerSystem extends IteratingSystem {

    private final AudioService audioService;

    public ControllerSystem(AudioService audioService) {

        super(Family.all(Controller.class).get());
        this.audioService = audioService;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Controller controller = Controller.MAPPER.get(entity);
        if (controller.getPressedCommands().isEmpty() && controller.getReleasedCommands().isEmpty()) {
            return;
        }
        for (Command command : controller.getPressedCommands()) {
            switch (command) {
                case UP -> moveEntity(entity,0f,1f);
                case DOWN -> moveEntity(entity,0f,-1f);
                case RIGHT -> moveEntity(entity,1f,0f);
                case LEFT -> moveEntity(entity,-1f,0f);
                case SELECT -> startEntityAttack(entity);
            }
        }
        controller.getPressedCommands().clear();

        for (Command command : controller.getReleasedCommands()) {
            switch (command) {
                case UP -> moveEntity(entity,0f,-1f);
                case DOWN -> moveEntity(entity,0f,1f);
                case RIGHT -> moveEntity(entity,-1f,0f);
                case LEFT -> moveEntity(entity,1f,0f);
            }
        }
        controller.getReleasedCommands().clear();
    }
    private void startEntityAttack(Entity entity) {

        audioService.playSound(SoundAsset.SWORD_HIT);

    }

    private void moveEntity(Entity entity, float directionX, float directionY) {
        Move move = Move.MAPPER.get(entity);
        if (move == null) {
            return;
        }
        move.getDirection().x += directionX;
        move.getDirection().y += directionY;
    }
}
