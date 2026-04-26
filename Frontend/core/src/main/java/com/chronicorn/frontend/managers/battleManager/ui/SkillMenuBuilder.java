package com.chronicorn.frontend.managers.battleManager.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.skills.Skill;

public class SkillMenuBuilder {
    private static final int SKILL_BG_X = 765;
    private static final int SKILL_BG_Y = 105;
    private static final float MODIFIER = 0.5F;

    public interface Listener {
        void onSkillClicked(Skill skill, Group buttonGroup);
        boolean isSelected(Group buttonGroup);
    }

    public static float getButtonWidth() {
        return SKILL_BG_X * MODIFIER;
    }

    public static float getButtonHeight() {
        return SKILL_BG_Y * MODIFIER;
    }

    public static Group createSkillButton(final Skill skill, final Skin skin, final Listener listener) {
        final Group buttonGroup = new Group();

        // 1. Fixed-size green background
        String skillBgName = "skill_bg_" + skill.getElement().name().toLowerCase();
        Image bg = new Image(skin.getDrawable(skillBgName));
        bg.setPosition(0, 0);
        bg.setSize(SKILL_BG_X * MODIFIER, SKILL_BG_Y * MODIFIER);

        // Disable touch on the image so the Group handles the hover entirely
        bg.setTouchable(Touchable.disabled);
        buttonGroup.addActor(bg);

        // 2. Skill Name
        Label nameLabel = new Label(skill.getName(), skin, "default");
        float textX = (SKILL_BG_X * MODIFIER) - nameLabel.getWidth() - 75; // Adjust 75 to clear your right-side icon
        float textY = ((SKILL_BG_Y * MODIFIER) / 2f) - (nameLabel.getHeight() / 2f);

        nameLabel.setPosition(textX, textY);

        // Disable touch on the label to prevent hover glitching
        nameLabel.setTouchable(Touchable.disabled);
        buttonGroup.addActor(nameLabel);

        buttonGroup.setSize(SKILL_BG_X * MODIFIER, SKILL_BG_Y * MODIFIER);

        // 3. Set Origin for Scaling
        // Align.right pushes the scale outward to the left, keeping it anchored to the right menu edge
        buttonGroup.setOrigin(Align.right);

        // 4. Add Hover Actions
        buttonGroup.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    listener.onSkillClicked(skill, buttonGroup);
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                // pointer == -1 ensures this only triggers on mouse movement, not clicks
                if (pointer == -1) {
                    buttonGroup.clearActions();
                    buttonGroup.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f, Interpolation.fade));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1 && (listener == null || !listener.isSelected(buttonGroup))) {
                    buttonGroup.clearActions();
                    buttonGroup.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f, Interpolation.fade));
                }
            }
        });

        return buttonGroup;
    }
}
