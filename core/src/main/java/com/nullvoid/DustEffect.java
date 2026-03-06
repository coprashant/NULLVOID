package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * DustEffect — plays once at a given position then disappears.
 * Used for landing, stomping aliens, collecting diamonds.
 * Dust.png = 256x32 → 8 frames of 32x32.
 */
public class DustEffect {

    private static Texture                  sheet;
    private static Animation<TextureRegion> anim;

    private float   x, y, stateTime = 0f;
    private boolean active = false;

    public static void loadAssets() {
        sheet = new Texture("Dust.png");
        TextureRegion[][] g = TextureRegion.split(sheet, 32, 32);
        TextureRegion[] frames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) frames[i] = g[0][i];
        anim = new Animation<>(0.07f, frames);
        anim.setPlayMode(Animation.PlayMode.NORMAL);
    }

    public static void disposeAssets() {
        if (sheet != null) sheet.dispose();
    }

    public void play(float x, float y) {
        this.x = x; this.y = y;
        stateTime = 0f; active = true;
    }

    public void update(float delta) {
        if (!active) return;
        stateTime += delta;
        if (anim.isAnimationFinished(stateTime)) active = false;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        TextureRegion f = anim.getKeyFrame(stateTime);
        batch.draw(f, x - 24f, y, 48f, 48f);
    }

    public boolean isActive() { return active; }
}