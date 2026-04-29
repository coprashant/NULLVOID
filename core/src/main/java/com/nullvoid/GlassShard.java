package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GlassShard {

    public static final float SIZE     = 14f;
    private static final float SPEED_X = -190f;

    private static Texture       sheet;
    private static TextureRegion region;

    private float   x, y;
    private float   angle   = 0f;
    private boolean expired = false;

    public static void loadAssets() {
        sheet  = new Texture("GlassShards.png");
        region = new TextureRegion(sheet, 0, 0, sheet.getWidth(), sheet.getHeight());
    }

    public static void disposeAssets() {
        if (sheet != null) sheet.dispose();
    }

    public GlassShard(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float delta, float worldVel) {
        x     += (SPEED_X - worldVel) * delta;
        angle += 360f * delta;

        // Expires only when it leaves the screen horizontally
        if (x < -SIZE || x > NullVoid.W + SIZE)
            expired = true;
    }

    public void render(SpriteBatch batch) {
        batch.draw(region,
                   x - SIZE / 2f, y - SIZE / 2f,
                   SIZE / 2f, SIZE / 2f,
                   SIZE, SIZE,
                   1f, 1f,
                   angle,
                   false);
    }

    public boolean isExpired() { return expired; }
    public void    expire()    { expired = true; }

    public float hitX() { return x - SIZE * 0.4f; }
    public float hitY() { return y - SIZE * 0.4f; }
    public float hitW() { return SIZE * 0.8f; }
    public float hitH() { return SIZE * 0.8f; }
}