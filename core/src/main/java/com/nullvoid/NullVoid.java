package com.nullvoid;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NullVoid extends ApplicationAdapter {

    public static final float W = 480f;
    public static final float H = 270f;

    public enum State { MENU, PLAYING, GAME_OVER }
    public State state = State.MENU;

    public OrthographicCamera camera;
    public SpriteBatch batch;

    public GameWorld   world;
    public GameUI      ui;
    public InputHandler input;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        batch  = new SpriteBatch();
        input  = new InputHandler();
        world  = new GameWorld();
        ui     = new GameUI(batch);
        world.create();
        ui.create();
    }

    @Override
    public void render() {
        handleInput();

        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        if (state == State.PLAYING) {
            world.update(delta, input);
            if (world.isGameOver()) state = State.GAME_OVER;
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

    private void handleInput() {
        switch (state) {
            case MENU:
                if (input.isStart()) {
                    world.reset();
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
}