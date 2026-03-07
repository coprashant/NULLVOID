package com.nullvoid;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

public class GameWorld {

    private static final float WALK_SPEED  = 120f;
    private static final float RUN_SPEED   = 220f;
    private static final float INTRO_SPEED = 150f;
    private static final float INTRO_DIST  = 10f;

    private static final float SPEED_LERP = 12f;

    private static final float HIT_STUN_DURATION     = 0.4f;
    private static final float HIT_STUN_SPEED_FACTOR = 0.25f;
    private float hitStunTimer = 0f;

    private boolean introActive   = true;
    private float   introScrolled = 0f;

    private float speed      = 0f;   // current (lerped) speed
    private float targetSpeed = 0f;  // what speed is chasing
    private float scrollDir  = 1f;

    private int     score     = 0;
    private int     highScore = 0;
    private float   distance  = 0f;
    private boolean gameOver  = false;

    private float alienInterval = 5f;
    private float rockInterval  = 3.5f;
    private float ceilInterval  = 8f;
    private float alienTimer    = 0f;
    private float rockTimer     = 0f;
    private float ceilTimer     = 0f;
    private float gemTimer      = 0f;
    private float gemInterval   = 3f;

    private Player             player;
    private Background         background;
    private Array<Alien>       aliens   = new Array<>();
    private Array<Rock>        rocks    = new Array<>();
    private Array<CeilingGap>  ceilings = new Array<>();
    private Array<Collectible> gems     = new Array<>();
    private Array<DustEffect>  dust     = new Array<>();

    private Random rng = new Random();

    // ── Lifecycle ──────────────────────────────────────────────

    public void create() {
        Alien.loadAssets();
        Rock.loadAssets();
        Collectible.loadAssets();
        DustEffect.loadAssets();

        player     = new Player();
        background = new Background();
        player.create();
        background.create();
        CeilingGap.loadAssets(background.getTileSheet());

        reset();
    }

    public void reset() {
        player.reset();
        aliens.clear();
        rocks.clear();
        ceilings.clear();
        gems.clear();
        dust.clear();

        speed         = 0f;
        targetSpeed   = 0f;
        scrollDir     = 1f;
        hitStunTimer  = 0f;
        introActive   = true;
        introScrolled = 0f;
        score         = 0;
        distance      = 0f;
        gameOver      = false;
        alienTimer    = 0f;
        rockTimer     = 0f;
        ceilTimer     = 0f;
        gemTimer      = 0f;
        alienInterval = 5f;
        rockInterval  = 3.5f;
        ceilInterval  = 8f;
    }

    // ── Update ─────────────────────────────────────────────────

    public void update(float delta, InputHandler input) {
        if (gameOver) return;

        // ── Intro sequence ─────────────────────────────────────
        if (introActive) {
            boolean slideComplete = player.updateIntro(delta);

            if (slideComplete) {
                speed          = INTRO_SPEED;
                scrollDir      = 1f;
                introScrolled += INTRO_SPEED * delta * 0.04f;
                updateObjects(delta);
                background.update(delta, speed);
                distance += INTRO_SPEED * delta * 0.04f;

                if (introScrolled >= INTRO_DIST) {
                    introActive = false;
                    speed       = 0f;
                    targetSpeed = 0f;
                }
            } else {
                background.update(delta, 0f);
            }
            return;
        }

        // ── Normal gameplay ────────────────────────────────────
        player.update(delta, input);

        Player.MoveState ms = player.getMoveState();

        // Determine target speed from input
        switch (ms) {
            case WALK_RIGHT: targetSpeed = WALK_SPEED; scrollDir =  1f; break;
            case RUN_RIGHT:  targetSpeed = RUN_SPEED;  scrollDir =  1f; break;
            case WALK_LEFT:  targetSpeed = WALK_SPEED; scrollDir = -1f; break;
            case RUN_LEFT:   targetSpeed = RUN_SPEED;  scrollDir = -1f; break;
            default:         targetSpeed = 0f;                          break;
        }

        // Lerp current speed toward target — smooth acceleration/deceleration
        speed += (targetSpeed - speed) * Math.min(delta * SPEED_LERP, 1f);

        // Hit stun — clamp effective speed while the stun window is active
        float effectiveSpeed = speed;
        if (hitStunTimer > 0f) {
            hitStunTimer  -= delta;
            effectiveSpeed = speed * HIT_STUN_SPEED_FACTOR;
        }

        if (scrollDir > 0 && effectiveSpeed > 0)
            distance += effectiveSpeed * delta * 0.04f;

        // Distance-based difficulty scaling (0 → 1 over 500 m)
        float t = Math.min(distance / 500f, 1f);
        alienInterval = 5f   - t * 2.5f;
        rockInterval  = 3.5f - t * 1.3f;
        ceilInterval  = 8f   - t * 3f;

        background.update(delta, effectiveSpeed * scrollDir);

        spawnAliens(delta);
        spawnRocks(delta);
        spawnCeilings(delta);
        spawnGems(delta);

        updateObjects(delta);
        checkCollisions();

        if (player.justLanded())
            spawnDust(player.getX(), Player.GROUND_Y);
    }

