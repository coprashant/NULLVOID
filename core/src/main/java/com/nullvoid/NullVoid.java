package com.nullvoid;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NullVoid extends ApplicationAdapter {

    public static final float W = 480f;
    public static final float H = 270f;

    private static final String PREFS_NAME     = "nullvoid.prefs";
    private static final String KEY_HIGH_SCORE = "highScore";

    public enum State { MENU, PLAYING, GAME_OVER }
    public State state = State.MENU;

    public OrthographicCamera camera;
    public SpriteBatch        batch;

    public GameWorld    world;
    public GameUI       ui;
    public InputHandler input;

    private Preferences prefs;

    // ── Lifecycle ──────────────────────────────────────────────

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        batch  = new SpriteBatch();
        input  = new InputHandler();
        world  = new GameWorld();
        ui     = new GameUI(batch);

        // Load persisted high score before world is created
        prefs  = Gdx.app.getPreferences(PREFS_NAME);

        world.create();
        ui.create();

        // Push saved high score into the world so it shows on first run
        world.setHighScore(prefs.getInteger(KEY_HIGH_SCORE, 0));
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);

        handleInput();

        if (state == State.PLAYING) {
            world.update(delta, input);

            if (world.isGameOver()) {
                state = State.GAME_OVER;
                saveHighScore();   // persist as soon as game ends
            }
        }

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        world.render(batch);
        ui.render(state, world, camera);
    }

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
        ui.dispose();
    }

    // ── Input handling ─────────────────────────────────────────

    private void handleInput() {
        switch (state) {
            case MENU:
                if (input.isStart()) {
                    // Carry the persisted high score into the new run
                    int savedBest = prefs.getInteger(KEY_HIGH_SCORE, 0);
                    world.reset();
                    world.setHighScore(savedBest);
                    state = State.PLAYING;
                }
                break;
            case GAME_OVER:
                if (input.isStart()) state = State.MENU;
                break;
            default:
                break;
        }
    }

    // ── Persistence ────────────────────────────────────────────

    private void saveHighScore() {
        int current = prefs.getInteger(KEY_HIGH_SCORE, 0);
        int latest  = world.getHighScore();
        if (latest > current) {
            prefs.putInteger(KEY_HIGH_SCORE, latest);
            prefs.flush();
        }
    }
}