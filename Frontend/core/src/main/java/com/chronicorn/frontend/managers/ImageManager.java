package com.chronicorn.frontend.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.custom.CroppedDrawable;
import com.chronicorn.frontend.custom.HorizontalCropDrawable;
import com.chronicorn.frontend.skills.Action;

import java.util.ArrayList;

public class ImageManager {
    public static Skin skin;
    public static BitmapFont font;
    public static int fontSize = 24;
    public static float modifier = 0.5f;

    public static ShaderProgram portraitFadeShader;

    public static void loadWindowSkin(int fontSize) {
        skin = new Skin();
        loadFontTexture(fontSize);

        // 1. Pixel Assets (PENTING untuk Login UI & Komponen lain)
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white-pixel", new Texture(pixmap));

        Pixmap pixmapDark = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapDark.setColor(0f, 0f, 0f, 0.5f);
        pixmapDark.fill();
        skin.add("dark-pixel", new Texture(pixmapDark));

        // Bersihkan pixmap temp
        pixmap.dispose();
        pixmapDark.dispose();

        // 2. Window Style
        Texture windowTex = new Texture(Gdx.files.internal("Window.png"));
        skin.add("window-base", windowTex);
        NinePatch patch = new NinePatch(windowTex, 12, 12, 12, 12);
        NinePatchDrawable windowDrawable = new NinePatchDrawable(patch);
        skin.add("window-drawable", windowDrawable, Drawable.class);

        skin.add("default", new Label.LabelStyle(font, Color.WHITE));
        skin.add("default", new Window.WindowStyle(font, Color.WHITE, windowDrawable));

        // 3. Button Styles
        // Style Polos
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = font;
        tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);

        // Style Kotak (Login Menu)
        Drawable buttonBg = windowDrawable.tint(new Color(1.2f, 1.2f, 1.2f, 1f));
        TextButton.TextButtonStyle boxedStyle = new TextButton.TextButtonStyle();
        boxedStyle.font = font;
        boxedStyle.fontColor = Color.WHITE;
        boxedStyle.up = buttonBg;
        boxedStyle.down = windowDrawable;
        skin.add("boxed-button", boxedStyle);

        // 4. Components (Panggil method helper di sini)
        loadSliderTexture();
        loadCheckboxTexture();
        loadTextFieldStyle();
        loadHealthBarStyle();

