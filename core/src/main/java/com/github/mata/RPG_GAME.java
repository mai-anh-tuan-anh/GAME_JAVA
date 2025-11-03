package com.github.mata;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RPG_GAME extends Game {
    @Override
    public void create() {
        setScreen(new FirstScreen());
    }
}