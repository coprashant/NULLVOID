package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Alien {

    public static final float SIZE    = 48f;
    public static final float FRAME_W = 32f;
    public static final float FRAME_H = 32f;

    public enum Type { WALKER, PATROL }

    private float   x, y;
    private float   velX, velY;
    private boolean onGround     = true;
    private boolean dead         = false;
    private float   stateTime    = 0f;
    private float   jumpCooldown = 0f;
    private Type    type;

    private float patrolLeft, patrolRight;

    private static final float WALK_SPEED   = 55f;
    private static final float PATROL_SPEED = 40f;
    private static final float GRAVITY      = -700f;
    private static final float JUMP_FORCE   =  360f;

    private static final float LOOK_AHEAD_BASE  = SIZE * 1.2f;
    private static final float LOOK_AHEAD_SPEED = 0.22f;

    // Patrol ranged attack constants
    private static final float SHOOT_RANGE    = 260f;
    private static final float SHOOT_INTERVAL = 2.8f;
    private float shootCooldown = 1.4f;

    // Shard produced this frame, collected by GameWorld via pollShard()
    private GlassShard pendingShard = null;

    private static Texture   runTex, idleTex, jumpTex, deathTex;
    private static Animation<TextureRegion> runAnim,   runAnimL,
                                            idleAnim,
                                            jumpAnim,  jumpAnimL,
                                            deathAnim;

    public static void loadAssets() {
        runTex   = new Texture("Alien_run.png");
        idleTex  = new Texture("Alien_idle.png");
        jumpTex  = new Texture("Alien_jump.png");
        deathTex = new Texture("Alien_death.png");

        runAnim   = buildAnim(runTex,   32, 32, 6, 0.10f,
                              Animation.PlayMode.LOOP);
        idleAnim  = buildAnim(idleTex,  32, 32, 4, 0.15f,
                              Animation.PlayMode.LOOP);
        deathAnim = buildAnim(deathTex, 32, 32, 4, 0.12f,
                              Animation.PlayMode.NORMAL);
        jumpAnim  = buildAnim(jumpTex,  32, 32, 6, 0.10f,
                              Animation.PlayMode.NORMAL);

        runAnimL  = buildAnimFlipped(runTex,  32, 32, 6, 0.10f);
        jumpAnimL = buildAnimFlipped(jumpTex, 32, 32, 6, 0.10f);
    }

    public static void disposeAssets() {
        if (runTex   != null) runTex.dispose();
        if (idleTex  != null) idleTex.dispose();
        if (jumpTex  != null) jumpTex.dispose();
        if (deathTex != null) deathTex.dispose();
    }

    public static Alien createWalker(float spawnX) {
        Alien a = new Alien();
        a.type  = Type.WALKER;
        a.x     = spawnX;
        a.y     = Player.GROUND_Y;
        a.velX  = -WALK_SPEED;
        return a;
    }

    public static Alien createPatrol(float centerX, float range) {
        Alien a       = new Alien();
        a.type        = Type.PATROL;
        a.x           = centerX;
        a.y           = Player.GROUND_Y;
        a.velX        = PATROL_SPEED;
        a.patrolLeft  = centerX - range;
        a.patrolRight = centerX + range;
        return a;
    }

    // Full update: used during normal gameplay so playerX drives the shoot check
    public void update(float delta, float worldSpeed,
                       com.badlogic.gdx.utils.Array<Rock> rocks,
                       float playerX) {
        stateTime   += delta;
        pendingShard = null;
        if (jumpCooldown > 0f) jumpCooldown -= delta;
        if (dead) return;

        x -= worldSpeed * delta;
        if (type == Type.PATROL) {
            patrolLeft  -= worldSpeed * delta;
            patrolRight -= worldSpeed * delta;
        }

        if (!onGround) {
            velY += GRAVITY * delta;
            y    += velY * delta;
            if (y <= Player.GROUND_Y) {
                y         = Player.GROUND_Y;
                velY      = 0f;
                onGround  = true;
                stateTime = 0f;
            }
        }

        if (onGround && jumpCooldown <= 0f) {
            Rock nearest = nearestApproachingRock(rocks, worldSpeed);
            if (nearest != null) {
                if (type == Type.WALKER) {
                    jump();
                } else {
                    float rockCenterX = nearest.hitX() + nearest.hitW() * 0.5f;
                    boolean rockInZone = rockCenterX >= patrolLeft
                                      && rockCenterX <= patrolRight;
                    if (rockInZone) {
                        jump();
                    } else {
                        velX         = (velX > 0) ? -PATROL_SPEED : PATROL_SPEED;
                        jumpCooldown = 0.8f;
                    }
                }
            }
        }

        // Shoot only when grounded, player is to the left and within range
        if (type == Type.PATROL && onGround) {
            if (shootCooldown > 0f) {
                shootCooldown -= delta;
            } else {
                float dist = x - playerX;
                if (dist > 0f && dist < SHOOT_RANGE) {
                    pendingShard  = new GlassShard(x - SIZE * 0.3f,
                                                  y + SIZE * 0.55f);
                    shootCooldown = SHOOT_INTERVAL;
                }
            }
        }

        x += velX * delta;

        if (type == Type.PATROL) {
            if (x >= patrolRight) { x = patrolRight; velX = -PATROL_SPEED; }
            if (x <= patrolLeft)  { x = patrolLeft;  velX =  PATROL_SPEED; }
        }
    }

    // Overload without playerX for intro/legacy paths that do not need shooting
    public void update(float delta, float worldSpeed,
                       com.badlogic.gdx.utils.Array<Rock> rocks) {
        update(delta, worldSpeed, rocks, -9999f);
    }

    // GameWorld calls this once per frame after update() to collect any fired shard
    public GlassShard pollShard() {
        GlassShard s = pendingShard;
        pendingShard  = null;
        return s;
    }

    public void die() {
        dead      = true;
        stateTime = 0f;
        velX      = 0f;
        velY      = 0f;
    }

    public void render(SpriteBatch batch) {
        TextureRegion frame;

        if (dead) {
            frame = deathAnim.getKeyFrame(stateTime);
        } else if (!onGround) {
            frame = (velX < 0 ? jumpAnimL : jumpAnim).getKeyFrame(stateTime);
        } else if (velX < 0) {
            frame = runAnimL.getKeyFrame(stateTime);
        } else {
            frame = runAnim.getKeyFrame(stateTime);
        }

        batch.draw(frame, x - SIZE / 2f, y, SIZE, SIZE);
    }

    public float   getX()        { return x; }
    public float   getY()        { return y; }
    public boolean isDead()      { return dead; }
    public boolean isOffScreen() { return x < -SIZE * 2; }
    public boolean isRemovable() {
        return dead && deathAnim.isAnimationFinished(stateTime);
    }

    public float hitX() { return x - SIZE * 0.3f;  }
    public float hitY() { return y + SIZE * 0.15f; }
    public float hitW() { return SIZE * 0.6f;       }
    public float hitH() { return SIZE * 0.6f;       }

    public float headX() { return x - SIZE * 0.25f; }
    public float headY() { return y + SIZE * 0.65f; }
    public float headW() { return SIZE * 0.5f;       }
    public float headH() { return SIZE * 0.2f;       }

    private void jump() {
        if (!onGround || jumpCooldown > 0f) return;
        velY         = JUMP_FORCE;
        onGround     = false;
        jumpCooldown = 1.2f;
        stateTime    = 0f;
    }

    private Rock nearestApproachingRock(
            com.badlogic.gdx.utils.Array<Rock> rocks, float worldSpeed) {

        if (Math.abs(y - Player.GROUND_Y) > 4f) return null;

        float lookDist = LOOK_AHEAD_BASE + Math.abs(worldSpeed) * LOOK_AHEAD_SPEED;
        Rock  best     = null;
        float bestDist = Float.MAX_VALUE;

        for (Rock rock : rocks) {
            float rockLeft  = rock.hitX();
            float rockRight = rock.hitX() + rock.hitW();

            boolean approaching;
            float   dist;

            if (velX < 0) {
                float frontX = x - SIZE * 0.3f;
                approaching = rockRight > frontX - lookDist
                           && rockRight < frontX + SIZE * 0.3f;
                dist = frontX - rockRight;
            } else {
                float frontX = x + SIZE * 0.3f;
                approaching = rockLeft >= frontX
                           && rockLeft < frontX + lookDist;
                dist = rockLeft - frontX;
            }

            if (approaching && dist < bestDist) {
                bestDist = dist;
                best     = rock;
            }
        }

        return best;
    }

    private static Animation<TextureRegion> buildAnim(
            Texture tex, int fw, int fh, int count,
            float spd, Animation.PlayMode mode) {
        TextureRegion[][] g = TextureRegion.split(tex, fw, fh);
        TextureRegion[] frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) frames[i] = g[0][i];
        Animation<TextureRegion> a = new Animation<>(spd, frames);
        a.setPlayMode(mode);
        return a;
    }

    private static Animation<TextureRegion> buildAnimFlipped(
            Texture tex, int fw, int fh, int count, float spd) {
        TextureRegion[][] g = TextureRegion.split(tex, fw, fh);
        TextureRegion[] frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) {
            frames[i] = new TextureRegion(g[0][i]);
            frames[i].flip(true, false);
        }
        Animation<TextureRegion> a = new Animation<>(spd, frames);
        a.setPlayMode(Animation.PlayMode.LOOP);
        return a;
    }
}