        // Selection Box (Custom Pixmap)
        Pixmap pixmapSel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapSel.setColor(1, 1, 1, 0.3f);
        pixmapSel.fill();
        skin.add("selection-box", new Texture(pixmapSel));
        pixmapSel.dispose();
    }

    private static void loadTextFieldStyle() {
        TextField.TextFieldStyle tfs = new TextField.TextFieldStyle();
        tfs.font = font;
        tfs.fontColor = Color.WHITE;
        tfs.background = skin.newDrawable("dark-pixel");

        // Cursor & Selection
        tfs.cursor = skin.newDrawable("white-pixel", Color.WHITE);
        tfs.cursor.setMinWidth(2f);
        tfs.selection = skin.newDrawable("white-pixel", Color.BLUE);

        // Padding text field
        tfs.background.setLeftWidth(10);
        tfs.background.setRightWidth(10);
        tfs.background.setTopHeight(5);
        tfs.background.setBottomHeight(5);

        skin.add("default", tfs);
    }

    private static void loadHealthBarStyle() {
        // 1. Load Textures
        Texture texBg = new Texture(Gdx.files.internal("HPBar-Back.png"));
        Texture texFill = new Texture(Gdx.files.internal("HPBar-Fill.png"));

        // 2. Buat Drawable
        Drawable drawBg = new TextureRegionDrawable(new TextureRegion(texBg));
        Drawable drawFill = new TextureRegionDrawable(new TextureRegion(texFill));

        // 3. Buat Style
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = drawBg;
        barStyle.knobBefore = drawFill;

        // Trik: Set tinggi minimal agar gambar tidak gepeng
        barStyle.background.setMinHeight(texBg.getHeight());
        barStyle.knobBefore.setMinHeight(texFill.getHeight());

        // 4. Daftarkan ke Skin
        skin.add("hp-bar", barStyle);
    }

    private static void loadSliderTexture() {
        if (!skin.has("white-pixel", Texture.class)) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE); pixmap.fill();
            skin.add("white-pixel", new Texture(pixmap));
            pixmap.dispose();
        }

        Drawable sliderBg = skin.newDrawable("white-pixel", Color.DARK_GRAY);
        sliderBg.setMinWidth(100);
        sliderBg.setMinHeight(10);

        Drawable sliderKnob = skin.newDrawable("white-pixel", Color.LIGHT_GRAY);
        sliderKnob.setMinWidth(10);
        sliderKnob.setMinHeight(20);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = sliderBg;
        sliderStyle.knob = sliderKnob;

        skin.add("default-horizontal", sliderStyle);
    }

    private static void loadCheckboxTexture() {
        Drawable checkOff = skin.newDrawable("white-pixel", Color.RED);
        checkOff.setMinWidth(24);
        checkOff.setMinHeight(24);

        Drawable checkOn = skin.newDrawable("white-pixel", Color.GREEN);
        checkOn.setMinWidth(24);
        checkOn.setMinHeight(24);

        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.checkboxOff = checkOff;
        checkBoxStyle.checkboxOn = checkOn;
        checkBoxStyle.font = font;
        checkBoxStyle.fontColor = Color.WHITE;

        skin.add("default", checkBoxStyle);
    }

    public static void loadFontTexture(int fontSize) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/gamefont.TTF"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        // 1. Generate the font at a higher native resolution if you plan to scale it down with your 0.5f modifier
        parameter.size = fontSize;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;

        // 2. Enable Mipmap generation
        parameter.genMipMaps = true;

        // 3. Use MipMap filtering for scaling down (minFilter) and Linear for scaling up (magFilter)
        parameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        font = generator.generateFont(parameter);

        // 4. Disable integer positions to prevent jittering/blurring when the font is scaled or placed at decimal coordinates
        font.setUseIntegerPositions(false);

        generator.dispose();
    }

    public static void loadBattleAssets(ArrayList<Actor> gameParty, ArrayList<Enemy> gameTroop) {
        ShaderManager.init();

        Texture brownBaseTex = new Texture(Gdx.files.internal("battlehud/brown-base.png"));
        brownBaseTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        skin.add("brown_base", new TextureRegionDrawable(new TextureRegion(brownBaseTex)), Drawable.class);

        // HP Bar
        Texture texBg = new Texture(Gdx.files.internal("battlehud/BattleHPBar-Empty-True.png"), true);
        texBg.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Linear);
        Texture texFill = new Texture(Gdx.files.internal("battlehud/BattleHPBar-Full-True.png"), true);
        texFill.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Linear);
        Drawable drawBg = new TextureRegionDrawable(new TextureRegion(texBg));
        Drawable drawFill = new HorizontalCropDrawable(new TextureRegion(texFill));
        drawBg.setMinWidth(texBg.getWidth() * modifier);
        drawBg.setMinHeight(texBg.getHeight() * modifier);
        drawFill.setMinWidth(texFill.getWidth() * modifier);
        drawFill.setMinHeight(texFill.getHeight() * modifier);
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = null;
        barStyle.knobBefore = drawFill;
        skin.add("hp-battle-bar", barStyle);

        // Catch-Up HP Bar
        Drawable drawRedFill = new HorizontalCropDrawable(new TextureRegion(texFill)).tint(Color.SCARLET);
        drawRedFill.setMinWidth(texFill.getWidth() * modifier);
        drawRedFill.setMinHeight(texFill.getHeight() * modifier);

        ProgressBar.ProgressBarStyle catchupStyle = new ProgressBar.ProgressBarStyle();
        // Background is null so it doesn't double-draw the empty bar
        catchupStyle.background = drawBg;
        catchupStyle.knobBefore = drawRedFill;
        skin.add("hp-catchup-bar", catchupStyle);

        Texture indicatorTex = new Texture(Gdx.files.internal("battlehud/userindicator.png"), true);
        indicatorTex.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        skin.add("user_indicator", new TextureRegionDrawable(new TextureRegion(indicatorTex)), Drawable.class);

        // 2. Load character-specific assets dynamically
        loadActorAsset(gameParty);
        loadEnemyAsset(gameTroop);

        // 3. Load skill icons
