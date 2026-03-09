package com.nullvoid;

// One floating score popup. Floats up and fades over its lifetime.
public class ScorePopup {

    private static final float LIFETIME  = 0.8f;
    private static final float RISE_SPD  = 28f;

    private float   x, y;
    private String  text;
    private float   timer = 0f;
    private boolean active = false;

    // r/g/b so GameUI can tint by event type
    public float r, g, b;

    public void play(float x, float y, String text, float r, float g, float b) {
        this.x    = x;
        this.y    = y;
        this.text = text;
        this.r    = r;
        this.g    = g;
        this.b    = b;
        timer     = 0f;
        active    = true;
    }

    public void update(float delta) {
        if (!active) return;
        timer += delta;
        y     += RISE_SPD * delta;
        if (timer >= LIFETIME) active = false;
    }

    public float   getX()      { return x; }
    public float   getY()      { return y; }
    public String  getText()   { return text; }
    public float   getAlpha()  { return 1f - (timer / LIFETIME); }
    public boolean isActive()  { return active; }
}