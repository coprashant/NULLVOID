package com.nullvoid;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * NullVoid — Main Game Class
 *
 * This class is the entry point for LibGDX.
 * LibGDX calls these methods automatically:
 *
 *   create()  → called once when the game starts  (like a constructor)
 *   render()  → called ~60 times per second        (your game loop)
 *   dispose() → called when game closes            (free memory)
 *
 * Think of render() as: update logic → clear screen → draw everything
 */
public class NullVoid extends ApplicationAdapter {

    // ── Rendering tools ───────────────────────────────────────
    private OrthographicCamera camera;   // 2D "view" into the world
    private ShapeRenderer shapes;        // draws rectangles, circles, lines
    private SpriteBatch batch;           // draws text and sprites
    private BitmapFont font;             // default LibGDX font

    // ── Game world dimensions ──────────────────────────────────
    public static final float WORLD_W = 800f;
    public static final float WORLD_H = 480f;

    // ── Game state machine ─────────────────────────────────────
    // The game is always in exactly ONE of these states
    public enum State { MENU, PLAYING, GAME_OVER }
    private State state = State.MENU;

    // ── Sub-systems (each handles one concern) ─────────────────
    private GameWorld  world;      // packet, bits, gates, scrolling
    private GameUI     ui;         // score display, menus, overlays
    private InputHandler input;    // keyboard / touch input

    // ──────────────────────────────────────────────────────────
    //  LIFECYCLE
    // ──────────────────────────────────────────────────────────

    @Override
    public void create() {
        // Camera: we tell it "show a 800×480 region"
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_W, WORLD_H);

        shapes = new ShapeRenderer();
        batch  = new SpriteBatch();
        font   = new BitmapFont();          // built-in fallback font
        font.setColor(Color.WHITE);

        // Wire up sub-systems
        world  = new GameWorld();
        ui     = new GameUI(batch, font, shapes);
        input  = new InputHandler();
    }

    @Override
    public void render() {
        // 1. Read player input
        handleInput();

        // 2. Update logic (only when playing)
        if (state == State.PLAYING) {
            world.update(Gdx.graphics.getDeltaTime());  // deltaTime = seconds since last frame
            if (world.isGameOver()) {
                state = State.GAME_OVER;
            }
        }

        // 3. Clear the screen
        Gdx.gl.glClearColor(0.04f, 0.04f, 0.10f, 1f);  // deep navy background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 4. Apply camera to all renderers
        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // 5. Draw world (background grid, bits, packet, gates)
        if (state == State.PLAYING || state == State.GAME_OVER) {
            world.render(shapes, batch, font);
        }

        // 6. Draw UI overlays on top
        ui.render(state, world);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        world.dispose();
    }

    // ──────────────────────────────────────────────────────────
    //  INPUT ROUTING
    // ──────────────────────────────────────────────────────────

    private void handleInput() {
        switch (state) {
            case MENU:
                if (input.isStartPressed()) {
                    world.reset();
                    state = State.PLAYING;
                }
                break;

            case PLAYING:
                // Pass lane toggle commands (1,2,3,4 keys or touch zones)
                // to the world. World decides if packet is on a pad.
                world.handleInput(input);
                break;

            case GAME_OVER:
                if (input.isStartPressed()) {
                    state = State.MENU;
                }
                break;
        }
    }
}
