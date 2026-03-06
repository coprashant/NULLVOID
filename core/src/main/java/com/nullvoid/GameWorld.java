package com.nullvoid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

/**
 * GameWorld — all game logic lives here.
 *
 * Responsibilities:
 *  - Scrolling the background grid
 *  - Moving the packet up/down lanes
 *  - Spawning and moving Firewall Gates
 *  - Tracking which bits (8,4,2,1) are ON
 *  - Checking if packet binary value matches the gate
 *  - Score and speed management
 */
public class GameWorld {

    // ── Layout constants ───────────────────────────────────────
    private static final float W          = NullVoid.WORLD_W;
    private static final float H          = NullVoid.WORLD_H;

    // Four lanes, evenly spaced vertically
    private static final int   LANE_COUNT = 4;
    private static final float LANE_H     = H / LANE_COUNT;      // height of each lane
    private static final float PAD_SIZE   = 60f;                  // bit pad square size
    private static final float PACKET_SIZE= 28f;

    // Bit pad X positions (near the left side, player runs past them)
    private static final float PAD_X      = 120f;

    // Gate constants
    private static final float GATE_W     = 30f;
    private static final float GATE_SPAWN_X = W + 50f;           // spawn off-screen right
    private static final float GATE_PASS_X  = 60f;               // "passed" threshold

    // ── Bit weights for each lane (top to bottom = 8,4,2,1) ───
    private static final int[] BIT_VALUES = {8, 4, 2, 1};

    // ── Game state ─────────────────────────────────────────────
    private float  packetY;                   // packet's Y center position
    private int    packetLane;                // 0=top lane … 3=bottom lane
    private boolean[] bitsOn = new boolean[4]; // which bits are currently ON

    private float  speed      = 180f;         // world scroll speed (px/sec)
    private float  score      = 0;
    private int    highScore  = 0;
    private boolean gameOver  = false;

    // Gate management
    private Array<Gate> gates = new Array<>();
    private float gateTimer   = 0f;
    private float gateInterval= 4f;           // seconds between gates
    private int   nextTarget  = 0;            // the decimal number on next gate

    // Grid scroll offset
    private float scrollOffset = 0f;

    private Random rng = new Random();

    // ── Inner class: Gate ──────────────────────────────────────
    static class Gate {
        float x;           // current X position
        int   target;      // decimal value the player must match
        boolean passed;    // already scored this gate?

        Gate(float x, int target) {
            this.x = x;
            this.target = target;
        }
    }

    // ──────────────────────────────────────────────────────────
    //  PUBLIC API
    // ──────────────────────────────────────────────────────────

    public void reset() {
        packetLane   = 2;                     // start in lane index 2 (bit "2")
        packetY      = laneCenter(packetLane);
        bitsOn       = new boolean[]{false, false, false, false};
        speed        = 180f;
        score        = 0;
        gameOver     = false;
        gateTimer    = 0f;
        gateInterval = 4f;
        gates.clear();
        scrollOffset = 0f;
        spawnGate();                          // put first gate immediately
    }

    public void update(float delta) {
        if (gameOver) return;

        // Scroll background
        scrollOffset += speed * delta;

        // Speed ramp-up: get 5px/sec faster every second
        speed += 5f * delta;
        speed  = Math.min(speed, 500f);       // cap at 500

        // Score: increases with time
        score += delta * 10f;

        // Spawn gates on a timer
        gateTimer += delta;
        if (gateTimer >= gateInterval) {
            gateTimer = 0f;
            gateInterval = Math.max(2f, gateInterval - 0.1f); // gates come faster over time
            spawnGate();
        }

        // Move gates left
        for (Gate gate : gates) {
            gate.x -= speed * delta;
        }

        // Check collisions
        checkGateCollisions();

        // Remove off-screen gates
        for (int i = gates.size - 1; i >= 0; i--) {
            if (gates.get(i).x < -100f) {
                gates.removeIndex(i);
            }
        }
    }

    /** Called by NullVoid to pass input each frame */
    public void handleInput(InputHandler input) {
        int newLane = packetLane;

        if (input.isMoveUp()   && packetLane > 0)            newLane = packetLane - 1;
        if (input.isMoveDown() && packetLane < LANE_COUNT-1) newLane = packetLane + 1;

        if (newLane != packetLane) {
            packetLane = newLane;
            packetY    = laneCenter(packetLane);
            // Toggle the bit for the lane the packet just entered
            bitsOn[packetLane] = !bitsOn[packetLane];
        }
    }

    public void render(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        drawGrid(shapes);
        drawBitPads(shapes, batch, font);
        drawGates(shapes, batch, font);
        drawPacket(shapes);
        drawBitStatus(batch, font);
    }

    public boolean isGameOver() { return gameOver; }
    public float   getScore()   { return score; }
    public int     getHighScore(){ return highScore; }
    public int     getCurrentBinaryValue() {
        int val = 0;
        for (int i = 0; i < 4; i++) if (bitsOn[i]) val += BIT_VALUES[i];
        return val;
    }

    public void dispose() {}

