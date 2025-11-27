package oop.duong.rpggame.ui.model;

import com.badlogic.gdx.Gdx;
import oop.duong.rpggame.RPGGame;
import oop.duong.rpggame.audio.AudioService;
import oop.duong.rpggame.screen.GameScreen;

public class MenuViewModel extends ViewModel {

    private final AudioService audioService;

    public MenuViewModel(RPGGame game) {
        super(game);
        this.audioService = game.getAudioService();
    }

    public float getMusicVolume() {
        return audioService.getMusicVolume();
    }

    public float getSoundVolume() {
        return audioService.getSoundVolume();
    }

    public void setMusicVolume(float volume) {
        this.audioService.setMusicVolume(volume);
    }

    public void setSoundVolume(float volume) {
        this.audioService.setSoundVolume(volume);
    }

    public  void startGame() {
        game.setScreen(GameScreen.class);
    }

    public  void quitGame() {
        Gdx.app.exit();
    }


}

