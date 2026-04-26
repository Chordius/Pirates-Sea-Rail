package com.chronicorn.frontend.managers;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderManager {
    public static ShaderProgram portraitFadeShader;
    public static ShaderProgram additiveGlowShader;

    public static void init() {
        initPortraitShaders();
        initGlowShader();
        // You can add initFlashShader(), initGrayscaleShader(), etc. here later
    }

    private static void initPortraitShaders() {
        String vertexShader =
            "attribute vec4 a_position;\n" +
                "attribute vec4 a_color;\n" +
                "attribute vec2 a_texCoord0;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "void main() {\n" +
                "    v_color = a_color;\n" +
                "    v_texCoords = a_texCoord0;\n" +
                "    gl_Position =  u_projTrans * a_position;\n" +
                "}\n";

        String fragmentShader =
            "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "uniform sampler2D u_texture;\n" +
                "void main() {\n" +
                "    vec4 texColor = texture2D(u_texture, v_texCoords);\n" +
                "    \n" +
                "    float fadeLeft = smoothstep(0.25, 0.30, v_texCoords.x);\n" +
                "    float fadeRight = smoothstep(1.0, 0.99, v_texCoords.x);\n" +
                "    float fadeBottom = smoothstep(0.8, 0.70, v_texCoords.y);\n" +
                "    \n" +
                "    texColor.a *= fadeLeft * fadeRight * fadeBottom;\n" +
                "    gl_FragColor = v_color * texColor;\n" +
                "}\n";

        // Important: Set this static property so LibGDX enforces strict GLSL compilation errors
        ShaderProgram.pedantic = false;

        portraitFadeShader = new ShaderProgram(vertexShader, fragmentShader);
        if (!portraitFadeShader.isCompiled()) {
            System.err.println("Shader compilation failed: " + portraitFadeShader.getLog());
        }
    }

    private static void initGlowShader() {
        String vertexShader =
            "attribute vec4 a_position;\n" +
                "attribute vec4 a_color;\n" +
                "attribute vec2 a_texCoord0;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "void main() {\n" +
                "    v_color = a_color;\n" +
                "    v_texCoords = a_texCoord0;\n" +
                "    gl_Position =  u_projTrans * a_position;\n" +
                "}\n";

        String fragmentShader =
            "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "uniform sampler2D u_texture;\n" +
                "void main() {\n" +
                "    vec4 texColor = texture2D(u_texture, v_texCoords);\n" +
                "    \n" +
                "    // Default LibGDX math is: gl_FragColor = v_color * texColor;\n" +
                "    // Our Glow Math: Keep original texture, but ADD the tint color.\n" +
                "    // We multiply the tint by texColor.a so transparent pixels don't glow as solid blocks.\n" +
                "    \n" +
                "    vec3 glow = v_color.rgb * texColor.a * 0.33;\n" +
                "    gl_FragColor = vec4(texColor.rgb + glow, texColor.a);\n" +
                "}\n";

        additiveGlowShader = new ShaderProgram(vertexShader, fragmentShader);
        if (!additiveGlowShader.isCompiled()) {
            System.err.println("Glow Shader failed: " + additiveGlowShader.getLog());
        }
    }

    public static void dispose() {
        if (portraitFadeShader != null) {
            portraitFadeShader.dispose();
        }
    }
}