    // ── Render ─────────────────────────────────────────────────

    public void render(SpriteBatch batch) {
        batch.begin();
        background.render(batch);
        for (Collectible g : gems)     g.render(batch);
        for (Rock        r : rocks)    r.render(batch);
        for (CeilingGap  c : ceilings) c.render(batch);
        for (Alien       a : aliens)   a.render(batch);
        for (DustEffect  d : dust)     d.render(batch);
        player.render(batch);
        batch.end();
    }

    public void dispose() {
        player.dispose();
        background.dispose();
        Alien.disposeAssets();
        Rock.disposeAssets();
        Collectible.disposeAssets();
        DustEffect.disposeAssets();
    }

    // ── Accessors ──────────────────────────────────────────────

    public boolean isGameOver()   { return gameOver;  }
    public int     getScore()     { return score;     }
    public int     getHighScore() { return highScore; }
    public int     getLives()     { return player.getLives(); }
    public int     getDistance()  { return (int)distance; }
    public float   getSpeed()     { return speed; }
    public boolean isIntro()      { return introActive; }
    public void    setHighScore(int hs) { highScore = hs; }

    // ── Spawning ───────────────────────────────────────────────

    private void spawnAliens(float delta) {
        if (scrollDir < 0 || speed < 10f) return;
        alienTimer += delta;
        if (alienTimer < alienInterval) return;
        alienTimer = 0f;
        if (rng.nextBoolean()) {
            aliens.add(Alien.createWalker(NullVoid.W + 60f));
        } else {
            aliens.add(Alien.createPatrol(NullVoid.W + 60f, 55f));
        }
    }

    private void spawnRocks(float delta) {
        if (scrollDir < 0 || speed < 10f) return;
        rockTimer += delta;
        if (rockTimer < rockInterval) return;
        rockTimer = 0f;
        rocks.add(new Rock(NullVoid.W + 60f));
        if (rng.nextFloat() < 0.25f)
            rocks.add(new Rock(NullVoid.W + 180f));
    }

    private void spawnCeilings(float delta) {
        if (scrollDir < 0 || speed < 10f) return;
        ceilTimer += delta;
        if (ceilTimer < ceilInterval) return;
        ceilTimer = 0f;
        ceilings.add(new CeilingGap(NullVoid.W + 60f));
    }

    private void spawnGems(float delta) {
        if (speed < 10f) return;
        gemTimer += delta;
        if (gemTimer < gemInterval) return;
        gemTimer = 0f;
        float spawnX = NullVoid.W + 60f;
        for (Rock r : rocks) {
            if (Math.abs(r.getX() - spawnX) < 120f) return;
        }
        int count = rng.nextInt(3) + 1;
        for (int i = 0; i < count; i++)
            gems.add(new Collectible(spawnX + i * 40f));
    }

