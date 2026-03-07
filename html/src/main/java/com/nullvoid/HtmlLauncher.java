package com.nullvoid;

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;

public class HtmlLauncher {

    public static void main(String[] args) {
        TeaApplicationConfiguration config =
                new TeaApplicationConfiguration("canvas");

        config.width  = 800;
        config.height = 480;

        new TeaApplication(new NullVoid(), config);
    }
}