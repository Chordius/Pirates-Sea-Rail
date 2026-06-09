package com.chronicorn.frontend.managers.battleManager.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.statuseffect.StatusEffect;

public class StatusEffectTooltip extends Table {

    public StatusEffectTooltip(StatusEffect state, Skin skin) {
        super();
        this.setBackground(skin.newDrawable("white-pixel", new Color(0.08f, 0.08f, 0.08f, 0.95f)));
        this.pad(12);
        this.defaults().align(Align.left).space(4);

        // Name
        Label nameLbl = new Label(state.getName(), skin, "menu2");
        nameLbl.setColor(Color.GOLD);
        this.add(nameLbl).left().row();

        // Separator line
        Table separator = new Table();
        separator.setBackground(skin.newDrawable("white-pixel", Color.valueOf("aa9573")));
        this.add(separator).height(1).fillX().padTop(2).padBottom(4).row();

        // Info: Turns & Stacks
        Table infoTable = new Table();
        infoTable.defaults().align(Align.left).space(10);
        
        Label turnsLbl = new Label("Turns Left: " + state.getTurns(), skin, "menu");
        turnsLbl.setFontScale(0.85f);
        turnsLbl.setColor(Color.LIGHT_GRAY);
        infoTable.add(turnsLbl);

        // Only display Stack count if the status effect explicitly uses/stores stacks
        if (state.hasSavedValue("stacks")) {
            float stacks = state.getSavedValue("stacks");
            Label stacksLbl = new Label("Stacks: " + (int) stacks, skin, "menu");
            stacksLbl.setFontScale(0.85f);
            stacksLbl.setColor(Color.LIGHT_GRAY);
            infoTable.add(stacksLbl);
        }
        
        this.add(infoTable).fillX().padBottom(4).row();

        // Description
        Label descLbl = new Label(state.getDescription(), skin, "menu");
        descLbl.setFontScale(0.85f);
        descLbl.setWrap(true);
        descLbl.setColor(Color.WHITE);
        this.add(descLbl).width(220).row();

        this.pack();
    }
}
