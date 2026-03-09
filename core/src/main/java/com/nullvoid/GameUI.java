package com.nullvoid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class GameUI {

    private static final float W = NullVoid.W;
    private static final float H = NullVoid.H;

    private static final Color COL_CYAN   = new Color(0.20f, 1.00f, 0.85f, 1f);
    private static final Color COL_PURPLE = new Color(0.65f, 0.20f, 1.00f, 1f);
    private static final Color COL_GOLD   = new Color(1.00f, 0.85f, 0.20f, 1f);
    private static final Color COL_DIM    = new Color(0.40f, 0.40f, 0.55f, 1f);
    private static final Color COL_RED    = new Color(1.00f, 0.18f, 0.28f, 1f);

    private static final int STAR_COUNT = 100;
    private final float[] starX     = new float[STAR_COUNT];
    private final float[] starY     = new float[STAR_COUNT];
    private final float[] starSize  = new float[STAR_COUNT];
    private final float[] starSpd   = new float[STAR_COUNT];
    private final float[] starAlpha = new float[STAR_COUNT];

    private float   time       = 0f;
    private float   blinkTimer = 0f;
    private boolean blinkOn    = true;

    private float scanPos     = 0f;
    private float glitchX     = 0f;
    private float glitchAlpha = 0.85f;

    float hudHintTimer = 3.5f;

    private static final float HUD_H  = PixelFont.DH + 4f;
    private static final float HUD_Y  = H - HUD_H;
    private static final float ICON_W = 16f;
    private static final float ICON_H = 16f;

    // Pause button — far top-left corner
    private static final float BTN_X   = 3f;
    private static final float BTN_W   = 14f;
    private static final float BTN_H   = 14f;
    private static final float BTN_PAD = 5f;
    private float pauseBtnY = 0f;

    // Mute button in pause screen
    private static final float MUTE_W = 60f;
    private static final float MUTE_H = 16f;
    private float muteBtnX = 0f;
    private float muteBtnY = 0f;

    private OrthographicCamera    lastCam  = null;
    private final Vector3         touchVec = new Vector3();
    private boolean pauseButtonPressed     = false;
    private boolean muteToggled            = false;

    private final SpriteBatch batch;
    private BitmapFont        fontSm, fontMd, fontLg, fontPopup, fontBanner;
    private final GlyphLayout layout = new GlyphLayout();
    private ShapeRenderer     shapes;

    private Texture       lifeSheet;
    private TextureRegion lifeIcon;

    public GameUI(SpriteBatch batch) { this.batch = batch; }

    // Lifecycle

    public void create() {
        fontSm     = new BitmapFont(); fontSm.getData().setScale(0.72f);
        fontMd     = new BitmapFont(); fontMd.getData().setScale(0.95f);
        fontLg     = new BitmapFont(); fontLg.getData().setScale(1.70f);
        fontPopup  = new BitmapFont(); fontPopup.getData().setScale(1.1f);
        fontBanner = new BitmapFont(); fontBanner.getData().setScale(2.0f);

        shapes    = new ShapeRenderer();
        lifeSheet = new Texture("SmallAstronaut_Idle.png");
        lifeIcon  = new TextureRegion(lifeSheet, 0, 0, 16, 16);

        PixelFont.loadAssets();
        initStars();
    }

    private void initStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, W);
            starY[i] = MathUtils.random(0f, H);
            float layer = MathUtils.random();
            if (layer < 0.55f) {
                starSize[i]  = MathUtils.random(0.4f, 0.8f);
                starSpd[i]   = MathUtils.random(3f, 7f);
                starAlpha[i] = MathUtils.random(0.25f, 0.5f);
            } else if (layer < 0.85f) {
                starSize[i]  = MathUtils.random(0.8f, 1.4f);
                starSpd[i]   = MathUtils.random(9f, 16f);
                starAlpha[i] = MathUtils.random(0.45f, 0.7f);
            } else {
                starSize[i]  = MathUtils.random(1.4f, 2.0f);
                starSpd[i]   = MathUtils.random(18f, 30f);
                starAlpha[i] = MathUtils.random(0.65f, 1.0f);
            }
        }
    }

    // Button queries

    public boolean wasPauseButtonPressed() {
        boolean v = pauseButtonPressed; pauseButtonPressed = false; return v;
    }

    public boolean wasMuteToggled() {
        boolean v = muteToggled; muteToggled = false; return v;
    }

    // Update

    private void update(float delta, NullVoid.State state, boolean muted) {
        time += delta;
        blinkTimer += delta;
        if (blinkTimer >= 0.55f) { blinkTimer = 0f; blinkOn = !blinkOn; }

        scanPos += delta * 450f;

        if (MathUtils.random() > 0.98f) {
            glitchX     = MathUtils.random(-3f, 3f);
            glitchAlpha = 0.3f;
        } else {
            glitchX     = MathUtils.lerp(glitchX, 0, 0.2f);
            glitchAlpha = MathUtils.lerp(glitchAlpha, 0.85f, 0.1f);
        }

        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] += starSpd[i] * delta;
            if (starX[i] > W + 2f) starX[i] = -2f;
            starAlpha[i] += MathUtils.sin(time * 1.8f + i) * delta * 0.2f;
            starAlpha[i]  = MathUtils.clamp(starAlpha[i], 0.08f, 1.0f);
        }

        if (lastCam == null || !Gdx.input.justTouched()) return;

        touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        lastCam.unproject(touchVec);

        if (state == NullVoid.State.PLAYING) {
            if (touchVec.x >= BTN_X     && touchVec.x <= BTN_X + BTN_W
             && touchVec.y >= pauseBtnY && touchVec.y <= pauseBtnY + BTN_H) {
                pauseButtonPressed = true;
            }
        }

        if (state == NullVoid.State.PAUSED) {
            if (touchVec.x >= muteBtnX && touchVec.x <= muteBtnX + MUTE_W
             && touchVec.y >= muteBtnY && touchVec.y <= muteBtnY + MUTE_H) {
                muteToggled = true;
            }
        }
    }

    // Public render

    public void render(NullVoid.State state, GameWorld world,
                       OrthographicCamera cam, AudioManager audio) {
        shapes.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);
        lastCam = cam;
        update(Gdx.graphics.getDeltaTime(), state, audio.isMuted());

        switch (state) {
            case MENU:      drawMenu(world);                    break;
            case PLAYING:   drawHUD(world);                     break;
            case PAUSED:    drawPaused(world, audio.isMuted()); break;
            case GAME_OVER: drawGameOver(world);                break;
        }
    }

    // MENU

    private void drawMenu(GameWorld world) {
        drawSpaceBackground();
        drawStarfield();
        drawScanlines();
        drawMenuChrome(340f, 125f, 85f);
        batch.begin();
        drawMenuText(world);
        batch.end();
    }

    private void drawSpaceBackground() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float stripH = H / 24f;
        for (int i = 0; i < 24; i++) {
            float t = (float) i / 24f;
            shapes.setColor(
                MathUtils.lerp(0.02f, 0.04f, t),
                MathUtils.lerp(0.02f, 0.01f, t),
                MathUtils.lerp(0.13f, 0.05f, t), 1f);
            shapes.rect(0, H - (i + 1) * stripH, W, stripH + 1f);
        }
        for (int r = 7; r >= 0; r--) {
            float a  = (r == 0) ? 0.06f : 0.014f * (8 - r);
            float rw = 55f + r * 9f, rh = 35f + r * 6f;
            shapes.setColor(0.35f, 0.05f, 0.65f, a);
            shapes.ellipse(W - rw * 0.6f, H - rh * 0.5f, rw, rh);
        }
        for (int r = 5; r >= 0; r--) {
            float a  = 0.02f * (6 - r);
            float rw = 45f + r * 8f, rh = 28f + r * 5f;
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

    private void drawMenuChrome(float bw, float bh, float by) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float bx        = (W - bw) / 2f + glitchX;
        float perimeter = (bw + bh) * 2f;
        float scan      = scanPos % perimeter;

        shapes.setColor(0.01f, 0.03f, 0.08f, glitchAlpha);
        shapes.rect(bx, by, bw, bh);
        shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.2f);
        shapes.rect(bx,      by,      bw, 1f);
        shapes.rect(bx,      by + bh, bw, 1f);
        shapes.rect(bx,      by,      1f, bh);
        shapes.rect(bx + bw, by,      1f, bh);

        shapes.setColor(Color.WHITE);
        float sLen = 40f;
        if (scan < bw) {
            shapes.rect(bx + scan, by, Math.min(sLen, bw - scan), 1.2f);
        } else if (scan < bw + bh) {
            float rel = scan - bw;
            shapes.rect(bx + bw, by + rel, 1.2f, Math.min(sLen, bh - rel));
        } else if (scan < bw * 2f + bh) {
            float rel = scan - (bw + bh);
            shapes.rect(bx + bw - rel - sLen, by + bh, sLen, 1.2f);
        } else {
            float rel = scan - (bw * 2f + bh);
            shapes.rect(bx, by + bh - rel - sLen, 1.2f, sLen);
        }
        shapes.end();
    }

    private void drawMenuText(GameWorld world) {
        float titleY = 245f + MathUtils.sin(time * 2f) * 2f;
        float glow   = 0.38f + 0.32f * MathUtils.sin(time * 2.8f);

        fontLg.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, glow * 0.4f);
        drawCentered(fontLg, "NULLVOID", titleY + 1);
        fontLg.setColor(1f, 1f, 1f, 1f);
        drawCentered(fontLg, "NULLVOID", titleY);

        fontSm.setColor(COL_DIM.r, COL_DIM.g, COL_DIM.b, 0.8f);
        drawCentered(fontSm, "- SPACE RUNNER -", 222f);

        float bx      = W / 2f + glitchX;
        float flicker = 0.72f + 0.28f * MathUtils.sin(time * 6f);
        fontSm.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, flicker);
        drawCenteredAt(fontSm, "[ MISSION BRIEFING ]", bx, 198f);

        float leftX = bx - 20f, rightX = bx + 20f, ctrlY = 180f;
        fontSm.setColor(0.55f, 0.88f, 1.00f, 0.92f);
        drawRightAligned(fontSm, "A / D  or  LEFT / RIGHT", leftX, ctrlY);
        fontSm.draw(batch, "Move",   rightX, ctrlY);
        drawRightAligned(fontSm, "W  or  UP  or  SPACE",   leftX, ctrlY - 14f);
        fontSm.draw(batch, "Jump",   rightX, ctrlY - 14f);
        drawRightAligned(fontSm, "SHIFT + move",            leftX, ctrlY - 28f);
        fontSm.draw(batch, "Sprint", rightX, ctrlY - 28f);

        fontSm.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, 0.92f);
        drawCenteredAt(fontSm, "Stomp aliens  +10 pts",            bx, 130f);
        fontSm.setColor(0.78f, 0.48f, 1.00f, 0.92f);
        drawCenteredAt(fontSm, "Collect diamonds  +5 pts",         bx, 117f);
        fontSm.setColor(0.95f, 0.32f, 0.32f, 0.92f);
        drawCenteredAt(fontSm, "3 lives  -  don't lose them all!", bx, 104f);

        fontSm.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.6f);
        drawCentered(fontSm, "BEST: " + world.getHighScore() + " m", 65f);

        if (blinkOn) {
            fontMd.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, 1f);
            drawCentered(fontMd, "PRESS SPACE TO START", 35f);
        }
    }

    // HUD

    private void drawHUD(GameWorld world) {
        if (hudHintTimer > 0f) hudHintTimer -= Gdx.graphics.getDeltaTime();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.18f);
        shapes.rect(0, HUD_Y - 1f, W, 1f);
        shapes.end();

        float iconY = HUD_Y + (HUD_H - ICON_H) / 2f;
        float numY  = HUD_Y + (HUD_H - PixelFont.DH) / 2f;
        pauseBtnY   = HUD_Y + (HUD_H - BTN_H) / 2f;

        // Pause button — far left corner
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.1f, 0.1f, 0.18f, 0.7f);
        shapes.rect(BTN_X, pauseBtnY, BTN_W, BTN_H);
        shapes.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 0.5f);
        shapes.rect(BTN_X,              pauseBtnY,               BTN_W, 1f);
        shapes.rect(BTN_X,              pauseBtnY + BTN_H - 1f,  BTN_W, 1f);
        shapes.rect(BTN_X,              pauseBtnY,               1f, BTN_H);
        shapes.rect(BTN_X + BTN_W - 1f, pauseBtnY,               1f, BTN_H);
        float barW = 2f, barH = BTN_H * 0.55f;
        float barY = pauseBtnY + (BTN_H - barH) / 2f;
        shapes.setColor(1f, 1f, 1f, 0.9f);
        shapes.rect(BTN_X + 3f,           barY, barW, barH);
        shapes.rect(BTN_X + BTN_W - 5f,   barY, barW, barH);
        shapes.end();

        batch.begin();

        // Lives — right of pause button
        float livesStartX = BTN_X + BTN_W + BTN_PAD;
        for (int i = 0; i < Player.MAX_LIVES; i++) {
            boolean alive = i < world.getLives();
            batch.setColor(alive ? 1f : 0.35f, alive ? 1f : 0.35f,
                           alive ? 1f : 0.45f, alive ? 1f : 0.5f);
            batch.draw(lifeIcon, livesStartX + i * (ICON_W + 3f), iconY, ICON_W, ICON_H);
        }
        batch.setColor(1f, 1f, 1f, 1f);

        // Centre — distance
        int   dist  = world.getDistance();
        float distW = PixelFont.measureInt(dist) + PixelFont.DW * 2f + 6f;
        float cx    = (W - distW) / 2f;
        batch.setColor(0.55f, 0.92f, 1f, 1f);
        cx = PixelFont.drawInt(batch, dist, cx, numY);
        cx += 3f;
        cx = PixelFont.drawHyphen(batch, cx, numY);
        cx += 3f;
        fontMd.setColor(0.55f, 0.92f, 1f, 1f);
        fontMd.draw(batch, "m", cx, numY + PixelFont.DH - 1f);
        batch.setColor(1f, 1f, 1f, 1f);

        // Right — gem icon x score
        int   score  = world.getScore();
        float scoreW = PixelFont.ICON_SIZE + 2f + PixelFont.DW
                     + PixelFont.measureInt(score) + 4f;
        float rx = W - scoreW - 5f;
        rx = PixelFont.drawGemIcon(batch, rx, numY);
        rx = PixelFont.drawTimes(batch, rx, numY);
        rx += 2f;
        PixelFont.drawInt(batch, score, rx, numY);

        // Popups
        for (ScorePopup p : world.getPopups()) {
            if (!p.isActive()) continue;
            fontPopup.setColor(p.r, p.g, p.b, p.getAlpha());
            layout.setText(fontPopup, p.getText());
            fontPopup.draw(batch, p.getText(), p.getX() - layout.width / 2f, p.getY());
        }
        fontPopup.setColor(1f, 1f, 1f, 1f);

        // Pause hint
        if (hudHintTimer > 0f) {
            float alpha = MathUtils.clamp(hudHintTimer / 1.5f, 0f, 0.55f);
            fontSm.setColor(0.5f, 0.5f, 0.6f, alpha);
            String hint = "ESC / P  pause";
            layout.setText(fontSm, hint);
            fontSm.draw(batch, hint, W - layout.width - 6f, 10f);
        }

        batch.end();

        // Milestone banner — drawn on top of everything
        drawMilestoneBanner(world.getMilestones());
    }

    // Milestone banner

    private void drawMilestoneBanner(MilestoneManager ms) {
        if (!ms.isBannerActive()) return;

        float progress = ms.getBannerTimer() / ms.getBannerMax();

        // Fade in first 15%, hold, fade out last 25%
        float alpha;
        if (progress > 0.85f)      alpha = (1f - progress) / 0.15f;
        else if (progress < 0.25f) alpha = progress / 0.25f;
        else                       alpha = 1f;

        // Slight scale pulse on entry
        float scale = 1f + MathUtils.clamp((progress - 0.85f) / 0.15f, 0f, 1f) * 0.15f;

        String text = ms.getBannerText();
        layout.setText(fontBanner, text);
        float tw = layout.width * scale;
        float th = layout.height * scale;
        float bx = (W - tw) / 2f;
        float by = H / 2f - th / 2f + 10f;

        // Dark backing strip
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float pad = 12f;
        shapes.setColor(0f, 0f, 0f, alpha * 0.55f);
        shapes.rect(bx - pad, by - pad * 0.5f, tw + pad * 2f, th + pad);
        // Gold top/bottom border lines
        shapes.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, alpha * 0.8f);
        shapes.rect(bx - pad, by + th + pad * 0.5f, tw + pad * 2f, 1.5f);
        shapes.rect(bx - pad, by - pad * 0.5f,      tw + pad * 2f, 1.5f);
        shapes.end();

        batch.begin();
        // Gold glow pass
        fontBanner.getData().setScale(2.0f * scale);
        fontBanner.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, alpha * 0.35f);
        fontBanner.draw(batch, text, bx + 1f, by + th - 1f);
        // Main white text
        fontBanner.setColor(1f, 1f, 1f, alpha);
        fontBanner.draw(batch, text, bx, by + th);
        fontBanner.getData().setScale(2.0f);
        batch.end();
    }

    // PAUSE

    private void drawPaused(GameWorld world, boolean muted) {
        drawOverlay(0.60f);
        float bw = 220f, bh = 145f, by = (H - bh) / 2f;
        drawMenuChrome(bw, bh, by);

        batch.begin();
        float bx     = W / 2f + glitchX;
        float titleY = by + bh - 15f;

        fontMd.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 1f);
        drawCenteredAt(fontMd, ":: SYSTEM PAUSED ::", bx, titleY);
        fontSm.setColor(COL_DIM.r, COL_DIM.g, COL_DIM.b, 0.8f);
        drawCenteredAt(fontSm, "-------------------", bx, titleY - 12f);
        fontSm.setColor(Color.WHITE);
        drawCenteredAt(fontSm, "CURRENT SCORE: " + world.getScore(),     bx, titleY - 35f);
        drawCenteredAt(fontSm, "DISTANCE: " + world.getDistance() + "m", bx, titleY - 50f);
        fontSm.setColor(COL_PURPLE.r, COL_PURPLE.g, COL_PURPLE.b, 0.9f);
        drawCenteredAt(fontSm, "LIVES: " + world.getLives() + " / " + Player.MAX_LIVES,
                       bx, titleY - 70f);

        // Mute button
        String muteLabel = muted ? "[ SOUND: OFF ]" : "[ SOUND: ON  ]";
        layout.setText(fontSm, muteLabel);
        muteBtnX = bx - layout.width / 2f - 4f;
        muteBtnY = by + 26f;
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(muted ? 0.3f : 0.05f,
                        muted ? 0.05f : 0.25f,
                        muted ? 0.05f : 0.35f, 0.85f);
        shapes.rect(muteBtnX, muteBtnY, layout.width + 8f, MUTE_H);
        shapes.setColor(muted ? COL_RED.r : COL_CYAN.r,
                        muted ? COL_RED.g : COL_CYAN.g,
                        muted ? COL_RED.b : COL_CYAN.b, 0.6f);
        shapes.rect(muteBtnX,                     muteBtnY,          layout.width + 8f, 1f);
        shapes.rect(muteBtnX,                     muteBtnY + MUTE_H, layout.width + 8f, 1f);
        shapes.rect(muteBtnX,                     muteBtnY,          1f, MUTE_H);
        shapes.rect(muteBtnX + layout.width + 7f, muteBtnY,          1f, MUTE_H);
        shapes.end();

        batch.begin();
        fontSm.setColor(muted ? COL_RED.r : COL_CYAN.r,
                        muted ? COL_RED.g : COL_CYAN.g,
                        muted ? COL_RED.b : COL_CYAN.b, 1f);
        fontSm.draw(batch, muteLabel, muteBtnX + 4f, muteBtnY + MUTE_H - 3f);

        if (blinkOn) {
            fontSm.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, 1f);
            drawCenteredAt(fontSm, "[ PRESS SPACE TO RESUME ]", bx, by + 12f);
        }
        batch.end();
    }

    // GAME OVER

    private void drawGameOver(GameWorld world) {
        drawSpaceBackground();
        drawStarfield();
        drawScanlines();
        drawOverlay(0.45f);

        float bw = 300f, bh = 160f, by = 75f;
        drawMenuChrome(bw, bh, by);

        batch.begin();
        float bx      = W / 2f + glitchX;
        float flicker = 0.8f + 0.2f * MathUtils.sin(time * 10f);

        fontLg.setColor(COL_RED.r, COL_RED.g, COL_RED.b, flicker);
        drawCenteredAt(fontLg, "GAME OVER!!", bx, by + bh - 20f);
        fontSm.setColor(COL_DIM.r, COL_DIM.g, COL_DIM.b, 0.8f);
        drawCenteredAt(fontSm, "!! ALL LIVES ENDED !!", bx, by + bh - 45f);
        fontMd.setColor(Color.WHITE);
        drawCenteredAt(fontMd, "FINAL SCORE: " + world.getScore(),       bx, by + 75f);
        drawCenteredAt(fontMd, "DISTANCE: " + world.getDistance() + "m", bx, by + 60f);
        fontMd.setColor(COL_CYAN.r, COL_CYAN.g, COL_CYAN.b, 1f);
        drawCenteredAt(fontMd, "PERSONAL BEST: " + world.getHighScore(), bx, by + 40f);
        if (blinkOn) {
            fontMd.setColor(COL_GOLD.r, COL_GOLD.g, COL_GOLD.b, 1f);
            drawCenteredAt(fontMd, "PRESS SPACE TO RESTART", bx, 40f);
        }
        batch.end();
    }

    // Helpers

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
        f.draw(batch, text, x - layout.width / 2f, y);
    }

    private void drawRightAligned(BitmapFont f, String text, float x, float y) {
        layout.setText(f, text);
        f.draw(batch, text, x - layout.width, y);
    }

    public void dispose() {
        fontSm.dispose();
        fontMd.dispose();
        fontLg.dispose();
        fontPopup.dispose();
        fontBanner.dispose();
        shapes.dispose();
        lifeSheet.dispose();
        PixelFont.disposeAssets();
    }
}