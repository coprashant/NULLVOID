package com.nullvoid;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

public class GameWorld {

    private static final float WALK_SPEED    = 120f;
    private static final float RUN_SPEED     = 220f;
    private static final float INTRO_SPEED   = 150f;
    private static final float INTRO_DIST    = 10f;
    private static final float SPEED_LERP    = 12f;
    private static final float MAX_SPEED_CAP = 420f;

    private static final float HIT_STUN_DURATION     = 0.4f;
    private static final float HIT_STUN_SPEED_FACTOR = 0.25f;
    private float hitStunTimer = 0f;

    // Player cannot go further back than this many metres from peak distance
    private static final float BACKWARD_LIMIT_METRES = 10f;
    private float peakDistance = 0f;

    private boolean introActive   = true;
    private float   introScrolled = 0f;

    private float speed       = 0f;
    private float targetSpeed = 0f;
    private float scrollDir   = 1f;

    private int     score     = 0;
    private int     highScore = 0;
    private float   distance  = 0f;
    private boolean gameOver  = false;

    // Top-5 leaderboard held in memory; NullVoid persists it via getScores()
    public static final int LEADERBOARD_SIZE = 5;
    private int[] scores = new int[LEADERBOARD_SIZE];

    private float alienTimer = 0f;
    private float rockTimer  = 0f;
    private float ceilTimer  = 0f;
    private float gemTimer   = 0f;

    private float lastSpawnTime = 0f;
    private float gameTime      = 0f;

    private boolean jumpedThisFrame       = false;
    private boolean gemCollectedThisFrame = false;

    private MilestoneManager milestones = new MilestoneManager();

    private Player               player;
    private Background           background;
    private Array<Alien>         aliens   = new Array<>();
    private Array<Rock>          rocks    = new Array<>();
    private Array<CeilingGap>    ceilings = new Array<>();
    private Array<Collectible>   gems     = new Array<>();
    private Array<GlassShard>    shards   = new Array<>();
    private Array<DustEffect>    dust     = new Array<>();
    private Array<SparkleEffect> sparkles = new Array<>();
    private Array<ScorePopup>    popups   = new Array<>();

    private Random rng = new Random();

    public void create() {
        Alien.loadAssets();
        Rock.loadAssets();
        Collectible.loadAssets();
        DustEffect.loadAssets();
        SparkleEffect.loadAssets();
        GlassShard.loadAssets();

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
        shards.clear();
        dust.clear();
        sparkles.clear();
        popups.clear();
        milestones.reset();

        speed         = 0f;
        targetSpeed   = 0f;
        scrollDir     = 1f;
        hitStunTimer  = 0f;
        introActive   = true;
        introScrolled = 0f;
        score         = 0;
        distance      = 0f;
        peakDistance  = 0f;
        gameOver      = false;
        alienTimer    = 0f;
        rockTimer     = 0f;
        ceilTimer     = 0f;
        gemTimer      = 0f;
        gameTime      = 0f;
        lastSpawnTime = 0f;
    }

