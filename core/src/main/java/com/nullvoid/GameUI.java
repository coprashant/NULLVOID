package com.nullvoid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * GameUI — draws all overlays (menu, game over screen, HUD labels).
 *
 * World handles game logic drawing.
 * UI handles everything that sits ON TOP of the world.
 */
public class GameUI {

    private SpriteBatch   batch;
    private BitmapFont    font;
    private ShapeRenderer shapes;

    private static final float W = NullVoid.WORLD_W;
    private static final float H = NullVoid.WORLD_H;

    public GameUI(SpriteBatch batch, BitmapFont font, ShapeRenderer shapes) {
        this.batch  = batch;
        this.font   = font;
        this.shapes = shapes;
    }

    public void render(NullVoid.State state, GameWorld world) {
        switch (state) {
            case MENU:     drawMenu();           break;
            case GAME_OVER: drawGameOver(world); break;
            case PLAYING:  drawPlayingHUD(world);break;
        }
    }

    // ── MENU SCREEN ────────────────────────────────────────────
    private void drawMenu() {
        // Semi-transparent dark overlay
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.7f);
        shapes.rect(0, 0, W, H);
        shapes.end();

        batch.begin();

        font.setColor(0f, 1f, 0.8f, 1f);
        font.draw(batch, "N U L L V O I D",    W/2 - 80f, H/2 + 80f);

        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Move UP/DOWN to toggle bits",  W/2 - 110f, H/2 + 30f);
        font.draw(batch, "Match the gate's decimal value to pass", W/2 - 140f, H/2);
        font.draw(batch, "Bits: 8  4  2  1  (top to bottom)", W/2 - 120f, H/2 - 30f);

        font.setColor(Color.YELLOW);
        font.draw(batch, "PRESS SPACE / TAP TO START",  W/2 - 120f, H/2 - 80f);

        batch.end();
    }

    // ── GAME OVER SCREEN ───────────────────────────────────────
    private void drawGameOver(GameWorld world) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.8f);
        shapes.rect(0, 0, W, H);
        shapes.end();

        batch.begin();

        font.setColor(1f, 0.2f, 0.4f, 1f);
        font.draw(batch, "PACKET DROPPED!",       W/2 - 80f, H/2 + 60f);

        font.setColor(Color.WHITE);
        font.draw(batch, "Score:      " + (int)world.getScore(),  W/2 - 60f, H/2 + 20f);
        font.draw(batch, "High Score: " + world.getHighScore(),   W/2 - 60f, H/2);

        font.setColor(Color.YELLOW);
        font.draw(batch, "PRESS SPACE to return to menu",         W/2 - 130f, H/2 - 50f);

        batch.end();
    }

    // ── PLAYING HUD (minimal — world draws most of it) ────────
    private void drawPlayingHUD(GameWorld world) {
        // Could add combo counter, speed indicator, etc. here later
        // The score + bits are already drawn by GameWorld.drawBitStatus()
    }
}
