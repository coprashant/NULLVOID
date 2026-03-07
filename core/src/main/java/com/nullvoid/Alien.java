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

    private static Texture   runTex, idleTex, deathTex;
    private static Animation<TextureRegion> runAnim, runAnimL,
                                            idleAnim, deathAnim;

    // ── Asset loading ──────────────────────────────────────────

    public static void loadAssets() {
        runTex   = new Texture("Alien_run.png");
        idleTex  = new Texture("Alien_idle.png");
        deathTex = new Texture("Alien_death.png");

        runAnim   = buildAnim(runTex,   32, 32, 6, 0.10f,
                              Animation.PlayMode.LOOP);
        idleAnim  = buildAnim(idleTex,  32, 32, 4, 0.15f,
                              Animation.PlayMode.LOOP);
        deathAnim = buildAnim(deathTex, 32, 32, 4, 0.12f,
                              Animation.PlayMode.NORMAL);
        runAnimL  = buildAnimFlipped(runTex, 32, 32, 6, 0.10f);
    }

    public static void disposeAssets() {
        if (runTex   != null) runTex.dispose();
        if (idleTex  != null) idleTex.dispose();
        if (deathTex != null) deathTex.dispose();
    }

    // ── Factories ──────────────────────────────────────────────

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

    // ── Update ─────────────────────────────────────────────────

    public void update(float delta, float worldSpeed,
                       com.badlogic.gdx.utils.Array<Rock> rocks) {
        stateTime    += delta;
        if (jumpCooldown > 0f) jumpCooldown -= delta;
        if (dead) return;

        // Scroll with world
        x -= worldSpeed * delta;
        if (type == Type.PATROL) {
            patrolLeft  -= worldSpeed * delta;
            patrolRight -= worldSpeed * delta;
        }

        // Gravity
        if (!onGround) {
            velY += GRAVITY * delta;
            y    += velY * delta;
            if (y <= Player.GROUND_Y) {
                y        = Player.GROUND_Y;
                velY     = 0f;
                onGround = true;
            }
        }

        // Rock avoidance — only when on ground and cooldown expired
        if (onGround && jumpCooldown <= 0f) {
            for (Rock rock : rocks) {
                if (isApproachingRock(rock, worldSpeed)) {
                    if (type == Type.WALKER) {
                        // Walkers always jump over rocks
                        jump();
                    } else {
                        float rockCenterX = rock.hitX() + rock.hitW() * 0.5f;
                        boolean rockInZone = rockCenterX >= patrolLeft
                                          && rockCenterX <= patrolRight;
                        if (rockInZone) {
                            jump();
                        } else {
                            // Rock is outside patrol range — just flip away
                            velX = -velX;
                            jumpCooldown = 1.0f;
                        }
                    }
                    break;
                }
            }
        }

        // Horizontal movement
        x += velX * delta;

        // Patrol bounds
        if (type == Type.PATROL) {
            if (x > patrolRight) { x = patrolRight; velX = -PATROL_SPEED; }
            if (x < patrolLeft)  { x = patrolLeft;  velX =  PATROL_SPEED; }
        }
    }

    // ── Combat ─────────────────────────────────────────────────

    public void die() {
        dead      = true;
        stateTime = 0f;
        velX      = 0f;
        velY      = 0f;
    }

    // ── Render ─────────────────────────────────────────────────

    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim;
        if (dead) {
            anim = deathAnim;
        } else if (velX < 0) {
            anim = runAnimL;
        } else {
            anim = runAnim;
        }
        TextureRegion frame = anim.getKeyFrame(stateTime);
        batch.draw(frame, x - SIZE / 2f, y, SIZE, SIZE);
    }

    // ── Accessors ──────────────────────────────────────────────

    public float   getX()        { return x; }
    public float   getY()        { return y; }
    public boolean isDead()      { return dead; }
    public boolean isOffScreen() { return x < -SIZE * 2; }
    public boolean isRemovable() {
        return dead && deathAnim.isAnimationFinished(stateTime);
    }

    // Body hitbox
    public float hitX() { return x - SIZE * 0.3f;  }
    public float hitY() { return y + SIZE * 0.15f; }
    public float hitW() { return SIZE * 0.6f;       }
    public float hitH() { return SIZE * 0.6f;       }

    // Stomp target — top of alien's head
    public float headX() { return x - SIZE * 0.25f; }
    public float headY() { return y + SIZE * 0.65f; }
    public float headW() { return SIZE * 0.5f;       }
    public float headH() { return SIZE * 0.2f;       }

    // ── Private helpers ────────────────────────────────────────

    private void jump() {
        if (!onGround || jumpCooldown > 0f) return;
        velY         = JUMP_FORCE;
        onGround     = false;
        jumpCooldown = 1.2f;
    }


    private boolean isApproachingRock(Rock rock, float worldSpeed) {
        if (Math.abs(y - Player.GROUND_Y) > 4f) return false;

        float rockLeft  = rock.hitX();
        float rockRight = rock.hitX() + rock.hitW();

        // Scale look-ahead so aliens react in time at high world speeds
        float lookDist = SIZE * 0.8f + Math.abs(worldSpeed) * 0.15f;

        if (velX < 0) {
            float frontX = x - SIZE * 0.3f;
            return rockRight >= frontX - lookDist
                && rockRight <= frontX + 4f;
        } else {
            float frontX = x + SIZE * 0.3f;
            return rockLeft >= frontX - 4f
                && rockLeft <= frontX + lookDist;
        }
    }

    // ── Animation builders ─────────────────────────────────────

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
            Texture tex, int fw, int fh,
            int count, float spd) {
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