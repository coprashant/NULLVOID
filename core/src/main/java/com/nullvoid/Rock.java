package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Rock — static obstacle on the ground. Player must jump over.
 *
 * Uses rock/debris tiles from the RunnerTileSet.
 * From the grid sheet, row 3 col 9-13 has rock debris sprites.
 */
public class Rock {

    public static final float SIZE = 32f;

    private float x;
    private boolean passed = false;

    private static Texture       tileSheet;
    private static TextureRegion rockRegion;

    public static void loadAssets() {
        tileSheet = new Texture("RunnerTileSet.png");
        // Row 3, col 9 = rock debris (from grid sheet analysis)
        TextureRegion[][] grid = TextureRegion.split(tileSheet, 24, 24);
        rockRegion = grid[3][10];
    }

    public static void disposeAssets() {
        if (tileSheet != null) tileSheet.dispose();
    }

    public Rock(float x) {
        this.x = x;
    }

    public void update(float delta, float speed) {
        x -= speed * delta;
    }

    public void render(SpriteBatch batch) {
        batch.draw(rockRegion, x - SIZE / 2f,
                   Player.GROUND_Y, SIZE, SIZE);
    }

    public float   getX()        { return x; }
    public boolean isPassed()    { return passed; }
    public void    markPassed()  { passed = true; }
    public boolean isOffScreen() { return x < -SIZE; }

    // Hitbox
    public float hitX() { return x - SIZE * 0.35f; }
    public float hitY() { return Player.GROUND_Y; }
    public float hitW() { return SIZE * 0.7f; }
    public float hitH() { return SIZE * 0.85f; }
}