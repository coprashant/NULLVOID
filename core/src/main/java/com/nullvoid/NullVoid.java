package com.nullvoid;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class NullVoid extends ApplicationAdapter {

    public static final float W = 480f;
    public static final float H = 270f;

    private static final String PREFS_NAME     = "nullvoid.prefs";
    private static final String KEY_HIGH_SCORE = "highScore";
    // Scores stored as individual keys: score_0 .. score_4
    private static final String KEY_SCORE_PREFIX = "score_";

    public enum State { MENU, PLAYING, PAUSED, GAME_OVER, LEADERBOARD }
    public State state = State.MENU;

    public OrthographicCamera camera;
    public Viewport            viewport;
    public SpriteBatch         batch;

    public GameWorld    world;
    public GameUI       ui;
    public InputHandler input;
    public AudioManager audio;

    private Preferences prefs;

    @Override
    public void create() {
        camera   = new OrthographicCamera();
        viewport = new FitViewport(W, H, camera);
        viewport.apply();
        camera.position.set(W / 2f, H / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        input = new InputHandler();
        world = new GameWorld();
        ui    = new GameUI(batch);
        audio = new AudioManager();

        prefs = Gdx.app.getPreferences(PREFS_NAME);

        world.create();
        ui.create();
        audio.create();

        world.setHighScore(prefs.getInteger(KEY_HIGH_SCORE, 0));
        loadLeaderboard();
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);

        handleInput();

        if (state == State.PLAYING) {
            world.update(delta, input);
            if (world.playerJustJumped())       audio.playJump();
            if (world.playerJustCollectedGem()) audio.playGem();
            if (world.isGameOver()) {
                state = State.GAME_OVER;
                audio.stopBGM();
                saveHighScore();
                world.submitScore(world.getScore());
                saveLeaderboard();
            }
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        world.render(batch);
        ui.render(state, world, camera, audio);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
        ui.dispose();
        audio.dispose();
    }

    private void handleInput() {
        boolean pauseBtn       = ui.wasPauseButtonPressed();
        boolean leaderboardBtn = ui.wasLeaderboardButtonPressed();

        switch (state) {
            case MENU:
                if (leaderboardBtn) {
                    state = State.LEADERBOARD;
                    break;
                }
                if (input.isStartWithTouch()) {
                    int savedBest = prefs.getInteger(KEY_HIGH_SCORE, 0);
                    world.reset();
                    world.setHighScore(savedBest);
                    ui.hudHintTimer = 3.5f;
                    state = State.PLAYING;
                    audio.playBGM();
                }
                break;

            case PLAYING:
                if (input.isPause() || pauseBtn) {
                    state = State.PAUSED;
                    audio.pauseBGM();
                }
                break;

            case PAUSED:
                if (ui.wasMuteToggled()) audio.toggleMute();
                if (input.isPause() || input.isStart()) {
                    state = State.PLAYING;
                    audio.resumeBGM();
                }
                break;

            case GAME_OVER:
                if (leaderboardBtn) {
                    state = State.LEADERBOARD;
                    break;
                }
                if (input.isStartWithTouch()) {
                    ui.hudHintTimer = 0f;
                    state = State.MENU;
                }
                break;

            case LEADERBOARD:
                if (input.isBack() || input.isStart()) {
                    state = State.MENU;
                }
                break;
        }
    }

    private void saveHighScore() {
        int current = prefs.getInteger(KEY_HIGH_SCORE, 0);
        int latest  = world.getHighScore();
        if (latest > current) {
            prefs.putInteger(KEY_HIGH_SCORE, latest);
            prefs.flush();
        }
    }

    private void loadLeaderboard() {
        int[] saved = new int[GameWorld.LEADERBOARD_SIZE];
        for (int i = 0; i < GameWorld.LEADERBOARD_SIZE; i++)
            saved[i] = prefs.getInteger(KEY_SCORE_PREFIX + i, 0);
        world.setScores(saved);
    }

    private void saveLeaderboard() {
        int[] s = world.getScores();
        for (int i = 0; i < GameWorld.LEADERBOARD_SIZE; i++)
            prefs.putInteger(KEY_SCORE_PREFIX + i, s[i]);
        prefs.flush();
    }
}