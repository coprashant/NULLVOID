package com.nullvoid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * InputHandler — wraps LibGDX input into clean game actions.
 *
 * Why not call Gdx.input directly in GameWorld?
 * Because this class is the ONLY place that knows about keyboard keys.
 * If you later add touch/gamepad, you only change this class.
 *
 * justPressed = only true for ONE frame when key is first pushed down.
 * This prevents holding UP and skipping multiple lanes per second.
 */
public class InputHandler {

    public boolean isMoveUp() {
        return Gdx.input.isKeyJustPressed(Input.Keys.UP)
            || Gdx.input.isKeyJustPressed(Input.Keys.W);
    }

    public boolean isMoveDown() {
        return Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
            || Gdx.input.isKeyJustPressed(Input.Keys.S);
    }

    public boolean isStartPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.justTouched();   // tap on mobile / browser
    }
}
