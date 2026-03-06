package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * CeilingGap — a low-hanging ceiling section the player must NOT jump into.
 * Player needs to stay on the ground and pass underneath.
 *
 * Uses ceiling/wall tiles from RunnerTileSet row 0 (top cave tiles).
 */
public class CeilingGap {

    public static final float TILE     = 32f;
    public static final float CEILING_Y = NullVoid.H - TILE * 2f;
    public static final float GAP_WIDTH = 96f;  // width of the low section

    private float x;
    private boolean passed = false;

    private static Texture       tileSheet;
    private static TextureRegion ceilTile;

    public static void loadAssets(Texture sheet) {
        tileSheet = sheet;
        TextureRegion[][] grid = TextureRegion.split(sheet, 24, 24);
        ceilTile = grid[5][0];   // dark floor tile used as ceiling
    }

    public CeilingGap(float x) {
        this.x = x;
    }

    public void update(float delta, float speed) {
        x -= speed * delta;
    }

    public void render(SpriteBatch batch) {
        // Draw a row of ceiling tiles hanging down
        int numTiles = (int)(GAP_WIDTH / TILE) + 1;
        for (int i = 0; i < numTiles; i++) {
            batch.draw(ceilTile,
                       x + i * TILE,
                       CEILING_Y,
                       TILE, TILE * 3f);  // tall hanging section
        }
    }

    public float   getX()        { return x; }
    public boolean isPassed()    { return passed; }
    public void    markPassed()  { passed = true; }
    public boolean isOffScreen() { return x + GAP_WIDTH < 0; }

    // Hitbox — the dangerous low area
    public float hitX() { return x; }
    public float hitY() { return CEILING_Y - 4f; }
    public float hitW() { return GAP_WIDTH; }
    public float hitH() { return NullVoid.H - CEILING_Y; }
}