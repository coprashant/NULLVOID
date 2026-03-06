package com.nullvoid;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * DesktopLauncher — opens a native window and runs your game in it.
 *
 * This is the desktop equivalent of HtmlLauncher.
 * Same idea: "here's how to run NullVoid on THIS platform."
 *
 * LWJGL3 handles:
 *  - Creating the OS window
 *  - Setting up OpenGL for rendering
 *  - Reading keyboard, mouse, gamepad input
 *  - Audio
 *
 * You run this class to play the game during development.
 * Way faster than building for web every time.
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        // Window settings
        config.setTitle("N U L L V O I D");
        config.setWindowedMode(800, 480);       // same size as the web canvas
        config.setResizable(false);              // fixed size for now

        // FPS cap — 60 is standard
        config.setForegroundFPS(60);
        config.setIdleFPS(30);                  // slow down when window is not focused

        // Use vsync to prevent screen tearing
        config.useVsync(true);

        // Window icon (optional — we'll add this later when we have assets)
        // config.setWindowIcon("icon.png");

        // Boot the game — hands control to NullVoid.create()
        // From here, LibGDX calls create() once, then render() ~60x per second
        new Lwjgl3Application(new NullVoid(), config);
    }
}
