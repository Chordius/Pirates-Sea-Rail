package com.chronicorn.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.enemies.Pirate;
import com.chronicorn.frontend.battlers.parties.Deal;
import com.chronicorn.frontend.battlers.parties.Porter;
import com.chronicorn.frontend.battlers.parties.Reyna;
import com.chronicorn.frontend.battlers.parties.Sailor;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.animationManager.ActionSequenceProcessor;
import com.chronicorn.frontend.managers.animationManager.AnimationManager;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.managers.battleManager.mechanics.TargetingLogic;
import com.chronicorn.frontend.managers.battleManager.ui.BackgroundBuilder;
import com.chronicorn.frontend.managers.battleManager.ui.ActorCardUI;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.ui.EnemyWidget;
import com.chronicorn.frontend.managers.battleManager.ui.SkillMenuBuilder;
import com.chronicorn.frontend.skills.Action;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.SkillDatabase;
import com.chronicorn.frontend.statuseffect.StatusEffectDatabase;

import java.util.ArrayList;

public class BattleScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private BattleManager battleManager;
    private ActionSequenceProcessor sequenceProcessor;
    private Array<Battler> enemies;

    // Viewport dimensions for calculating positions
    private final float VIEWPORT_WIDTH = Gdx.graphics.getWidth();
    private final float VIEWPORT_HEIGHT = Gdx.graphics.getHeight();

    // UI Containers
    private Group worldLayer;
    private Group enemyLayer;
    private Group playerVFXLayer;
    private Table partyUIContainer;
    private Table skillMenuContainer;
    private IntArray pendingIndices = new IntArray();
    private int pendingPrimaryIndex = -1;
    private Array<ActorCardUI> actorCards;
    private Array<EnemyWidget> enemyWidgets;
    private Group selectedSkillBtn = null;

    // Boolean
    private boolean isInputWindowActive = false;
    private boolean isSelectingTarget = false;
    private AnimationManager animationManager;
    private Action executingAction;

    public BattleScreen() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Global Right-Click Listener to cancel skills
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.RIGHT && isSelectingTarget) {
                    cancelSkillSelection();
                    return true;
                }
                return false;
            }
        });

        skin = ImageManager.skin;

        initializeBattle();
        buildUI();
    }

    private void initializeBattle() {
        SkillDatabase.init();
        StatusEffectDatabase.init();
        ArrayList<Battler> allBattlers = new ArrayList<>();
        enemies = new Array<>();

        allBattlers.add(new Sailor());
        allBattlers.add(new Porter());
        allBattlers.add(new Reyna());
        allBattlers.add(new Deal());

        for (Battler b : allBattlers) {
            ((Actor) b).changeLevel(10);
        }

        Enemy enemy1 = new Pirate();
        Enemy enemy2 = new Pirate();
        Enemy enemy3 = new Pirate();
        Enemy enemy4 = new Pirate();
        allBattlers.add(enemy1);
        allBattlers.add(enemy2);
        allBattlers.add(enemy3);
        allBattlers.add(enemy4);
        enemies.add(enemy1, enemy2, enemy3, enemy4);

        battleManager = new BattleManager(allBattlers);

        ArrayList<Actor> gameParty = battleManager.getGameParty();
        ArrayList<Enemy> gameTroop = battleManager.getGameTroop();
        ImageManager.loadBattleAssets(gameParty, gameTroop);
    }

    private void buildUI() {
        ImageManager.loadBattleBg("village.png");

        worldLayer = new Group();
        worldLayer.setSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        worldLayer.setOrigin(Align.center); // Ensures scaling happens from the center
        stage.addActor(worldLayer);

        // BACKGROUND LAYER (Added first so it renders at the very back)
        Image background = new BackgroundBuilder()
            .setTexture("battleback")
            .setSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
            .build(skin);
        worldLayer.addActor(background);

        Image inspiration = new Image(skin.getDrawable("inspired"));
        inspiration.setTouchable(Touchable.disabled);
        stage.addActor(inspiration);

        // ENEMY LAYER (Added second so it renders over the background, but behind UI)
        enemyLayer = new Group();
        worldLayer.addActor(enemyLayer);
        renderEnemies();

        actorCards = new Array<>();
        // 1. Party UI Container
        partyUIContainer = new Table();
        partyUIContainer.bottom().left().padLeft(47).padBottom(37);
        partyUIContainer.setFillParent(true);

        for (Battler b : battleManager.getAllBattlers()) {
            if (b instanceof Actor) {
                ActorCardUI card = new ActorCardUI(b, skin, new ActorCardUI.Listener() {
                    @Override
                    public void onAllyClicked(Battler clickedAlly) {
                        if (isSelectingTarget) {
                            onAllyOk(clickedAlly);
                        }
                    }

                    @Override
                    public void onAllyHovered(ActorCardUI hoveredCard) {
                        if (isSelectingTarget) {
                            setHoveredAllyTarget(hoveredCard);
                        }
                    }
                });
                actorCards.add(card);
                partyUIContainer.add(card).padRight(47);
            }
        }
        stage.addActor(partyUIContainer);

        playerVFXLayer = new Group();
        stage.addActor(playerVFXLayer);

        animationManager = new AnimationManager(enemyLayer, playerVFXLayer, enemyWidgets, actorCards);
        sequenceProcessor = new ActionSequenceProcessor(battleManager, animationManager, new ActionSequenceProcessor.SequenceEventListener() {
            @Override
            public void onCameraZoom(float targetScale, float duration) {
                zoomWorld(targetScale, duration);
            }

            @Override
            public void onCameraReset() {
                // Default back to 1.0x scale over a standard 0.3 seconds
                resetWorldZoom();
            }
        });

        // 2. Skill Menu Container
        skillMenuContainer = new Table();
        skillMenuContainer.bottom().right().padBottom(24);
        skillMenuContainer.setFillParent(true);
        skillMenuContainer.setVisible(false);
        stage.addActor(skillMenuContainer);
    }

    // --- UI FLOW METHODS ---

    private void startActorCommandSelection() {
        isInputWindowActive = true;
        Battler activeActor = battleManager.getActiveBattler();

        // Initialize the empty Action container in the Battler
        activeActor.inputtingAction();
        updateActorCardVisuals(activeActor);
        showSkillMenu(activeActor);
    }

    private void updateActorCardVisuals(Battler activeActor) {
        for (ActorCardUI card : actorCards) {
            // Compare the card's battler to the manager's active battler
            card.setActive(card.getBattler() == activeActor);
        }
    }

    private void showSkillMenu(Battler activeActor) {
        skillMenuContainer.clearChildren();

        Array<Skill> iterableSkills = activeActor.getSkills();
        if (iterableSkills.size >= 3) {
            iterableSkills.setSize(3);
        }

        for (final Skill skill : iterableSkills) {
            Group skillBtn = SkillMenuBuilder.createSkillButton(skill, skin, new SkillMenuBuilder.Listener() {
                @Override
                public void onSkillClicked(Skill clickedSkill, Group buttonGroup) {
                    onSkillOk(clickedSkill, buttonGroup);
                }

                @Override
                public boolean isSelected(Group buttonGroup) {
                    return selectedSkillBtn == buttonGroup;
                }
            });

            skillMenuContainer
                .add(skillBtn)
                .width(SkillMenuBuilder.getButtonWidth())
                .height(SkillMenuBuilder.getButtonHeight())
                .padBottom(0)
                .row();
        }
        skillMenuContainer.setVisible(true);
    }

    private void onSkillOk(Skill selectedSkill, Group skillBtn) {
        // If a previous skill was selected, scale it back down to 1.0f
        if (selectedSkillBtn != null) {
            selectedSkillBtn.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f));
        }

        selectedSkillBtn = skillBtn;

        Action action = battleManager.getActiveBattler().inputtingAction();
        action.setSkill(selectedSkill);

        // DO NOT hide the skill menu here. It must remain visible so the player can click another skill.
        onSelectAction();
    }

    private void onSelectAction() {
        Action action = battleManager.getActiveBattler().inputtingAction();
        TargetScope scope = action.getSkill().getScope();

        if (!action.needsSelection()) {
            action.setPrimaryTarget(battleManager.getActiveBattler());
            // Since there is no target selection, hide the menu and submit
            skillMenuContainer.setVisible(false);
            submitCommand();
        } else if (scope == TargetScope.ALLY || scope == TargetScope.ALLIES) {
            showAllySelection();
        } else {
            showEnemySelection();
        }
    }

    private void showEnemySelection() {
        // FLOW STEP 3: Enable enemy selection and apply default reticle
        isSelectingTarget = true;

        for (EnemyWidget w : enemyWidgets) {
            w.setTouchable(Touchable.enabled);
        }

        // Apply reticle to the middle enemy by default
        if (enemyWidgets.size > 0) {
            setHoveredTarget(enemyWidgets.get(enemyWidgets.size / 2));
        }
    }

    public void setHoveredTarget(EnemyWidget primaryWidget) {
        // 1. Clear UI visuals
        for (EnemyWidget w : enemyWidgets) w.setTargeted(false);

        // 2. Pure UI Logic: Find where we are in the array
        pendingPrimaryIndex = enemyWidgets.indexOf(primaryWidget, true);
        Skill selectedSkill = battleManager.getActiveBattler().inputtingAction().getSkill();

        // 3. Get the "Hit Map" from Business Logic
        pendingIndices = TargetingLogic.getTargetIndices(pendingPrimaryIndex, enemyWidgets.size, selectedSkill.getScope());

        for (int i = 0; i < enemyWidgets.size; i++) {
            EnemyWidget widget = enemyWidgets.get(i);
            boolean isPrimary = (i == pendingPrimaryIndex);
            boolean isSecondary = pendingIndices.contains(i) && !isPrimary;
            widget.setTargeted(isPrimary, isSecondary);
        }

        // NEW: Pan the camera (move the world layer)
        float screenCenterX = VIEWPORT_WIDTH / 2f;
        float widgetCenterX = primaryWidget.getX() + (primaryWidget.getWidth() / 2f);

        // Calculate how far off-center the enemy is, and multiply by a factor (e.g., 0.2f)
        // to prevent panning completely off the screen.
        float offset = (screenCenterX - widgetCenterX) * 0.1f;

        // Clear previous panning actions and apply the new one
        worldLayer.clearActions();
        worldLayer.addAction(Actions.moveTo(offset, 0, 0.3f, Interpolation.pow2Out));
    }

    public void onEnemyOk(Battler selectedEnemy) {
        isSelectingTarget = false;

        // 5. UI finally speaks to the Logic/Action
        Action currentAction = battleManager.getActiveBattler().inputtingAction();

        currentAction.setPrimaryTarget(selectedEnemy); // Direct speak

        for (EnemyWidget w : enemyWidgets) {
            w.setTouchable(Touchable.disabled);
            w.setTargeted(false);
            w.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f));
        }
        skillMenuContainer.setVisible(false);

        // Clean up UI
        submitCommand();
    }

    private void showAllySelection() {
        isSelectingTarget = true;

        // Enable clicks on the party UI
        for (ActorCardUI card : actorCards) {
            card.setTouchable(Touchable.enabled);
        }

        // Apply reticle to the active actor by default
        if (actorCards.size > 0) {
            Battler active = battleManager.getActiveBattler();
            for (ActorCardUI card : actorCards) {
                if (card.getBattler() == active) {
                    setHoveredAllyTarget(card);
                    break;
                }
            }
        }
    }

    public void setHoveredAllyTarget(ActorCardUI primaryCard) {
        // 1. Clear visuals
        for (ActorCardUI card : actorCards) card.setTargeted(false, false);

        // 2. Find index
        int primaryIndex = actorCards.indexOf(primaryCard, true);
        Skill selectedSkill = battleManager.getActiveBattler().inputtingAction().getSkill();

        // 3. Get Hit Map
        IntArray hitMap = TargetingLogic.getTargetIndices(primaryIndex, actorCards.size, selectedSkill.getScope());

        for (int i = 0; i < actorCards.size; i++) {
            boolean isPrimary = (i == primaryIndex);
            boolean isSecondary = hitMap.contains(i) && !isPrimary;
            actorCards.get(i).setTargeted(isPrimary, isSecondary);
        }
    }

    public void onAllyOk(Battler selectedAlly) {
        isSelectingTarget = false;

        Action currentAction = battleManager.getActiveBattler().inputtingAction();
        currentAction.setPrimaryTarget(selectedAlly);

        for (ActorCardUI card : actorCards) {
            card.setTouchable(Touchable.disabled);
            card.setTargeted(false, false);
        }

        skillMenuContainer.setVisible(false);
        submitCommand();
    }

    private void cancelSkillSelection() {
        // Revert to the state before the skill was selected
        isSelectingTarget = false;

        if (selectedSkillBtn != null) {
            selectedSkillBtn.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f));
            selectedSkillBtn = null;
        }

        for (EnemyWidget w : enemyWidgets) {
            w.setTouchable(Touchable.disabled);
            w.setTargeted(false);
        }

        for (ActorCardUI card : actorCards) {
            card.setTouchable(Touchable.disabled);
            card.setTargeted(false, false);
        }

        worldLayer.clearActions();
        worldLayer.addAction(Actions.moveTo(0, 0, 0.3f, Interpolation.pow2Out));
        battleManager.getActiveBattler().clearAction();
        battleManager.getActiveBattler().inputtingAction();
    }

    public void submitCommand() {
        // Clean up UI scales and touches before sending the action to the manager
        if (selectedSkillBtn != null) {
            selectedSkillBtn.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f));
            selectedSkillBtn = null;
        }

        for (EnemyWidget w : enemyWidgets) {
            w.setTouchable(Touchable.disabled);
            w.setTargeted(false);
            w.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f));
        }

        Battler activeActor = battleManager.getActiveBattler();
        Action finalAction = activeActor.inputtingAction();

        battleManager.submitAction(finalAction);

        activeActor.clearAction();
        isInputWindowActive = false;
    }

    // Helper & Renderers

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        battleManager.update(delta);

        if (battleManager.getCurrentState() == BattleManager.TurnState.INPUT
            && battleManager.getActiveBattler().isPlayerControlled()) {

            if (!isInputWindowActive) {
                startActorCommandSelection();
            }
        } else if (battleManager.getCurrentState() == BattleManager.TurnState.EXECUTE_ACTION) {
            Action selectedAction = battleManager.getSelectedAction();
            if (selectedAction != null && selectedAction != executingAction) {
                sequenceProcessor.startSequence(selectedAction.getSkill().getActionSequence());
                executingAction = selectedAction;
            }
            sequenceProcessor.update(delta);
        } else {
            executingAction = null;
        }

        stage.act(delta);
        stage.draw();
    }

    private void renderEnemies() {
        enemyLayer.clearChildren();

        // Ensure the array is initialized
        if (enemyWidgets == null) {
            enemyWidgets = new Array<>();
        }
        enemyWidgets.clear();

        for (int i = 0; i < enemies.size; i++) {
            final Battler enemy = enemies.get(i);

            if (enemy.isAlive()) {
                EnemyWidget currentWidget = getEnemyWidget((Enemy) enemy, 0, 0);;
                currentWidget.setTouchable(Touchable.disabled);

                // Add the widget to the tracking array
                enemyWidgets.add(currentWidget);
                enemyLayer.addActor(currentWidget);
            }
        }

        updateEnemyLayout(false, 0f);
    }

    private EnemyWidget getEnemyWidget(Enemy enemy, float startX, float fixedY) {
        EnemyWidget currentWidget = new EnemyWidget(enemy, skin, new EnemyWidget.Listener() {
            @Override
            public void onEnemyClicked(Enemy clickedEnemy) {
                if (isSelectingTarget()) {
                    onEnemyOk(clickedEnemy);
                }
            }

            @Override
            public void onEnemyHovered(EnemyWidget widget) {
                if (isSelectingTarget()) {
                    setHoveredTarget(widget);
                }
            }

            @Override
            public void onEnemyDied(EnemyWidget widget) {
                processEnemyDeath(widget);
            }
        });

        currentWidget.setPosition(startX, fixedY);
        return currentWidget;
    }

    public boolean isSelectingTarget() {
        return isSelectingTarget;
    }

    public void processEnemyDeath(final EnemyWidget deadWidget) {
        // 1. Remove from UI tracking arrays so targeting ignores them immediately
        enemyWidgets.removeValue(deadWidget, true);
        enemies.removeValue(deadWidget.getEnemy(), true);

        // 2. Disable interactions and clear reticles
        deadWidget.setTouchable(Touchable.disabled);
        deadWidget.setTargeted(false, false);

        // 3. Vanish Animation (Shrink and Fade Out)
        deadWidget.clearActions();
        deadWidget.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeOut(0.5f)
            ),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    deadWidget.remove(); // Removes it from the Stage layer
                }
            })
        ));

        updateEnemyLayout(true, 0.4f);
    }

    private void updateEnemyLayout(boolean animate, float delay) {
        float fixedY = VIEWPORT_HEIGHT * 0.4f;
        float padding = 10f;

        float totalWidth = 0;
        for (EnemyWidget widget : enemyWidgets) {
            float trueWidth = Math.max(widget.getWidth(), 185.5f);
            totalWidth += trueWidth;
        }

        if (enemyWidgets.size > 1) {
            totalWidth += padding * (enemyWidgets.size - 1);
        }

        float currentX = (VIEWPORT_WIDTH * 0.5f) - (totalWidth * 0.5f);

        for (EnemyWidget widget : enemyWidgets) {
            if (animate) {
                widget.addAction(Actions.sequence(
                    Actions.delay(delay),
                    Actions.moveTo(currentX, fixedY, 0.4f, Interpolation.pow2Out)
                ));
            } else {
                widget.setPosition(currentX, fixedY);
            }

            float trueWidth = Math.max(widget.getWidth(), 185.5f);
            currentX += trueWidth + padding;
        }
    }

    public void zoomWorld(float targetScale, float duration) {
        if (worldLayer != null) {
            worldLayer.addAction(Actions.scaleTo(targetScale, targetScale, duration, Interpolation.pow2Out));
            worldLayer.addAction(Actions.moveBy(0, (int) (VIEWPORT_HEIGHT * ((1 - targetScale) * 2 / 3)), duration, Interpolation.pow2Out));
        }
    }

    public void resetWorldZoom() {
        worldLayer.clearActions();
        worldLayer.addAction(Actions.moveTo(0, 0, 0.3f, Interpolation.pow2Out));
        worldLayer.addAction(Actions.scaleTo(1, 1, 0.3f, Interpolation.pow2Out));
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (enemyWidgets != null) {
            for (EnemyWidget w : enemyWidgets) {
                w.dispose();
            }
        }

        if (actorCards != null) {
            for (ActorCardUI card : actorCards) {
                card.dispose();
            }
        }

        AnimationManager.dispose();
        stage.dispose();
        // Assuming ImageManager.dispose() handles skin disposal globally
    }
}
