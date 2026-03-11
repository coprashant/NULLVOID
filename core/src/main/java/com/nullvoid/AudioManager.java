package com.nullvoid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

// Owns all audio
public class AudioManager {

    private static final String PREFS_NAME = "nullvoid.prefs";
    private static final String KEY_MUTED  = "muted";

    private static final float BGM_VOLUME  = 0.4f;
    private static final float SFX_VOLUME  = 0.8f;
    private static final float GEM_VOLUME  = 1.0f;

    private Music bgm;
    private Sound jumpSfx;
    private Sound gemSfx;

    private boolean muted;
    private Preferences prefs;

    // Lifecycle

    public void create() {
        prefs   = Gdx.app.getPreferences(PREFS_NAME);
        muted   = prefs.getBoolean(KEY_MUTED, false);

        bgm     = Gdx.audio.newMusic(Gdx.files.internal("BGM.ogg"));
        jumpSfx = Gdx.audio.newSound(Gdx.files.internal("Jump SFX.ogg"));
        gemSfx  = Gdx.audio.newSound(Gdx.files.internal("Gem gather sfx.ogg"));

        bgm.setLooping(true);
        bgm.setVolume(muted ? 0f : BGM_VOLUME);
    }

    public void dispose() {
        bgm.dispose();
        jumpSfx.dispose();
        gemSfx.dispose();
    }

    // BGM control

    public void playBGM() {
        if (!bgm.isPlaying()) bgm.play();
        bgm.setVolume(muted ? 0f : BGM_VOLUME);
    }

    public void pauseBGM() {
        bgm.pause();
    }

    public void resumeBGM() {
        bgm.play();
        bgm.setVolume(muted ? 0f : BGM_VOLUME);
    }

    public void stopBGM() {
        bgm.stop();
    }

    // SFX

    public void playJump() {
        if (!muted) jumpSfx.play(SFX_VOLUME);
    }

    public void playGem() {
        if (!muted) gemSfx.play(GEM_VOLUME);
    }

    // Mute toggle

    public void toggleMute() {
        muted = !muted;
        bgm.setVolume(muted ? 0f : BGM_VOLUME);
        prefs.putBoolean(KEY_MUTED, muted);
        prefs.flush();
    }

    public boolean isMuted() { return muted; }
}