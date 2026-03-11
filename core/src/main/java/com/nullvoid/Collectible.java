package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Collectible {

    public static final float SIZE = 25f;

    public static final float GEM_Y_GROUND = Player.GROUND_Y + Player.SIZE * 0.45f;
    public static final float GEM_Y_ROCK   = Player.GROUND_Y + Rock.SIZE + 10f;
    public static final float ROCK_RADIUS  = 28f;

    private static Texture texture;

    private float   x, y;
    private float   bobTime   = 0f;
    private boolean collected = false;

    public static void loadAssets() {
        texture = new Texture("Diamond.png");
    }

    public static void disposeAssets() {
        if (texture != null) texture.dispose();
    }

    public Collectible(float x) {
        this.x = x;
        this.y = GEM_Y_GROUND;
    }

    // Called every frame — recalculates Y so gems always clear rocks
    public void update(float delta, float speed, Array<Rock> rocks) {
        x       -= speed * delta;
        bobTime += delta;

        // Check if any rock is directly beneath this gem right now
        float baseY = GEM_Y_GROUND;
        for (Rock r : rocks) {
            if (Math.abs(r.getX() - x) < ROCK_RADIUS) {
                baseY = GEM_Y_ROCK;
                break;
            }
        }

        y = baseY + (float)(Math.sin(bobTime * 4f) * 5f);
    }

    public void render(SpriteBatch batch) {
        if (!collected)
            batch.draw(texture, x - SIZE / 2f, y, SIZE, SIZE);
    }

    public float   getX()        { return x; }
    public float   getY()        { return y; }
    public boolean isCollected() { return collected; }
    public void    collect()     { collected = true; }
    public boolean isOffScreen() { return x < -SIZE; }

    public float hitX() { return x - SIZE * 0.45f; }
    public float hitY() { return y; }
    public float hitW() { return SIZE * 0.9f; }
    public float hitH() { return SIZE * 0.9f; }
}