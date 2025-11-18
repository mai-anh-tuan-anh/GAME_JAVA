package oop.duong.rpggame.input;

public interface ControllerState {
    void keyDown(Command command);

    default void keyUp(Command command) {}

}