    // ──────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ──────────────────────────────────────────────────────────

    /** Y center of a lane index (0 = top) */
    private float laneCenter(int lane) {
        // lane 0 → near top, lane 3 → near bottom
        return H - (lane * LANE_H) - (LANE_H / 2f);
    }

    private void spawnGate() {
        // Pick a random target between 1 and 15 (all 4-bit values)
        nextTarget = rng.nextInt(15) + 1;
        gates.add(new Gate(GATE_SPAWN_X, nextTarget));
    }

    private void checkGateCollisions() {
        float packetX = 80f;  // packet is fixed at X=80

        for (Gate gate : gates) {
            if (gate.passed) continue;

            // Has the gate reached the packet's X?
            if (gate.x <= packetX + PACKET_SIZE && gate.x + GATE_W >= packetX - PACKET_SIZE) {
                int playerValue = getCurrentBinaryValue();
                if (playerValue == gate.target) {
                    // ✅ Correct! Pulse through.
                    gate.passed = true;
                    score += 100f;
                    if ((int)score > highScore) highScore = (int)score;
                } else {
                    // ❌ Wrong value — game over
                    gameOver = true;
                    if ((int)score > highScore) highScore = (int)score;
                }
            }
        }
    }

    // ── Draw Methods ───────────────────────────────────────────

    private void drawGrid(ShapeRenderer shapes) {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.1f, 0.3f, 0.5f, 0.4f);  // dim blue grid lines

        float spacing = 60f;
        float offset  = scrollOffset % spacing;

        // Vertical lines (moving left = gives sense of speed)
        for (float x = -offset; x < W; x += spacing) {
            shapes.line(x, 0, x, H);
        }
        // Horizontal lane dividers
        for (int i = 1; i < LANE_COUNT; i++) {
            float y = i * LANE_H;
            shapes.line(0, y, W, y);
        }
        shapes.end();
    }

    private void drawBitPads(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        for (int i = 0; i < LANE_COUNT; i++) {
            float centerY = laneCenter(i);
            float padLeft = PAD_X - PAD_SIZE / 2f;
            float padBot  = centerY - PAD_SIZE / 2f;

            // Glow effect: draw a larger dim rectangle behind active pads
            if (bitsOn[i]) {
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(0f, 0.6f, 1f, 0.25f);
                shapes.rect(padLeft - 8, padBot - 8, PAD_SIZE + 16, PAD_SIZE + 16);
                shapes.end();
            }

            // Main pad
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            if (bitsOn[i]) {
                shapes.setColor(0f, 0.8f, 1f, 1f);   // bright cyan = ON
            } else {
                shapes.setColor(0.2f, 0.2f, 0.3f, 1f); // dim grey = OFF
            }
            shapes.rect(padLeft, padBot, PAD_SIZE, PAD_SIZE);
            shapes.end();

            // Pad border
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(bitsOn[i] ? Color.CYAN : Color.DARK_GRAY);
            shapes.rect(padLeft, padBot, PAD_SIZE, PAD_SIZE);
            shapes.end();

            // Bit value label (8, 4, 2, 1)
            batch.begin();
            font.setColor(bitsOn[i] ? Color.WHITE : Color.GRAY);
            font.draw(batch, String.valueOf(BIT_VALUES[i]),
                      PAD_X - 6f, centerY + 7f);
            batch.end();
        }
    }

    private void drawGates(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        for (Gate gate : gates) {
            // Gate bar
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1f, 0.2f, 0.5f, 0.7f);   // neon pink firewall
            shapes.rect(gate.x, 0, GATE_W, H);
            shapes.end();

            // Gate border glow
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(1f, 0.5f, 0.8f, 1f);
            shapes.rect(gate.x - 2, 0, GATE_W + 4, H);
            shapes.end();

            // Target number label
            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, String.valueOf(gate.target),
                      gate.x + 2f, H / 2f + 8f);
            batch.end();
        }
    }

    private void drawPacket(ShapeRenderer shapes) {
        float px = 80f;
        float py = packetY;

        // Outer glow
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 1f, 0.8f, 0.2f);
        shapes.circle(px, py, PACKET_SIZE + 10);

        // Packet body
        shapes.setColor(0f, 1f, 0.8f, 1f);   // neon green-cyan
        shapes.circle(px, py, PACKET_SIZE);

        // Inner highlight
        shapes.setColor(1f, 1f, 1f, 0.6f);
        shapes.circle(px - 6f, py + 6f, PACKET_SIZE * 0.3f);
        shapes.end();
    }

    private void drawBitStatus(SpriteBatch batch, BitmapFont font) {
        // Show current binary value in the HUD (top left)
        int val = getCurrentBinaryValue();
        String binary = String.format("%4s",
                         Integer.toBinaryString(val)).replace(' ', '0');

        batch.begin();
        font.setColor(Color.CYAN);
        font.draw(batch, "BITS: " + binary + "  =" + val,
                  10f, H - 10f);
        font.draw(batch, "SCORE: " + (int)score,
                  10f, H - 30f);
        batch.end();
    }
}
