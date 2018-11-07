package Skills;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.*;

import static org.dreambot.api.methods.MethodProvider.log;

public class Combat {
    AbstractScript s;

    // Areas
    Area chickensLumbridgeEast = new Area(3225, 3301, 3236, 3287);

    // Current state
    private State currentState;

    // Current enemy to kill
    private String currentEnemy;

    // Randomize counter
    private int randomizeCounter = 0;

    // Keep levels to determine enemies
    private int skillTreshold = 10;

    private int counter = 0;

    private enum State {
        RUN_ENEMY,
        ATTACK_ENEMY,
        RUN_BANK,
        BANK,
        BREAK,
        CHECK_ENEMY,
        CHECK_COMBATSTATE
    }

    public Combat (AbstractScript ctx) {
        this.s = ctx;
    }

    public void onStart() {

    }

    public int onLoop() {
        if (counter == 0) {
            // Run getState()
            currentState = getState();
            s.log("STATE ON BOOT: " + currentState);

            // Run roof check once
            if (s.getClientSettings().roofsEnabled()) {
                disableRoof();
            }
            counter++;
        }
        // Start running if energy is atleast 30
        startRunning();

        switch(currentState) {
            case CHECK_COMBATSTATE:
                if (s.getSkills().getRealLevel(Skill.ATTACK) == skillTreshold && s.getSkills().getRealLevel(Skill.STRENGTH) < skillTreshold && s.getSkills().getRealLevel(Skill.DEFENCE) < skillTreshold) {
                    // Select strength training
                    // Go to State.CHECK_ENEMY
                }
                if (s.getSkills().getRealLevel(Skill.ATTACK) == skillTreshold && s.getSkills().getRealLevel(Skill.STRENGTH) == skillTreshold && s.getSkills().getRealLevel(Skill.DEFENCE) < skillTreshold) {
                    // Select defense training
                    // Go to State.CHECK_ENEMY
                }
                if (s.getSkills().getRealLevel(Skill.ATTACK) < skillTreshold && s.getSkills().getRealLevel(Skill.STRENGTH) == skillTreshold && s.getSkills().getRealLevel(Skill.DEFENCE) == skillTreshold) {
                    // Select attack training
                    // Go to State.CHECK_ENEMY
                }
                if (s.getSkills().getRealLevel(Skill.ATTACK) < skillTreshold && s.getSkills().getRealLevel(Skill.STRENGTH) < skillTreshold && s.getSkills().getRealLevel(Skill.DEFENCE) == skillTreshold) {
                    // Select attack training
                    // Go to State.CHECK_ENEMY
                }
                if (s.getSkills().getRealLevel(Skill.ATTACK) == skillTreshold && s.getSkills().getRealLevel(Skill.STRENGTH) < skillTreshold && s.getSkills().getRealLevel(Skill.DEFENCE) == skillTreshold) {
                    // Select strength training
                    // Go to State.CHECK_ENEMY
                }
                break;
            case CHECK_ENEMY:
                // Skill treshold -- 10
                if (skillTreshold == 10) {
                    if (s.getSkills().getRealLevel(Skill.ATTACK) < skillTreshold || s.getSkills().getRealLevel(Skill.STRENGTH) < skillTreshold || s.getSkills().getRealLevel(Skill.DEFENCE) < skillTreshold) {
                        currentEnemy = "Chicken";
                        currentState = State.RUN_ENEMY;
                        s.log("CURRENT STATE: " + currentState);
                        break;
                    }
                }
                break;
            case RUN_ENEMY:
                switch(skillTreshold) {
                    case 10:
                        // Run chicken pen
                        if (chickensLumbridgeEast.contains(s.getLocalPlayer())) {
                            s.sleep(Calculations.random(700, 950));

                            break;
                        } else {
                            runToArea(chickensLumbridgeEast);
                            randomCameraMovement();
                        }
                        break;
                    case 20:
                        // Run giant rats
                        break;

                }
                break;
            case ATTACK_ENEMY:
                // Attack chickens
                String[] chickensLoot = new String[]{"Feathers"};
                attackEnemy("Chicken", chickensLoot);
                break;

        }

        return 600;
    }

    private State getState() {
        // Attack enemy
        return State.CHECK_COMBATSTATE;

    }

    // Disables the roof in advanced options
    private void disableRoof() {
        // Open options
        s.sleep(Calculations.random(700, 950));
        if (s.getTabs().openWithMouse(Tab.OPTIONS)) {
            s.sleepWhile(() -> !s.getTabs().isOpen(Tab.OPTIONS), Calculations.random(35000, 40000));
        }
        s.sleep(Calculations.random(700, 950));
        // Open advanced options
        if (!s.getWidgets().getWidgetChildrenContainingText("Advanced options").isEmpty()) {
            WidgetChild child = s.getWidgets().getWidgetChildrenContainingText("Advanced options").get(0);
            if (child != null) {
                child.interact();
            }
        }

        s.sleep(Calculations.random(700, 950));
        // Disable roof and exit settings
        s.getMouse().move(new Point(361,138));
        s.getMouse().click();

        s.sleep(Calculations.random(700, 950));
        s.getMouse().move(new Point(419,90));
        s.getMouse().click();
    }


    private void runToArea(Area area) {
        log("Running to area: " + area);
        if (s.getWalking().walk(area.getRandomTile())) {
            s.sleep(Calculations.random(1000, 2500));
        }
    }

    private void randomCameraMovement() {
        /// Random movements
        // Move the camera and mouse randomly
        randomizeCounter = Calculations.random(0, 8);
        if (randomizeCounter == Calculations.random(0, 8)) {
            log("Random camera rotation");
            s.getCamera().rotateTo(Calculations.random(2400), Calculations.random(s.getClient().getLowestPitch(), 384));
            s.sleep(Calculations.random(430,780));
        }
    }

    // Attack and loot enemies
    private void attackEnemy(String enemyName, String[] loot) {
        NPC enemy = s.getNpcs().closest(npc -> npc.getName().equals(enemyName) && !npc.isInCombat());
        if (enemy != null){
            if (enemy.interact("Attack")){
                randomCameraMovement();
                s.sleepUntil(() -> s.getLocalPlayer().getInteractingCharacter() != null,Calculations.random(2400, 3600));
                while (s.getLocalPlayer().getInteractingCharacter() != null) {
                    s.sleepUntil(() -> enemy.getInteractingCharacter() == null, Calculations.random(2400, 3600));
                }
            }
        }
        s.sleep(Calculations.random(700, 950));
        // Loot
        if (s.getLocalPlayer().getInteractingCharacter() == null) {
            GroundItem droppedItems = s.getGroundItems().closest(loot);
            if (droppedItems != null) {
                if (droppedItems.interact("Take")) {
                    s.sleepUntil(() -> s.getInventory().contains(loot), Calculations.random(2400, 3600));
                }
            }
        }
    }

    private void walkingSleep(){
        s.sleepUntil(() -> s.getLocalPlayer().isMoving(), Calculations.random(1200, 1600));
        s.sleepUntil(() -> !s.getLocalPlayer().isMoving(), Calculations.random(2400, 3600));
    }

    // Start running again if energy is atleast above 30
    private void startRunning() {
        if(!s.getWalking().isRunEnabled() && s.getWalking().getRunEnergy() > Calculations.random(30,50)){
            s.getWalking().toggleRun();
        }
    }
}
