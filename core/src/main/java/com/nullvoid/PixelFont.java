package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// Sprite-based number/symbol renderer using the pixel-art HUD sheets.
// Numbers.png 80x16 — 10 frames of 8x16, order: 1 2 3 4 5 6 7 8 9 0
// Hyphen.png  8x16  — single dash symbol
// Times.png   8x16  — single x symbol
// GemCounter  32x32 — gem icon
public class PixelFont {

    public static final float CHAR_W = 8f;
    public static final float CHAR_H = 16f;
    public static final float SCALE  = 1f;   // native size
    public static final float DW     = CHAR_W * SCALE;
    public static final float DH     = CHAR_H * SCALE;

    public static final float ICON_SIZE = 12f;

    private static Texture         numbersTex, hyphenTex, timesTex, gemIconTex;
    private static TextureRegion[] digits = new TextureRegion[10];
    private static TextureRegion   hyphen, times, gemIcon;

    public static void loadAssets() {
        numbersTex = new Texture("Numbers.png");
        hyphenTex  = new Texture("Hyphen.png");
        timesTex   = new Texture("Times.png");
        gemIconTex = new Texture("GemCounter.png");

        TextureRegion[][] grid = TextureRegion.split(numbersTex, 8, 16);
        for (int i = 0; i < 10; i++) digits[i] = grid[0][i];

        hyphen  = new TextureRegion(hyphenTex);
        times   = new TextureRegion(timesTex);
        gemIcon = new TextureRegion(gemIconTex);
    }

    public static void disposeAssets() {
        if (numbersTex != null) numbersTex.dispose();
        if (hyphenTex  != null) hyphenTex.dispose();
        if (timesTex   != null) timesTex.dispose();
        if (gemIconTex != null) gemIconTex.dispose();
    }

    // Digit order in sheet is 1-9 then 0
    private static TextureRegion regionForDigit(int d) {
        return (d == 0) ? digits[9] : digits[d - 1];
    }

    // Draws integer left-to-right from x, returns X after last character
    public static float drawInt(SpriteBatch batch, int value, float x, float y) {
        String s = Integer.toString(value);
        for (int i = 0; i < s.length(); i++) {
            batch.draw(regionForDigit(s.charAt(i) - '0'), x, y, DW, DH);
            x += DW;
        }
        return x;
    }

    public static float measureInt(int value) {
        return Integer.toString(value).length() * DW;
    }

    public static float drawHyphen(SpriteBatch batch, float x, float y) {
        batch.draw(hyphen, x, y, DW, DH);
        return x + DW;
    }

    public static float drawTimes(SpriteBatch batch, float x, float y) {
        batch.draw(times, x, y, DW, DH);
        return x + DW;
    }

    public static float drawGemIcon(SpriteBatch batch, float x, float y) {
        float offset = (DH - ICON_SIZE) / 2f;
        batch.draw(gemIcon, x, y + offset, ICON_SIZE, ICON_SIZE);
        return x + ICON_SIZE + 2f;
    }
}