    // ── Object updates ─────────────────────────────────────────

    private void updateObjects(float delta) {
        float worldVel = speed * scrollDir;

        for (int i = aliens.size - 1; i >= 0; i--) {
            Alien a = aliens.get(i);
            a.update(delta, worldVel, rocks);
            if (a.isRemovable() || a.isOffScreen())
                aliens.removeIndex(i);
        }
        for (int i = rocks.size - 1; i >= 0; i--) {
            Rock r = rocks.get(i);
            r.update(delta, worldVel);
            if (r.isOffScreen()) rocks.removeIndex(i);
        }
        for (int i = ceilings.size - 1; i >= 0; i--) {
            CeilingGap c = ceilings.get(i);
            c.update(delta, worldVel);
            if (c.isOffScreen()) ceilings.removeIndex(i);
        }
        for (int i = gems.size - 1; i >= 0; i--) {
            Collectible g = gems.get(i);
            g.update(delta, worldVel);
            if (g.isOffScreen() || g.isCollected())
                gems.removeIndex(i);
        }
        for (int i = dust.size - 1; i >= 0; i--) {
            DustEffect d = dust.get(i);
            d.update(delta);
            if (!d.isActive()) dust.removeIndex(i);
        }
    }

    // ── Collision ──────────────────────────────────────────────

    private void checkCollisions() {
        // Rocks
        for (Rock r : rocks) {
            if (r.isPassed()) continue;
            if (overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         r.hitX(), r.hitY(),
                         r.hitW(), r.hitH())) {
                triggerHit();
                return;
            }
            if (r.getX() + Rock.SIZE < player.getX()) r.markPassed();
        }

        // Ceiling gaps — hitbox height handles selectivity, no isJumping() gate
        for (CeilingGap c : ceilings) {
            if (c.isPassed()) continue;
            if (overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         c.hitX(), c.hitY(),
                         c.hitW(), c.hitH())) {
                triggerHit();
            }
            if (c.getX() + CeilingGap.GAP_WIDTH < player.getX())
                c.markPassed();
        }

        // Aliens
        for (Alien a : aliens) {
            if (a.isDead()) continue;

            boolean stomped = player.isJumping()
                && overlaps(player.stompX(), player.stompY(),
                            player.stompW(), player.stompH(),
                            a.headX(), a.headY(),
                            a.headW(), a.headH());
            if (stomped) {
                a.die();
                score += 10;
                spawnDust(a.getX(), a.getY() + Alien.SIZE * 0.5f);
                continue;
            }

            if (!player.isInvincible() &&
                overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         a.hitX(), a.hitY(),
                         a.hitW(), a.hitH())) {
                boolean died = player.hit();
                spawnDust(player.getX(), player.getY());
                if (died) triggerGameOver();
            }
        }

        // Gems
        for (Collectible g : gems) {
            if (g.isCollected()) continue;
            if (overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         g.hitX(), g.hitY(),
                         g.hitW(), g.hitH())) {
                g.collect();
                score += 5;
                spawnDust(g.getX(), g.getY());
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────

    private void triggerHit() {
        boolean died = player.hit();
        spawnDust(player.getX(), player.getY() + Player.SIZE);
        if (died) {
            triggerGameOver();
        } else {
            // Activate hit stun — world slows briefly so player isn't chain-hit
            hitStunTimer = HIT_STUN_DURATION;
        }
    }

    private void triggerGameOver() {
        gameOver = true;
        int finalScore = score + getDistance();
        if (finalScore > highScore) highScore = finalScore;
    }

    private void spawnDust(float x, float y) {
        DustEffect d = new DustEffect();
        d.play(x, y);
        dust.add(d);
    }

    private boolean overlaps(float ax, float ay, float aw, float ah,
                              float bx, float by, float bw, float bh) {
        return ax < bx + bw && ax + aw > bx
            && ay < by + bh && ay + ah > by;
    }
}