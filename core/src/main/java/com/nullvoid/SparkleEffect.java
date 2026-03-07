package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SparkleEffect {

    private static final int   FRAME_COUNT = 8;
    private static final int   FRAME_W     = 16;
    private static final int   FRAME_H     = 16;
    private static final float FRAME_DUR   = 0.06f;  
    private static final float DRAW_SIZE   = 15f;   

    private static Texture                  sheet;
    private static Animation<TextureRegion> anim;

    private float   x, y;
    private float   stateTime = 0f;
    private boolean active    = false;

    // ── Asset loading ──────────────────────────────────────────

    public static void loadAssets() {
        sheet = new Texture("sparkle-effect.png");
        TextureRegion[][] g = TextureRegion.split(sheet, FRAME_W, FRAME_H);
        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) frames[i] = g[0][i];
        anim = new Animation<>(FRAME_DUR, frames);
        anim.setPlayMode(Animation.PlayMode.NORMAL);
    }

    public static void disposeAssets() {
        if (sheet != null) sheet.dispose();
    }

    // ── Playback ───────────────────────────────────────────────

    public void play(float x, float y) {
        this.x    = x;
        this.y    = y;
        stateTime = 0f;
        active    = true;
    }

    public void update(float delta) {
        if (!active) return;
        stateTime += delta;
        if (anim.isAnimationFinished(stateTime)) active = false;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        // Tint cyan-gold to contrast against the blue cave background
        batch.setColor(1f, 0.95f, 0.4f, 1f);
        TextureRegion f = anim.getKeyFrame(stateTime);
        batch.draw(f,
                   x - DRAW_SIZE / 2f,
                   y - DRAW_SIZE / 2f,
                   DRAW_SIZE, DRAW_SIZE);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public boolean isActive() { return active; }
}