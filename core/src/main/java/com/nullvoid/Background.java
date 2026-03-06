package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Background {

    private static final float W    = NullVoid.W;
    private static final float H    = NullVoid.H;
    private static final float TILE = 24f;
    private static final float GROUND_TOP = Player.GROUND_Y;

    private Texture       tileSheet;
    private TextureRegion bgTile, midTile, floorTile,
                          ceilTile, groundFillTile;
    private ShapeRenderer shapes;

    private float offsetFar  = 0f;
    private float offsetMid  = 0f;
    private float offsetNear = 0f;

    private float glitchTimer = 3f;
    private float glitchAlpha = 0f;

    public Texture getTileSheet() { return tileSheet; }

    public void create() {
        tileSheet = new Texture("RunnerTileSet.png");
        TextureRegion[][] grid = TextureRegion.split(tileSheet, 24, 24);

        bgTile         = grid[5][0];
        midTile        = grid[6][0];
        floorTile      = grid[7][0];
        ceilTile       = grid[5][1];
        groundFillTile = grid[5][0];

        shapes = new ShapeRenderer();
    }

    public void update(float delta, float speed) {
        // Accumulate raw — no modulo here, applied per tile in draw
        offsetFar  += speed * 0.25f * delta;
        offsetMid  += speed * 0.55f * delta;
        offsetNear += speed * 1.00f * delta;

        glitchTimer -= delta;
        if (glitchTimer <= 0f) {
            glitchTimer = 3f + (float)(Math.random() * 5f);
            glitchAlpha = 0.10f;
        }
        if (glitchAlpha > 0f) glitchAlpha -= delta * 0.4f;
    }

    public void render(SpriteBatch batch) {

        // 1. Far background fill
        batch.setColor(0.25f, 0.25f, 0.35f, 1f);
        drawTileRect(batch, bgTile, offsetFar,
                     0, W, GROUND_TOP, H);
        batch.setColor(1f, 1f, 1f, 1f);

        // 2. Mid cave wall details
        batch.setColor(0.55f, 0.55f, 0.65f, 1f);
        drawTileRect(batch, midTile, offsetMid,
                     0, W,
                     GROUND_TOP + H * 0.35f,
                     GROUND_TOP + H * 0.35f + TILE * 3);
        batch.setColor(1f, 1f, 1f, 1f);

        // 3. Solid ground block (Y=0 to GROUND_Y)
        batch.setColor(0.6f, 0.55f, 0.5f, 1f);
        drawTileRect(batch, groundFillTile, offsetNear,
                     0, W, 0, GROUND_TOP);
        batch.setColor(1f, 1f, 1f, 1f);

        // 4. Floor surface tiles
        drawTileStrip(batch, floorTile, offsetNear,
                      GROUND_TOP, TILE);

        // 5. Ceiling strip
        drawTileStrip(batch, ceilTile, offsetNear,
                      H - TILE, TILE);

        // 6. Cyan ground glow
        batch.setColor(0.3f, 1f, 0.85f, 0.35f);
        drawTileStrip(batch, floorTile, offsetNear,
                      GROUND_TOP, 4f);
        batch.setColor(1f, 1f, 1f, 1f);

        // 7. Glitch overlay
        if (glitchAlpha > 0f) {
            batch.setColor(0.5f, 0f, 1f, glitchAlpha);
            batch.draw(bgTile, 0, 0, W, H);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private void drawTileRect(SpriteBatch batch, TextureRegion tile,
                               float offset,
                               float x1, float x2,
                               float y1, float y2) {
        float tileOffset = offset % TILE;
        int cols = (int)((x2 - x1) / TILE) + 2;
        int rows = (int)((y2 - y1) / TILE) + 2;
        for (int row = 0; row < rows; row++) {
            for (int col = -1; col < cols; col++) {
                float tx = x1 + col * TILE - tileOffset;
                float ty = y1 + row * TILE;
                if (tx + TILE < x1 || tx > x2) continue;
                batch.draw(tile, tx, ty, TILE, TILE);
            }
        }
    }

    private void drawTileStrip(SpriteBatch batch, TextureRegion tile,
                                float offset, float yPos, float tileH) {
        float tileOffset = offset % TILE;
        int cols = (int)(W / TILE) + 2;
        for (int c = -1; c < cols; c++) {
            batch.draw(tile,
                       c * TILE - tileOffset,
                       yPos, TILE, tileH);
        }
    }

    public void dispose() {
        tileSheet.dispose();
        shapes.dispose();
    }
}