    public void update(float delta, InputHandler input) {
        jumpedThisFrame       = false;
        gemCollectedThisFrame = false;

        if (gameOver) return;

        gameTime += delta;

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

        milestones.update(delta, (int) distance);

        if (milestones.justHitMilestone()) {
            targetSpeed = Math.min(targetSpeed + milestones.getSpeedBonus() * 0.3f,
                                   MAX_SPEED_CAP);
        }

        boolean wasJumping = player.isJumping();
        player.update(delta, input);
        if (!wasJumping && player.isJumping()) jumpedThisFrame = true;

        float runCap = Math.min(RUN_SPEED + milestones.getSpeedBonus(), MAX_SPEED_CAP);

        Player.MoveState ms = player.getMoveState();
        switch (ms) {
            case WALK_RIGHT: targetSpeed = WALK_SPEED; scrollDir =  1f; break;
            case RUN_RIGHT:  targetSpeed = runCap;     scrollDir =  1f; break;
            case WALK_LEFT:  targetSpeed = WALK_SPEED; scrollDir = -1f; break;
            case RUN_LEFT:   targetSpeed = runCap;     scrollDir = -1f; break;
            default:         targetSpeed = 0f;                          break;
        }

        speed += (targetSpeed - speed) * Math.min(delta * SPEED_LERP, 1f);

        float effectiveSpeed = speed;
        if (hitStunTimer > 0f) {
            hitStunTimer  -= delta;
            effectiveSpeed = speed * HIT_STUN_SPEED_FACTOR;
        }

        if (scrollDir > 0 && effectiveSpeed > 0) {
            distance += effectiveSpeed * delta * 0.04f;
            if (distance > peakDistance) peakDistance = distance;
        } else if (scrollDir < 0 && effectiveSpeed > 0) {
            float proposed = distance - effectiveSpeed * delta * 0.04f;
            float minAllowed = peakDistance - BACKWARD_LIMIT_METRES;
            if (proposed < minAllowed) {
                // Distance is clamped but speed is left alone so input still feels live
                distance     = Math.max(minAllowed, 0f);
                effectiveSpeed = 0f;
            } else {
                distance = Math.max(proposed, 0f);
            }
        }

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

    public void render(SpriteBatch batch) {
        batch.begin();
        background.render(batch);
        for (Collectible   g : gems)     g.render(batch);
        for (Rock          r : rocks)    r.render(batch);
        for (CeilingGap    c : ceilings) c.render(batch);
        for (Alien         a : aliens)   a.render(batch);
        for (GlassShard    s : shards)   s.render(batch);
        for (DustEffect    d : dust)     d.render(batch);
        for (SparkleEffect s : sparkles) s.render(batch);
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
        SparkleEffect.disposeAssets();
        GlassShard.disposeAssets();
    }

    public boolean           isGameOver()             { return gameOver;  }
    public int               getScore()               { return score;     }
    public int               getHighScore()           { return highScore; }
    public int               getLives()               { return player.getLives(); }
    public int               getDistance()            { return (int) distance; }
    public float             getSpeed()               { return speed; }
    public boolean           isIntro()                { return introActive; }
    public void              setHighScore(int hs)     { highScore = hs; }
    public Array<ScorePopup> getPopups()              { return popups; }
    public boolean           playerJustJumped()       { return jumpedThisFrame; }
    public boolean           playerJustCollectedGem() { return gemCollectedThisFrame; }
    public MilestoneManager  getMilestones()          { return milestones; }

    public int[] getScores() { return scores; }

    public void setScores(int[] saved) {
        for (int i = 0; i < LEADERBOARD_SIZE; i++)
            scores[i] = (i < saved.length) ? saved[i] : 0;
    }

    // Inserts current score into the sorted leaderboard and returns its rank (1-based), or -1
    public int submitScore(int s) {
        int rank = -1;
        for (int i = 0; i < LEADERBOARD_SIZE; i++) {
            if (s > scores[i]) {
                // Shift lower entries down
                for (int j = LEADERBOARD_SIZE - 1; j > i; j--)
                    scores[j] = scores[j - 1];
                scores[i] = s;
                rank = i + 1;
                break;
            }
        }
        return rank;
    }

    private boolean canSpawnObstacle() {
        return (gameTime - lastSpawnTime) >= milestones.getSpawnSeparation();
    }

    private void markSpawned() { lastSpawnTime = gameTime; }

    private void spawnAliens(float delta) {
        if (scrollDir < 0 || speed < 10f) return;
        alienTimer += delta;
        if (alienTimer < milestones.getAlienInterval()) return;
        alienTimer = 0f;
        if (!canSpawnObstacle()) return;
        markSpawned();
        if (rng.nextBoolean()) aliens.add(Alien.createWalker(NullVoid.W + 60f));
        else                   aliens.add(Alien.createPatrol(NullVoid.W + 60f, 55f));
    }

    private void spawnRocks(float delta) {
        if (scrollDir < 0 || speed < 10f) return;
        rockTimer += delta;
        if (rockTimer < milestones.getRockInterval()) return;
        rockTimer = 0f;
        if (!canSpawnObstacle()) return;
        markSpawned();
        rocks.add(new Rock(NullVoid.W + 60f));
        if (rng.nextFloat() < 0.25f)
            rocks.add(new Rock(NullVoid.W + 180f));
    }

    private void spawnCeilings(float delta) {
        if (scrollDir < 0 || speed < 10f) return;
        ceilTimer += delta;
        if (ceilTimer < milestones.getCeilInterval()) return;
        ceilTimer = 0f;
        if (!canSpawnObstacle()) return;
        markSpawned();
        ceilings.add(new CeilingGap(NullVoid.W + 60f));
    }

    private void spawnGems(float delta) {
        if (speed < 10f) return;
        gemTimer += delta;
        float gemInterval = Math.max(1.5f, 3f - milestones.getTier() * 0.1f);
        if (gemTimer < gemInterval) return;
        gemTimer = 0f;
        float spawnX = NullVoid.W + 60f;
        for (Alien a : aliens) {
            if (!a.isDead() && Math.abs(a.getX() - spawnX) < 120f) return;
        }
        int count = rng.nextInt(3) + 1;
        for (int i = 0; i < count; i++)
            gems.add(new Collectible(spawnX + i * 40f));
    }

    private void updateObjects(float delta) {
        float worldVel = speed * scrollDir;

        for (int i = aliens.size - 1; i >= 0; i--) {
            Alien a = aliens.get(i);
            a.update(delta, worldVel, rocks, player.getX());
            GlassShard shard = a.pollShard();
            if (shard != null) shards.add(shard);
            if (a.isRemovable() || a.isOffScreen()) aliens.removeIndex(i);
        }
        for (int i = shards.size - 1; i >= 0; i--) {
            GlassShard s = shards.get(i);
            s.update(delta, worldVel);
            if (s.isExpired()) shards.removeIndex(i);
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
            g.update(delta, worldVel, rocks);
            if (g.isOffScreen() || g.isCollected()) gems.removeIndex(i);
        }
        for (int i = dust.size - 1; i >= 0; i--) {
            DustEffect d = dust.get(i);
            d.update(delta);
            if (!d.isActive()) dust.removeIndex(i);
        }
        for (int i = sparkles.size - 1; i >= 0; i--) {
            SparkleEffect s = sparkles.get(i);
            s.update(delta);
            if (!s.isActive()) sparkles.removeIndex(i);
        }
        for (int i = popups.size - 1; i >= 0; i--) {
            ScorePopup p = popups.get(i);
            p.update(delta);
            if (!p.isActive()) popups.removeIndex(i);
        }
    }

    private void checkCollisions() {
        for (Rock r : rocks) {
            if (r.isPassed()) continue;
            if (overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         r.hitX(), r.hitY(), r.hitW(), r.hitH())) {
                triggerHit(); return;
            }
            if (r.getX() + Rock.SIZE < player.getX()) r.markPassed();
        }

        for (CeilingGap c : ceilings) {
            if (c.isPassed()) continue;
            if (overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         c.hitX(), c.hitY(), c.hitW(), c.hitH())) {
                triggerHit();
            }
            if (c.getX() + CeilingGap.GAP_WIDTH < player.getX()) c.markPassed();
        }

