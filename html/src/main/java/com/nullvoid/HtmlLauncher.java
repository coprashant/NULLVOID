package com.nullvoid;

import com.badlogic.gdx.backends.teavm.TeaApplicationConfiguration;
import com.badlogic.gdx.backends.teavm.TeaApplication;

/**
 * HtmlLauncher — the browser entry point.
 *
 * TeaVM compiles this class (and everything it references) into game.js.
 * This is the ONLY class in :html/src. Everything else is in :core.
 *
 * Think of it like: "this file says HOW to run the game in a browser".
 * The game itself (NullVoid.java) doesn't know or care it's in a browser.
 */
public class HtmlLauncher {

    /**
     * main() is what TeaVM looks for as the entry point.
     * When the browser loads game.js, this runs first.
     */
    public static void main(String[] args) {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        // "canvas" must match the id= of the <canvas> tag in index.html

        config.width  = 800;   // game canvas size
        config.height = 480;

        // Boot the game — hands control to NullVoid.create()
        new TeaApplication(new NullVoid(), config);
    }
}
