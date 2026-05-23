package com.chronicorn.frontend.custom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class FadingPortrait extends Image {
    private static ShaderProgram shader = null;

    public FadingPortrait() {
        super();

        if (shader == null) {
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
                    "    gl_Position = u_projTrans * a_position;\n" +
                    "}";

            String fragmentShader =
                "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "varying vec4 v_color;\n" +
                    "varying vec2 v_texCoords;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "void main() {\n" +
                    "    vec4 texColor = texture2D(u_texture, v_texCoords);\n" +

                    "    // FADES OUT RIGHT: 1.0 (Full Alpha) at X=0.90 to 0.0 (Invisible) at X=0.95\n" +
                    "    float fadeX = 1.0 - smoothstep(0.90, 0.95, v_texCoords.x);\n" +

                    "    // FADES OUT BOTTOM: 1.0 (Full Alpha) at Y=0.80 to 0.0 (Invisible) at Y=1.00\n" +
                    "    float fadeY = 1.0 - smoothstep(0.95, 1.00, v_texCoords.y);\n" +

                    "    // Combine the fades\n" +
                    "    float totalFade = fadeX * fadeY;\n" +

                    "    gl_FragColor = v_color * texColor * vec4(1.0, 1.0, 1.0, totalFade);\n" +
                    "}";

            shader = new ShaderProgram(vertexShader, fragmentShader);
            if (!shader.isCompiled()) {
                Gdx.app.error("FadingPortrait", "Shader compile error: " + shader.getLog());
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShaderProgram oldShader = batch.getShader();
        batch.setShader(shader);

        super.draw(batch, parentAlpha);

        batch.setShader(oldShader);
    }
}
