package com.nullvoid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * GameUI — HUD, menus, game over screen.
 *
 * HUD shows:
 *   - 3 life icons (SmallAstronaut_Idle)
 *   - Score (top right)
 *   - Distance (top center)
 */
public class GameUI {

    private SpriteBatch   batch;
    private BitmapFont    font, bigFont;
    private GlyphLayout   layout = new GlyphLayout();
    private ShapeRenderer shapes;

    private Texture      lifeSheet;
    private TextureRegion lifeIcon;   // small astronaut for lives

    private Texture      gemTex;

    private static final float W = NullVoid.W;
    private static final float H = NullVoid.H;

    public GameUI(SpriteBatch batch) { this.batch = batch; }

    public void create() {
        font    = new BitmapFont();
        font.getData().setScale(1.1f);

        bigFont = new BitmapFont();
        bigFont.getData().setScale(2.2f);

        shapes  = new ShapeRenderer();

        // Life icon — SmallAstronaut_Idle.png
        lifeSheet = new Texture("SmallAstronaut_Idle.png");
        // Just use first frame as icon
        lifeIcon  = new TextureRegion(lifeSheet, 0, 0, 16, 16);

        gemTex = new Texture("Diamond.png");
    }

    public void render(NullVoid.State state, GameWorld world,
                       OrthographicCamera cam) {
        shapes.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);

        switch (state) {
            case MENU:      drawMenu();         break;
            case PLAYING:   drawHUD(world);     break;
            case GAME_OVER: drawGameOver(world);break;
        }
    }

    // ── MENU ───────────────────────────────────────────────────
    private void drawMenu() {
        drawOverlay(0.75f);

        batch.begin();

        bigFont.setColor(0.2f, 1f, 0.7f, 1f);
        drawCentered(bigFont, "N U L L V O I D", H * 0.74f);

        font.setColor(0.8f, 0.8f, 1f, 1f);
        drawCentered(font, "Space Runner", H * 0.60f);

        font.setColor(Color.LIGHT_GRAY);
        drawCentered(font, "A / D  or  \u2190 \u2192   move left / right", H * 0.46f);
        drawCentered(font, "W  or  \u2191  or  SPACE   jump",              H * 0.38f);
        drawCentered(font, "Jump on aliens to kill them  +10pts",          H * 0.28f);
        drawCentered(font, "Collect diamonds  +5pts",                      H * 0.20f);
        drawCentered(font, "You have 3 lives.  Don't lose them all!",      H * 0.13f);

        float blink = (float)(Math.sin(
                System.currentTimeMillis() * 0.003) * 0.5 + 0.5);
        font.setColor(1f, 1f, 0f, blink);
        drawCentered(font, "PRESS SPACE TO START", H * 0.05f);

        batch.end();
    }

    // ── HUD ────────────────────────────────────────────────────
    private void drawHUD(GameWorld world) {
        batch.begin();

        // Lives — draw small astronaut icons
        float iconSize = 18f;
        for (int i = 0; i < world.getLives(); i++) {
            batch.draw(lifeIcon,
                       8f + i * (iconSize + 4f),
                       H - iconSize - 4f,
                       iconSize, iconSize);
        }

        // Score (top right)
        font.setColor(Color.WHITE);
        String scoreStr = "SCORE  " + world.getScore();
        layout.setText(font, scoreStr);
        font.draw(batch, scoreStr,
                  W - layout.width - 8f, H - 4f);

        // Distance (top center)
        font.setColor(0.6f, 0.9f, 1f, 1f);
        String distStr = world.getDistance() + " m";
        layout.setText(font, distStr);
        font.draw(batch, distStr,
                  (W - layout.width) / 2f, H - 4f);

        batch.end();
    }

    // ── GAME OVER ──────────────────────────────────────────────
    private void drawGameOver(GameWorld world) {
        drawOverlay(0.82f);

        batch.begin();

        bigFont.setColor(1f, 0.2f, 0.3f, 1f);
        drawCentered(bigFont, "GAME OVER", H * 0.74f);

        font.setColor(Color.WHITE);
        drawCentered(font, "Score:       " + world.getScore(),    H * 0.54f);
        drawCentered(font, "Distance:    " + world.getDistance() + " m", H * 0.44f);

        font.setColor(0.3f, 1f, 0.8f, 1f);
        drawCentered(font, "Best:        " + world.getHighScore(), H * 0.34f);

        float blink = (float)(Math.sin(
                System.currentTimeMillis() * 0.003) * 0.5 + 0.5);
        font.setColor(1f, 1f, 0f, blink);
        drawCentered(font, "PRESS SPACE TO RETURN TO MENU", H * 0.14f);

        batch.end();
    }

    // ── Helpers ────────────────────────────────────────────────
    private void drawOverlay(float alpha) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, alpha);
        shapes.rect(0, 0, W, H);
        shapes.end();
    }

    private void drawCentered(BitmapFont f, String text, float y) {
        layout.setText(f, text);
        f.draw(batch, text, (W - layout.width) / 2f, y);
    }

    public void dispose() {
        font.dispose();
        bigFont.dispose();
        shapes.dispose();
        lifeSheet.dispose();
        gemTex.dispose();
    }
}