        for (Alien a : aliens) {
            if (a.isDead()) continue;
            boolean stomped = player.isJumping()
                && overlaps(player.stompX(), player.stompY(),
                            player.stompW(), player.stompH(),
                            a.headX(), a.headY(), a.headW(), a.headH());
            if (stomped) {
                a.die();
                score += 10;
                spawnDust(a.getX(), a.getY() + Alien.SIZE * 0.5f);
                spawnPopup(a.getX(), a.getY() + Alien.SIZE, "+10", 1f, 0.85f, 0.2f);
                continue;
            }
            if (!player.isInvincible() &&
                overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         a.hitX(), a.hitY(), a.hitW(), a.hitH())) {
                boolean died = player.hit();
                spawnDust(player.getX(), player.getY());
                if (died) triggerGameOver();
            }
        }

        for (GlassShard s : shards) {
            if (!player.isInvincible() &&
                overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         s.hitX(), s.hitY(), s.hitW(), s.hitH())) {
                s.expire();
                boolean died = player.hit();
                spawnDust(player.getX(), player.getY());
                if (died) triggerGameOver();
            }
        }

        for (Collectible g : gems) {
            if (g.isCollected()) continue;
            if (overlaps(player.hitX(), player.hitY(),
                         player.hitW(), player.hitH(),
                         g.hitX(), g.hitY(), g.hitW(), g.hitH())) {
                g.collect();
                score += 5;
                gemCollectedThisFrame = true;
                spawnSparkle(g.getX(), g.getY());
                spawnPopup(g.getX(), g.getY() + Collectible.SIZE, "+5", 0.78f, 0.48f, 1f);
            }
        }
    }

    private void triggerHit() {
        boolean died = player.hit();
        spawnDust(player.getX(), player.getY() + Player.SIZE);
        if (died) triggerGameOver();
        else      hitStunTimer = HIT_STUN_DURATION;
    }

    private void triggerGameOver() {
        gameOver = true;
        if (score > highScore) highScore = score;
    }

    private void spawnDust(float x, float y) {
        DustEffect d = new DustEffect(); d.play(x, y); dust.add(d);
    }

    private void spawnSparkle(float x, float y) {
        SparkleEffect s = new SparkleEffect(); s.play(x, y); sparkles.add(s);
    }

    private void spawnPopup(float x, float y, String text,
                            float r, float g, float b) {
        ScorePopup p = new ScorePopup(); p.play(x, y, text, r, g, b); popups.add(p);
    }

    private boolean overlaps(float ax, float ay, float aw, float ah,
                              float bx, float by, float bw, float bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }
}