package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


public class CeilingGap {

    public static final float TILE      = 32f;
    public static final float GAP_WIDTH = 96f;

    public static final float HANG_BOTTOM = 110f;

    private float   x;
    private boolean passed = false;

    private static Texture       tileSheet;
    private static TextureRegion ceilTile;

    public static void loadAssets(Texture sheet) {
        tileSheet = sheet;
        TextureRegion[][] grid = TextureRegion.split(sheet, 24, 24);
        ceilTile = grid[5][0];
    }

    public CeilingGap(float x) {
        this.x = x;
    }

    public void update(float delta, float speed) {
        x -= speed * delta;
    }

    public void render(SpriteBatch batch) {
        float hangHeight = NullVoid.H - HANG_BOTTOM;
        int numTiles = (int)(GAP_WIDTH / TILE) + 1;
        for (int i = 0; i < numTiles; i++) {
            batch.draw(ceilTile,
                       x + i * TILE,
                       HANG_BOTTOM,
                       TILE, hangHeight);
        }
    }

    public float   getX()       { return x; }
    public boolean isPassed()   { return passed; }
    public void    markPassed() { passed = true; }
    public boolean isOffScreen(){ return x + GAP_WIDTH < 0; }

    public float hitX() { return x; }
    public float hitY() { return HANG_BOTTOM; }
    public float hitW() { return GAP_WIDTH; }
    public float hitH() { return NullVoid.H - HANG_BOTTOM; }
}