package com.nullvoid;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Files;

public class DesktopLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config =
                new Lwjgl3ApplicationConfiguration();

        config.setTitle("N U L L V O I D");
        config.setWindowedMode(960, 540);
        config.setResizable(true);
        config.setWindowPosition(-1, -1);
        config.setForegroundFPS(60);
        config.useVsync(true);

        new Lwjgl3Application(new NullVoid(), config);
    }
}