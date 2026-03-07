package com.nullvoid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class GameUI {

    private static final float W = NullVoid.W; 
    private static final float H = NullVoid.H; 

    private static final Color COL_CYAN   = new Color(0.20f, 1.00f, 0.85f, 1f);
    private static final Color COL_PURPLE = new Color(0.65f, 0.20f, 1.00f, 1f);
    private static final Color COL_GOLD   = new Color(1.00f, 0.85f, 0.20f, 1f);
    private static final Color COL_DIM    = new Color(0.40f, 0.40f, 0.55f, 1f);

    private static final int STAR_COUNT = 100;
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] starSpd = new float[STAR_COUNT];
    private final float[] starAlpha = new float[STAR_COUNT];

    private float time = 0f;
    private float blinkTimer = 0f;
    private boolean blinkOn = true;
    
    private float scanPos = 0f;
    private float glitchX = 0f;
    private float glitchAlpha = 0.85f;

    private final SpriteBatch batch;
    private BitmapFont fontSm, fontMd, fontLg;
    private final GlyphLayout layout = new GlyphLayout();
    private ShapeRenderer shapes;

    private Texture lifeSheet;
    private TextureRegion lifeIcon;
    private Texture gemTex;

    public GameUI(SpriteBatch batch) { this.batch = batch; }

    public void create() {
        fontSm = new BitmapFont(); fontSm.getData().setScale(0.72f);
        fontMd = new BitmapFont(); fontMd.getData().setScale(0.95f);
        fontLg = new BitmapFont(); fontLg.getData().setScale(1.70f);

        shapes = new ShapeRenderer();
        lifeSheet = new Texture("SmallAstronaut_Idle.png");
        lifeIcon = new TextureRegion(lifeSheet, 0, 0, 16, 16);
        gemTex = new Texture("Diamond.png");

        initStars();
    }

    private void initStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, W);
            starY[i] = MathUtils.random(0f, H);
            float layer = MathUtils.random();
            if (layer < 0.55f) {
                starSize[i] = MathUtils.random(0.4f, 0.8f);
                starSpd[i] = MathUtils.random(3f, 7f);
                starAlpha[i] = MathUtils.random(0.25f, 0.5f);
            } else if (layer < 0.85f) {
                starSize[i] = MathUtils.random(0.8f, 1.4f);
                starSpd[i] = MathUtils.random(9f, 16f);
                starAlpha[i] = MathUtils.random(0.45f, 0.7f);
            } else {
                starSize[i] = MathUtils.random(1.4f, 2.0f);
                starSpd[i] = MathUtils.random(18f, 30f);
                starAlpha[i] = MathUtils.random(0.65f, 1.0f);
            }
        }
    }

    private void update(float delta) {
        time += delta;
        blinkTimer += delta;
        if (blinkTimer >= 0.55f) { blinkTimer = 0f; blinkOn = !blinkOn; }

        scanPos += delta * 450f;
        if (scanPos > (340f + 125f) * 2f) scanPos = 0;

        if (MathUtils.random() > 0.98f) {
            glitchX = MathUtils.random(-3f, 3f);
            glitchAlpha = 0.3f;
        } else {
            glitchX = MathUtils.lerp(glitchX, 0, 0.2f);
            glitchAlpha = MathUtils.lerp(glitchAlpha, 0.85f, 0.1f);
        }

        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] += starSpd[i] * delta;
            if (starX[i] > W + 2f) starX[i] = -2f;
            starAlpha[i] += MathUtils.sin(time * 1.8f + i) * delta * 0.2f;
            starAlpha[i] = MathUtils.clamp(starAlpha[i], 0.08f, 1.0f);
        }
    }

    public void render(NullVoid.State state, GameWorld world, OrthographicCamera cam) {
        shapes.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);
        update(com.badlogic.gdx.Gdx.graphics.getDeltaTime());

        switch (state) {
            case MENU: drawMenu(world); break;
            case PLAYING: drawHUD(world); break;
            case GAME_OVER: drawGameOver(world); break;
        }
    }

    private void drawMenu(GameWorld world) {
        drawSpaceBackground();
        drawStarfield();
        drawScanlines();
        drawMenuChrome();

        batch.begin();
        drawMenuText(world);
        batch.end();
    }

    private void drawSpaceBackground() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        
        // Gradient Background
        float stripH = H / 24f;
        for (int i = 0; i < 24; i++) {
            float t = (float) i / 24f;
            shapes.setColor(
                MathUtils.lerp(0.02f, 0.04f, t),
                MathUtils.lerp(0.02f, 0.01f, t),
                MathUtils.lerp(0.13f, 0.05f, t), 1f);
            shapes.rect(0, H - (i + 1) * stripH, W, stripH + 1f);
        }

        // Purple Nebula (Top Right)
        for (int r = 7; r >= 0; r--) {
            float a = (r == 0) ? 0.06f : 0.014f * (8 - r);
            float rw = 55f + r * 9f;
            float rh = 35f + r * 6f;
            shapes.setColor(0.35f, 0.05f, 0.65f, a);
            shapes.ellipse(W - rw * 0.6f, H - rh * 0.5f, rw, rh);
        }

        // Cyan Nebula (Bottom Left)
        for (int r = 5; r >= 0; r--) {
            float a = 0.02f * (6 - r);
            float rw = 45f + r * 8f;
            float rh = 28f + r * 5f;
            shapes.setColor(0.0f, 0.45f, 0.55f, a);
            shapes.ellipse(10f, 45f - rh * 0.4f, rw, rh);
        }
        
        shapes.end();
    }

    private void drawStarfield() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < STAR_COUNT; i++) {
            float tint = (i % 6 == 0) ? 0.85f : 1.0f;
            shapes.setColor(tint, tint, 1.0f, MathUtils.clamp(starAlpha[i], 0f, 1f));
            shapes.rect(starX[i], starY[i], starSize[i], starSize[i]);
        }
        shapes.end();
    }

    private void drawScanlines() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.09f);
        for (float y = 0; y < H; y += 3f) shapes.rect(0, y, W, 1f);
        shapes.end();
    }

    private void drawMenuChrome() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        float bw = 340f, bh = 125f;
        float bx = (W - bw) / 2f + glitchX;
        float by = 85f;

        shapes.setColor(0.01f, 0.03f, 0.08f, glitchAlpha);
        shapes.rect(bx, by, bw, bh);

        shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.2f);
        shapes.rect(bx, by, bw, 1f);
        shapes.rect(bx, by + bh, bw, 1f);
        shapes.rect(bx, by, 1f, bh);
        shapes.rect(bx + bw, by, 1f, bh);

        shapes.setColor(Color.WHITE);
        float sLen = 40f; 
        if (scanPos < bw) {
            shapes.rect(bx + scanPos, by, Math.min(sLen, bw - scanPos), 1.2f);
        } else if (scanPos < bw + bh) {
            float rel = scanPos - bw;
            shapes.rect(bx + bw, by + rel, 1.2f, Math.min(sLen, bh - rel));
        } else if (scanPos < bw * 2 + bh) {
            float rel = scanPos - (bw + bh);
            shapes.rect(bx + bw - rel - sLen, by + bh, sLen, 1.2f);
        } else {
            float rel = scanPos - (bw * 2 + bh);
            shapes.rect(bx, by + bh - rel - sLen, 1.2f, sLen);
        }

        shapes.end();
    }

    private void drawMenuText(GameWorld world) {
        float titleY = 245f + MathUtils.sin(time * 2f) * 2f;
        float glow = 0.38f + 0.32f * MathUtils.sin(time * 2.8f);
        
        fontLg.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, glow * 0.4f);
        drawCentered(fontLg, "NULLVOID", titleY + 1);
        fontLg.setColor(1f, 1f, 1f, 1f);
        drawCentered(fontLg, "NULLVOID", titleY);

        fontSm.setColor(COL_DIM.r, COL_DIM.g, COL_DIM.b, 0.8f);
        drawCentered(fontSm, "— SPACE RUNNER —", 222f);

        float bx = W / 2f + glitchX;
        float flicker = 0.72f + 0.28f * MathUtils.sin(time * 6f);
        fontSm.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, flicker);
        drawCenteredAt(fontSm, "[ MISSION BRIEFING ]", bx, 198f);

        float leftX = bx - 20f;
        float rightX = bx + 20f;
        float ctrlY = 180f;

        fontSm.setColor(0.55f, 0.88f, 1.00f, 0.92f);
        drawRightAligned(fontSm, "A / D  or  LEFT / RIGHT", leftX, ctrlY);
        fontSm.draw(batch, "Move", rightX, ctrlY);
        
        drawRightAligned(fontSm, "W  or  UP  or  SPACE", leftX, ctrlY - 14f);
        fontSm.draw(batch, "Jump", rightX, ctrlY - 14f);
        
        drawRightAligned(fontSm, "SHIFT + move", leftX, ctrlY - 28f);
        fontSm.draw(batch, "Sprint", rightX, ctrlY - 28f);

        fontSm.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, 0.92f);
        drawCenteredAt(fontSm, "Stomp aliens  +10 pts", bx, 130f);
        fontSm.setColor(0.78f, 0.48f, 1.00f, 0.92f);
        drawCenteredAt(fontSm, "Collect diamonds  +5 pts", bx, 117f);
        fontSm.setColor(0.95f, 0.32f, 0.32f, 0.92f);
        drawCenteredAt(fontSm, "3 lives  -  don't lose them all!", bx, 104f);

        fontSm.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.6f);
        drawCentered(fontSm, "BEST DISTANCE: " + world.getHighScore() + "m", 65f);

        if (blinkOn) {
            fontMd.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, 1f);
            drawCentered(fontMd, "PRESS SPACE TO START", 35f);
        }
    }

    private void drawHUD(GameWorld world) {
        batch.begin();
        float iconSize = 18f;
        for (int i = 0; i < world.getLives(); i++) {
            batch.draw(lifeIcon, 8f + i * (iconSize + 4f), H - iconSize - 4f, iconSize, iconSize);
        }
        fontMd.setColor(Color.WHITE);
        String scoreStr = "SCORE " + world.getScore();
        layout.setText(fontMd, scoreStr);
        fontMd.draw(batch, scoreStr, W - layout.width - 8f, H - 4f);
        fontMd.setColor(0.6f, 0.9f, 1f, 1f);
        String distStr = world.getDistance() + " m";
        layout.setText(fontMd, distStr);
        fontMd.draw(batch, distStr, (W - layout.width) / 2f, H - 4f);
        batch.end();
    }

    private void drawGameOver(GameWorld world) {
        drawStarfield();
        drawScanlines();
        drawOverlay(0.80f);
        batch.begin();
        fontLg.setColor(1f, 0.18f, 0.28f, 1f);
        drawCentered(fontLg, "GAME OVER", 210f);
        fontMd.setColor(Color.WHITE);
        drawCentered(fontMd, "Score      " + world.getScore(), 170f);
        drawCentered(fontMd, "Distance   " + world.getDistance() + " m", 152f);
        fontMd.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 1f);
        drawCentered(fontMd, "Best       " + world.getHighScore(), 134f);
        if (blinkOn) {
            fontMd.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, 1f);
            drawCentered(fontMd, "PRESS SPACE TO RETURN", 35f);
        }
        batch.end();
    }

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

    private void drawCenteredAt(BitmapFont f, String text, float x, float y) {
        layout.setText(f, text);
        f.draw(batch, text, x - (layout.width / 2f), y);
    }

    private void drawRightAligned(BitmapFont f, String text, float x, float y) {
        layout.setText(f, text);
        f.draw(batch, text, x - layout.width, y);
    }

    public void dispose() {
        fontSm.dispose(); fontMd.dispose(); fontLg.dispose();
        shapes.dispose(); lifeSheet.dispose(); gemTex.dispose();
    }
}