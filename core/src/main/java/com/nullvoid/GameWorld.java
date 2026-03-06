package com.nullvoid;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

public class GameWorld {

    private static final float WALK_SPEED  = 120f;
    private static final float RUN_SPEED   = 220f;
    private static final float INTRO_SPEED = 150f;
    private static final float INTRO_DIST  = 10f;

    private boolean introActive   = true;
    private float   introScrolled = 0f;

    private float speed     = 0f;
    private float scrollDir = 1f;

    private int     score     = 0;
    private int     highScore = 0;
    private float   distance  = 0f;
    private boolean gameOver  = false;

    private float alienTimer = 0f, alienInterval = 5f;
    private float rockTimer  = 0f, rockInterval  = 3.5f;
    private float ceilTimer  = 0f, ceilInterval  = 8f;
    private float gemTimer   = 0f, gemInterval   = 3f;

    private Player             player;
    private Background         background;
    private Array<Alien>       aliens   = new Array<>();
    private Array<Rock>        rocks    = new Array<>();
    private Array<CeilingGap>  ceilings = new Array<>();
    private Array<Collectible> gems     = new Array<>();
    private Array<DustEffect>  dust     = new Array<>();

    private Random rng = new Random();

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
        scrollDir     = 1f;
        introActive   = true;
        introScrolled = 0f;
        score         = 0;
        distance      = 0f;
        gameOver      = false;
        alienTimer    = 0f; alienInterval = 5f;
        rockTimer     = 0f; rockInterval  = 3.5f;
        ceilTimer     = 0f; ceilInterval  = 8f;
        gemTimer      = 0f;
    }

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
                }
            } else {
                background.update(delta, 0f);
            }
            return;
        }

        // ── Normal gameplay ────────────────────────────────────
        player.update(delta, input);

        Player.MoveState ms = player.getMoveState();

        // Snap speed instantly based on input — no ramp
        switch (ms) {
            case WALK_RIGHT: speed = WALK_SPEED; scrollDir =  1f; break;
            case RUN_RIGHT:  speed = RUN_SPEED;  scrollDir =  1f; break;
            case WALK_LEFT:  speed = WALK_SPEED; scrollDir = -1f; break;
            case RUN_LEFT:   speed = RUN_SPEED;  scrollDir = -1f; break;
            default:         speed = 0f;                          break;
        }

        if (scrollDir > 0 && speed > 0)
            distance += speed * delta * 0.04f;

        background.update(delta, speed * scrollDir);

        spawnAliens(delta);
        spawnRocks(delta);
        spawnCeilings(delta);
        spawnGems(delta);

        updateObjects(delta);
        checkCollisions();
    }

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

    public boolean isGameOver()   { return gameOver;  }
    public int     getScore()     { return score;     }
    public int     getHighScore() { return highScore; }
    public int     getLives()     { return player.getLives(); }
    public int     getDistance()  { return (int)distance; }
    public float   getSpeed()     { return speed; }
    public boolean isIntro()      { return introActive; }

    private void spawnAliens(float delta) {
        if (scrollDir < 0 || speed < 10f) return;
        alienTimer   += delta;
        alienInterval = Math.max(2.5f, alienInterval - 0.003f * delta);
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
        rockTimer   += delta;
        rockInterval = Math.max(2.2f, rockInterval - 0.002f * delta);
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
        ceilTimer    = 0f;
        ceilInterval = Math.max(5f, ceilInterval - 0.05f);
        ceilings.add(new CeilingGap(NullVoid.W + 60f));
    }

    private void spawnGems(float delta) {
        if (speed < 10f) return;
        gemTimer += delta;
        if (gemTimer < gemInterval) return;
        gemTimer = 0f;
        float spawnX = NullVoid.W + 60f;
        for (Rock r : rocks) {
            if (Math.abs(r.getX() - spawnX) < 80f) return;
        }
        int count = rng.nextInt(3) + 1;
        for (int i = 0; i < count; i++)
            gems.add(new Collectible(spawnX + i * 40f));
    }

    private void checkCollisions() {
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

        for (CeilingGap c : ceilings) {
            if (c.isPassed()) continue;
            if (player.isJumping() &&
                overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         c.hitX(), c.hitY(),
                         c.hitW(), c.hitH())) {
                triggerHit();
            }
            if (c.getX() + CeilingGap.GAP_WIDTH < player.getX())
                c.markPassed();
        }

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

    private void triggerHit() {
        boolean died = player.hit();
        spawnDust(player.getX(), player.getY() + Player.SIZE);
        if (died) triggerGameOver();
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