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

    public enum State { MENU, PLAYING, GAME_OVER }
    public State state = State.MENU;

    public OrthographicCamera camera;
    public Viewport            viewport;   // ← FitViewport keeps aspect ratio
    public SpriteBatch         batch;

    public GameWorld    world;
    public GameUI       ui;
    public InputHandler input;

    private Preferences prefs;

    // ── Lifecycle ──────────────────────────────────────────────

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

        prefs = Gdx.app.getPreferences(PREFS_NAME);

        world.create();
        ui.create();

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
                saveHighScore();
            }
        }

        // Clear with letterbox colour (black bars when aspect doesn't match)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Apply viewport — sets the GL scissor/viewport to the fit area
        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        world.render(batch);
        ui.render(state, world, camera);
    }

    @Override
    public void resize(int width, int height) {
        // Called whenever the window is resized — update viewport to refit
        viewport.update(width, height, true);
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