//        for (Action skill : availableSkills) {
//            String iconName = "icon_" + skill.getIconId();
//            if (!skin.has(iconName, Drawable.class)) { // Prevent duplicate loading
//                Texture iconTex = new Texture(Gdx.files.internal("ui/skills/" + iconName + ".png"));
//                skin.add(iconName, new TextureRegionDrawable(new TextureRegion(iconTex)), Drawable.class);
//            }
//        }
    }

    public static void loadActorAsset(ArrayList<Actor> activeBattlers) {
        for (Actor battler : activeBattlers) {
            String skillBgName = "skill_bg_" + battler.getElement().name().toLowerCase();
            String portraitName = "portrait_" + battler.getName().toLowerCase();
            String ultBarName = "ult_bar_" + battler.getName().toLowerCase();

            // Load Skill
            Texture skillBgTexWind = new Texture(Gdx.files.internal("battlehud/skill-ui/" + skillBgName + ".png"), true);
            skillBgTexWind.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            skin.add(skillBgName, new TextureRegionDrawable(new TextureRegion(skillBgTexWind)), Drawable.class);

            // Load Portrait
            Texture portraitTex = new Texture(Gdx.files.internal("portraits/" + portraitName + ".png"));
            portraitTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            skin.add(portraitName, new TextureRegionDrawable(new TextureRegion(portraitTex)), Drawable.class);

            // Load Ultimate Bar Progress
            Texture ultBarFill = new Texture(Gdx.files.internal("battlehud/ultimates/" + ultBarName + ".png"));
            Texture ultBarBg = new Texture(Gdx.files.internal("battlehud/ultimates/" + ultBarName + "_bg.png"));
            ultBarFill.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            ultBarBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Drawable drawUltBg = new TextureRegionDrawable(new TextureRegion(ultBarBg));
            Drawable drawUltFill = new CroppedDrawable(new TextureRegion(ultBarFill));
            drawUltBg.setMinWidth(ultBarBg.getWidth());
            drawUltBg.setMinHeight(ultBarBg.getHeight());
            drawUltFill.setMinWidth(ultBarFill.getWidth());
            drawUltFill.setMinHeight(ultBarFill.getHeight());
            ProgressBar.ProgressBarStyle barUltStyle = new ProgressBar.ProgressBarStyle();
            barUltStyle.background = drawUltBg;
            barUltStyle.knobBefore = drawUltFill;
            skin.add("ult-battle-bar" + battler.getName().toLowerCase(), barUltStyle);
            skin.add("ult-battle-bar" + battler.getName().toLowerCase() + "_ghost", new TextureRegionDrawable(new TextureRegion(ultBarFill)), Drawable.class);
        }
    }

    public static void loadEnemyAsset(ArrayList<Enemy> activeBattlers) {
        float modifier = 0.5f;

        // 1. LOAD THE UNIFIED BACKGROUND INDEPENDENTLY
        Texture eBg = new Texture(Gdx.files.internal("battlehud/EnemyBars-Back.png"), true);
        eBg.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);
        Drawable eBgDraw = new TextureRegionDrawable(new TextureRegion(eBg));
        eBgDraw.setMinWidth(eBg.getWidth() * modifier);
        eBgDraw.setMinHeight(eBg.getHeight() * modifier);
        skin.add("enemy-bar-bg", eBgDraw, Drawable.class);

        // 2. HP BAR STYLES
        Texture eHpFill = new Texture(Gdx.files.internal("battlehud/EnemyBars-HP-Fill.png"));
        eHpFill.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Texture eHpEmpty = new Texture(Gdx.files.internal("battlehud/EnemyBars-HP-Empty.png"));
        eHpEmpty.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Drawable eHpFillDraw = new HorizontalCropDrawable(new TextureRegion(eHpFill));
        // Use tinting for the white catch-up effect
        Drawable eHpGhostFillDraw = new HorizontalCropDrawable(new TextureRegion(eHpFill)).tint(Color.ROYAL);
        Drawable eHpEmptyDraw = new TextureRegionDrawable(new TextureRegion(eHpEmpty));

        eHpFillDraw.setMinWidth(eHpFill.getWidth() * modifier);
        eHpFillDraw.setMinHeight(eHpFill.getHeight() * modifier);
        eHpGhostFillDraw.setMinWidth(eHpFill.getWidth() * modifier);
        eHpGhostFillDraw.setMinHeight(eHpFill.getHeight() * modifier);
        eHpEmptyDraw.setMinWidth(eHpEmpty.getWidth() * modifier);
        eHpEmptyDraw.setMinHeight(eHpEmpty.getHeight() * modifier);

        // Ghost Bar Style (Bottom Layer) - Holds the Empty background
        ProgressBar.ProgressBarStyle eHpGhostStyle = new ProgressBar.ProgressBarStyle();
        eHpGhostStyle.background = eHpEmptyDraw;
        eHpGhostStyle.knobBefore = eHpGhostFillDraw;
        skin.add("enemy-hp-ghost-bar", eHpGhostStyle);

        // Main Bar Style (Top Layer) - Background is null so it doesn't cover the ghost
        ProgressBar.ProgressBarStyle eHpStyle = new ProgressBar.ProgressBarStyle();
        eHpStyle.background = null;
        eHpStyle.knobBefore = eHpFillDraw;
        skin.add("enemy-hp-bar", eHpStyle);

        // 3. WEAKNESS BAR STYLES
        Texture eWeakFill = new Texture(Gdx.files.internal("battlehud/EnemyBars-Weak-Fill.png"));
        eWeakFill.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Linear);
        Texture eWeakEmpty = new Texture(Gdx.files.internal("battlehud/EnemyBars-Weak-Empty.png"));
        eWeakEmpty.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Linear);

        Drawable eWeakFillDraw = new HorizontalCropDrawable(new TextureRegion(eWeakFill));
        Drawable eWeakGhostFillDraw = new HorizontalCropDrawable(new TextureRegion(eWeakFill)).tint(Color.GOLD);
        Drawable eWeakEmptyDraw = new TextureRegionDrawable(new TextureRegion(eWeakEmpty));

        eWeakFillDraw.setMinWidth(eWeakFill.getWidth() * modifier);
        eWeakFillDraw.setMinHeight(eWeakFill.getHeight() * modifier);
        eWeakGhostFillDraw.setMinWidth(eWeakFill.getWidth() * modifier);
        eWeakGhostFillDraw.setMinHeight(eWeakFill.getHeight() * modifier);
        eWeakEmptyDraw.setMinWidth(eWeakEmpty.getWidth() * modifier);
        eWeakEmptyDraw.setMinHeight(eWeakEmpty.getHeight() * modifier);

        ProgressBar.ProgressBarStyle eWeakGhostStyle = new ProgressBar.ProgressBarStyle();
        eWeakGhostStyle.background = eWeakEmptyDraw;
        eWeakGhostStyle.knobBefore = eWeakGhostFillDraw;
        skin.add("enemy-weakness-ghost-bar", eWeakGhostStyle);

        ProgressBar.ProgressBarStyle eWeakStyle = new ProgressBar.ProgressBarStyle();
        eWeakStyle.background = null;
        eWeakStyle.knobBefore = eWeakFillDraw;
        skin.add("enemy-weakness-bar", eWeakStyle);

        // 4. ICONS
        TextureAtlas iconAtlas = new TextureAtlas(Gdx.files.internal("icon.atlas"));
        skin.addRegions(iconAtlas);

        // 5. reticle
        Texture reticle = new Texture(Gdx.files.internal("battlehud/target.png"), true);
        reticle.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        skin.add("reticle", new TextureRegionDrawable(new TextureRegion(reticle)), Drawable.class);

        Texture ghostReticle = new Texture(Gdx.files.internal("battlehud/ghost-target.png"), true);
        ghostReticle.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        skin.add("ghost-reticle", new TextureRegionDrawable(new TextureRegion(ghostReticle)), Drawable.class);

        for (Enemy battler : activeBattlers) {
            String enemyName = battler.getName().toLowerCase();
            String enemyCode = "enemy_" + battler.getName().toLowerCase();

            Texture enemyAsset = new Texture(Gdx.files.internal("enemies/" + enemyName + ".png"), true);
            enemyAsset.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            skin.add(enemyCode, new TextureRegionDrawable(new TextureRegion(enemyAsset)), Drawable.class);
        }
    }
    public static void unloadBattleAssets(Array<Battler> activeBattlers, Array<Action> availableSkills) {
        // Remove from skin and dispose the base textures to free up RAM
        skin.remove("skill_bg_green", Drawable.class);
        skin.remove("brown_base", Drawable.class);
        skin.remove("battleback", Drawable.class);
        // Note: You would also call .dispose() on the underlying Texture objects here
        // to prevent memory leaks after the battle ends.
    }

    public static void initPortraitShaders() {
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
                "    // X approaches 0 on the left. Fades from 0.0 to 0.15 (15% of width)\n" +
                "    float fadeLeft = smoothstep(0.25, 0.30, v_texCoords.x);\n" +
                "    \n" +
                "    // X approaches 1 on the right. Fades from 1.0 to 0.85 (15% of width)\n" +
                "    float fadeRight = smoothstep(1.0, 0.99, v_texCoords.x);\n" +
                "    \n" +
                "    // Y approaches 1 at the bottom. Fades from 1.0 to 0.70 (30% of height)\n" +
                "    float fadeBottom = smoothstep(0.8, 0.70, v_texCoords.y);\n" +
                "    \n" +
                "    // Multiply the original alpha by the calculated fade values\n" +
                "    texColor.a *= fadeLeft * fadeRight * fadeBottom;\n" +
                "    \n" +
                "    gl_FragColor = v_color * texColor;\n" +
                "}\n";

        portraitFadeShader = new ShaderProgram(vertexShader, fragmentShader);
        if (!portraitFadeShader.isCompiled()) {
            System.err.println("Shader compilation failed: " + portraitFadeShader.getLog());
        }
    }

    public static void dispose() {
        if (skin != null) skin.dispose();
    }

    public static void loadBattleBg(String drawableName) {
        Texture backgroundBaseTex = new Texture(Gdx.files.internal("battleback/" + drawableName));
        backgroundBaseTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        skin.add("battleback", new TextureRegionDrawable(new TextureRegion(backgroundBaseTex)), Drawable.class);

        Texture inspirationTex = new Texture(Gdx.files.internal("battlehud/Inspiration.png"));
        inspirationTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        skin.add("inspired", new TextureRegionDrawable(new TextureRegion(inspirationTex)), Drawable.class);
    }
}
