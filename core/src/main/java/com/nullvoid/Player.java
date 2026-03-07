package com.nullvoid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {

    public static final float FIXED_X  = NullVoid.W * 0.30f;
    public static final float GROUND_Y = 40f;
    public static final float SIZE     = 48f;
    public static final float FRAME_W  = 24f;
    public static final float FRAME_H  = 24f;

    private static final float INTRO_START_X = -SIZE;
    private static final float INTRO_SPEED   = 180f;
    private boolean introMoving = true;
    private float   introX;

    private static final float GRAVITY    = -900f;
    private static final float JUMP_FORCE =  420f;

    private static final float COYOTE_DURATION = 0.10f;
    private float coyoteTimer = 0f;

    public static final int MAX_LIVES = 3;
    private int lives = MAX_LIVES;

    private float   y;
    private float   velY            = 0f;
    private boolean jumping         = false;
    private boolean dead            = false;
    private boolean facingLeft      = false;
    private boolean landedThisFrame = false;

    private float invincibleTimer = 0f;
    private static final float INVINCIBLE_DURATION = 1.5f;

    public enum MoveState { IDLE, WALK_LEFT, WALK_RIGHT,
                            RUN_LEFT,  RUN_RIGHT }
    private MoveState moveState = MoveState.IDLE;

    public enum AnimState { IDLE, RUN, JUMP, DEAD }
    private AnimState animState = AnimState.IDLE;
    private float stateTime = 0f;

    private Texture runTex, idleTex, jumpTex, deathTex;
    private Animation<TextureRegion> runAnim,  runAnimL,
                                     idleAnim, idleAnimL,
                                     jumpAnim, jumpAnimL,
                                     deathAnim;

    // ── Lifecycle ──────────────────────────────────────────────

    public void create() {
        runTex   = new Texture("Astronaut_Run.png");
        idleTex  = new Texture("Astronaut_Idle.png");
        jumpTex  = new Texture("Astronaut_Jump.png");
        deathTex = new Texture("Astronaut_Death.png");

        runAnim   = buildAnim(runTex,   24, 24, 6, 0.10f,
                              Animation.PlayMode.LOOP);
        idleAnim  = buildAnim(idleTex,  24, 24, 6, 0.15f,
                              Animation.PlayMode.LOOP);
        jumpAnim  = buildAnim(jumpTex,  24, 24, 5, 0.10f,
                              Animation.PlayMode.NORMAL);
        deathAnim = buildAnim(deathTex, 32, 32, 4, 0.15f,
                              Animation.PlayMode.NORMAL);

        runAnimL  = buildAnimFlipped(runTex,  24, 24, 6, 0.10f);
        idleAnimL = buildAnimFlipped(idleTex, 24, 24, 6, 0.15f);
        jumpAnimL = buildAnimFlipped(jumpTex, 24, 24, 5, 0.10f);

        reset();
    }

    public void reset() {
        y               = GROUND_Y;
        velY            = 0f;
        jumping         = false;
        dead            = false;
        facingLeft      = false;
        lives           = MAX_LIVES;
        animState       = AnimState.RUN;
        moveState       = MoveState.IDLE;
        stateTime       = 0f;
        invincibleTimer = 0f;
        introMoving     = true;
        introX          = INTRO_START_X;
        landedThisFrame = false;
        coyoteTimer     = 0f;
    }

    // ── Intro ──────────────────────────────────────────────────

    public boolean updateIntro(float delta) {
        stateTime += delta;

        if (introMoving) {
            animState  = AnimState.RUN;
            facingLeft = false;
            introX    += INTRO_SPEED * delta;
            if (introX >= FIXED_X) {
                introX      = FIXED_X;
                introMoving = false;
                animState   = AnimState.IDLE;
            }
            return false;
        }

        animState  = AnimState.RUN;
        facingLeft = false;
        return true;
    }

    // ── Update ─────────────────────────────────────────────────

    public void update(float delta, InputHandler input) {
        stateTime       += delta;
        landedThisFrame  = false;
        if (invincibleTimer > 0f) invincibleTimer -= delta;
        if (dead) return;

        boolean left  = input.isLeft();
        boolean right = input.isRight();
        boolean shift = input.isShift();

        // Direction and move state
        if (left && !right) {
            moveState  = shift ? MoveState.RUN_LEFT : MoveState.WALK_LEFT;
            facingLeft = true;
        } else if (right && !left) {
            moveState  = shift ? MoveState.RUN_RIGHT : MoveState.WALK_RIGHT;
            facingLeft = false;
        } else {
            moveState = MoveState.IDLE;
        }

        // Animation — jump takes priority while airborne
        if (!jumping) {
            animState = (moveState == MoveState.IDLE)
                        ? AnimState.IDLE : AnimState.RUN;
        }

        if (y <= GROUND_Y && !jumping) {
            coyoteTimer = COYOTE_DURATION;
        } else if (coyoteTimer > 0f) {
            coyoteTimer -= delta;
        }

        // Jump — allowed while grounded OR within the coyote window
        if (input.isJump() && coyoteTimer > 0f) {
            velY        = JUMP_FORCE;
            jumping     = true;
            coyoteTimer = 0f;   // consume window so it can't be used twice
            animState   = AnimState.JUMP;
            stateTime   = 0f;
        }

        // Gravity + landing
        if (jumping) {
            velY += GRAVITY * delta;
            y    += velY * delta;
            if (y <= GROUND_Y) {
                y               = GROUND_Y;
                velY            = 0f;
                jumping         = false;
                landedThisFrame = true;
                animState       = (moveState == MoveState.IDLE)
                                  ? AnimState.IDLE : AnimState.RUN;
                stateTime       = 0f;
            }
        }
    }

    // ── Hit / death ────────────────────────────────────────────

    public boolean hit() {
        if (invincibleTimer > 0f) return false;
        lives--;
        invincibleTimer = INVINCIBLE_DURATION;
        if (lives <= 0) {
            dead      = true;
            animState = AnimState.DEAD;
            stateTime = 0f;
            return true;
        }
        return false;
    }

    // ── Render ─────────────────────────────────────────────────

    public void render(SpriteBatch batch) {
        if (invincibleTimer > 0f) {
            if (((int)(invincibleTimer * 10f) % 2) == 0) return;
        }

        float drawX = introMoving ? introX : FIXED_X;
        TextureRegion frame = currentFrame();
        float drawH = (animState == AnimState.DEAD)
                      ? SIZE * (32f / 24f) : SIZE;
        batch.draw(frame, drawX - SIZE / 2f, y, SIZE, drawH);
    }

    public void dispose() {
        runTex.dispose();
        idleTex.dispose();
        jumpTex.dispose();
        deathTex.dispose();
    }

    // ── Accessors ──────────────────────────────────────────────

    public float     getX()          { return FIXED_X; }
    public float     getY()          { return y; }
    public int       getLives()      { return lives; }
    public boolean   isJumping()     { return jumping; }
    public boolean   isDead()        { return dead; }
    public boolean   isInvincible()  { return invincibleTimer > 0f; }
    public MoveState getMoveState()  { return moveState; }
    public boolean   isIntroMoving() { return introMoving; }

    public boolean justLanded() {
        boolean l = landedThisFrame;
        landedThisFrame = false;
        return l;
    }

    // Hitboxes
    public float hitX()   { return FIXED_X - SIZE * 0.28f; }
    public float hitY()   { return y; }
    public float hitW()   { return SIZE * 0.56f; }
    public float hitH()   { return SIZE * 0.85f; }

    public float stompX() { return FIXED_X - SIZE * 0.25f; }
    public float stompY() { return y; }
    public float stompW() { return SIZE * 0.5f; }
    public float stompH() { return 10f; }

    // ── Animation helpers ──────────────────────────────────────

    private TextureRegion currentFrame() {
        switch (animState) {
            case IDLE:
                return (facingLeft ? idleAnimL : idleAnim)
                        .getKeyFrame(stateTime);
            case JUMP:
                return (facingLeft ? jumpAnimL : jumpAnim)
                        .getKeyFrame(stateTime);
            case DEAD:
                return deathAnim.getKeyFrame(stateTime);
            default:
                return (facingLeft ? runAnimL : runAnim)
                        .getKeyFrame(stateTime);
        }
    }

    private Animation<TextureRegion> buildAnim(
            Texture tex, int fw, int fh, int count,
            float spd, Animation.PlayMode mode) {
        TextureRegion[][] g = TextureRegion.split(tex, fw, fh);
        TextureRegion[] frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) frames[i] = g[0][i];
        Animation<TextureRegion> a = new Animation<>(spd, frames);
        a.setPlayMode(mode);
        return a;
    }

    private Animation<TextureRegion> buildAnimFlipped(
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