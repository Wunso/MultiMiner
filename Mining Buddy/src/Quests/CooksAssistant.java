package Quests;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.*;

import static org.dreambot.api.methods.MethodProvider.log;

public class CooksAssistant {
    AbstractScript s;

    public CooksAssistant (AbstractScript ctx) {
        this.s = ctx;
    }

    // Current state
    private State currentState;

    // Randomize counter
    private int randomizeCounter = 0;

    // Cook's Assistant Areas
    Area CookArea = new Area(3206, 3217, 3211, 3213, 0);

    // Quest config id
    private final int QUEST_PROG = 29;

    // Item tracking booleans
    private boolean hasAlreadyRetrievedMilk;
    private boolean hasAlreadyRetrievedFlour;
    private boolean hasAlreadyRetrievedEgg;

    // Cook's Assistant completed (default false)
    private boolean cooksAssistantCompleted = false;

    private int counter = 0;

    private enum State {
        START_QUEST,
        RUN_COOK,
        CHECK_ITEMS,
        //GET_BUCKET,
        GET_MILK,
        RUN_COWS,
        GET_EGG,
        RUN_CHICKENS,
        GET_WHEAT,
        RUN_FIELD,
        RUN_MILL,
        ASCEND_LADDER,
        GRAIN_TO_HOPPER,
        HOPPER_CONTROLS,
        DESCEND_LADDER,
        GET_FLOUR,
        HAND_IN_ITEMS,
        COMPLETE
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

        switch(currentState) {
            case START_QUEST:
                // Start the quest
                s.sleep(Calculations.random(700, 950));
                if (s.getDialogues().getOptions() == null){
                    talkTo("Cook");
                } else {
                    s.getDialogues().chooseOption(s.getDialogues().getOptionIndexContaining("I know where to") > -1 ? 4 : 1);
                }
                if (s.getPlayerSettings().getConfig(QUEST_PROG) == 1) {
                    currentState = State.CHECK_ITEMS;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case CHECK_ITEMS:
                // Check what item to get next
                s.sleep(Calculations.random(700, 950));
                int conf = s.getPlayerSettings().getConfig(QUEST_PROG);
                switch(conf) {
                    case 0:
                        currentState = State.RUN_COOK;
                        s.log("CURRENT STATE: " + currentState);
                        break;
                    case 1:
                        hasAlreadyRetrievedMilk = s.getInventory().contains("Bucket of milk");
                        hasAlreadyRetrievedEgg = s.getInventory().contains("Egg");
                        hasAlreadyRetrievedFlour = s.getInventory().contains("Pot of flour");

                        if (!hasAlreadyRetrievedMilk && !hasAlreadyRetrievedEgg && !hasAlreadyRetrievedFlour) {
                            currentState = State.RUN_COWS;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                        if (hasAlreadyRetrievedMilk && !hasAlreadyRetrievedEgg && !hasAlreadyRetrievedFlour) {
                            currentState = State.RUN_CHICKENS;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                        if (hasAlreadyRetrievedMilk && hasAlreadyRetrievedEgg && !hasAlreadyRetrievedFlour) {
                            if (!s.getInventory().contains("Grain")) {
                                currentState = State.RUN_FIELD;
                                s.log("CURRENT STATE: " + currentState);
                                break;
                            } else {
                                currentState = State.RUN_MILL;
                                s.log("CURRENT STATE: " + currentState);
                                break;
                            }
                        }
                        if (!hasAlreadyRetrievedMilk && hasAlreadyRetrievedEgg && !hasAlreadyRetrievedFlour) {
                            currentState = State.RUN_COWS;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                        if (hasAlreadyRetrievedMilk && hasAlreadyRetrievedEgg && hasAlreadyRetrievedFlour) {
                            currentState = State.RUN_COOK;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                        break;
                }
            /*case GET_BUCKET:
                // Get bucket
                if (!hasAlreadyRetrievedMilk) {
                    if (s.getLocalPlayer().getTile().getY() < 9000) {
                        if (CookArea.contains(s.getLocalPlayer())) {
                            GameObject trapDoor = s.getGameObjects().closest("Trapdoor");
                            if (trapDoor != null) {
                                if (trapDoor.isOnScreen()) {
                                    if (trapDoor.interact("Climb-down")) {
                                        walkingSleep();
                                        s.sleepUntil(() -> s.getLocalPlayer().getTile().getY() > 9000, Calculations.random(1400, 1800));
                                    }
                                } else {
                                    s.getCamera().rotateToEntityEvent(trapDoor);
                                }
                            }
                        } else {
                            runToArea(CookArea);
                            randomCameraMovement();
                        }
                    } else {
                        // Take bucket
                        if (s.getLocalPlayer().getTile().distance(new Tile(3216, 9624, 0)) > 1) {
                            s.getWalking().walk(new Tile(3216, 9624, 0));
                            walkingSleep();
                        } else {
                            GroundItem bucket = s.getGroundItems().closest("Bucket");
                            if (bucket != null) {
                                if (bucket.isOnScreen()) {
                                    if (bucket.interact("Take")) {
                                        s.sleepUntil(() -> s.getInventory().contains("Bucket"), Calculations.random(1400, 1800));
                                        currentState = State.RUN_COWS;
                                        s.log("CURRENT STATE: " + currentState);
                                        break;
                                    }
                                } else {
                                    s.getCamera().rotateToEntityEvent(bucket);
                                }
                            }
                        }
                    }
                }
                break;*/
            case GET_MILK:
                // take milk from cow
                if (!s.getInventory().contains("Bucket of milk")) {
                    if (s.getLocalPlayer().getTile().getY() > 9000) {
                        // Climb ladder
                        GameObject ladder = s.getGameObjects().closest("Ladder");
                        if (ladder != null) {
                            if (ladder.isOnScreen()) {
                                if (ladder.interact("Climb-up")) {
                                    s.sleepUntil(() -> s.getLocalPlayer().getTile().getY() < 9000, Calculations.random(1400, 1800));
                                }
                            } else {
                                s.getCamera().rotateToEntityEvent(ladder);
                            }
                        }
                    } else {
                        if (!s.getInventory().contains("Bucket of milk")) {
                            // Get milk
                            GameObject dairyCow = s.getGameObjects().closest("Dairy cow");
                            if (dairyCow != null) {
                                if (dairyCow.isOnScreen()) {
                                    if (dairyCow.interact("Milk")) {
                                        s.sleepUntil(() -> s.getInventory().contains("Bucket of Milk") && s.getLocalPlayer().getAnimation() == -1, Calculations.random(1400, 1800));
                                        currentState = State.CHECK_ITEMS;
                                        s.log("CURRENT STATE: " + currentState);
                                        break;
                                    }
                                } else {
                                    s.getCamera().rotateToEntityEvent(dairyCow);
                                }
                            }
                        }
                    }
                }
                break;
            case RUN_COWS:
                // Run to dairy cows
                if (s.getLocalPlayer().getTile().distance(new Tile(3255, 3272,0)) > 1){
                    s.getWalking().walk(new Tile(3255, 3272,0));
                    walkingSleep();
                } else {
                    currentState = State.GET_MILK;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case RUN_CHICKENS:
                // Run to chicken pen
                if (s.getLocalPlayer().getTile().distance(new Tile(3226, 3300,0)) > 1){
                    s.getWalking().walk(new Tile(3226, 3300,0));
                    walkingSleep();
                } else {
                    currentState = State.GET_EGG;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case GET_EGG:
                // Get egg
                GroundItem egg = s.getGroundItems().closest("Egg");
                if (egg != null) {
                    if (egg.isOnScreen()) {
                        if (egg.interact("Take")) {
                            s.sleepUntil(() -> s.getInventory().contains("Egg"), Calculations.random(1400, 1800));
                            currentState = State.CHECK_ITEMS;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                    } else {
                        s.getCamera().rotateToEntityEvent(egg);
                    }
                }
                break;
            case RUN_FIELD:
                // Run to wheat field
                if (s.getLocalPlayer().getTile().distance(new Tile(3161, 3291, 0)) > 1){
                    s.getWalking().walk(new Tile(3161, 3291, 0));
                    walkingSleep();
                } else {
                    currentState = State.GET_WHEAT;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case GET_WHEAT:
                // Take grain
                GameObject wheat = s.getGameObjects().closest("Wheat");
                if (wheat != null) {
                    if (wheat.isOnScreen()) {
                        if (wheat.interact("Pick")) {
                            s.sleepUntil(() -> s.getInventory().contains("Grain"), Calculations.random(1400, 1800));
                            currentState = State.CHECK_ITEMS;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                    } else {
                        s.getCamera().rotateToEntityEvent(wheat);
                    }
                }
                break;
            case RUN_MILL:
                // Run to mill
                if (s.getLocalPlayer().getTile().distance(new Tile(3166, 3305, 0)) > 5){
                    s.getWalking().walk(new Tile(3166, 3305, 0));
                    walkingSleep();
                } else {
                    currentState = State.ASCEND_LADDER;
                    s.log("currentState: " + currentState);
                    break;
                }
                break;
            case ASCEND_LADDER:
                // Climb ladder
                if (s.getLocalPlayer().getTile().getZ() != 2) {
                    GameObject ladder = s.getGameObjects().closest("Ladder");
                    if (ladder != null) {
                        if (ladder.isOnScreen()) {
                            if (ladder.interact("Climb-up")) {
                                s.sleepUntil(() -> s.getLocalPlayer().getAnimation() == -1 || !s.getLocalPlayer().isMoving(), Calculations.random(1400, 1800));
                            }
                        } else {
                            s.getCamera().rotateToEntityEvent(ladder);
                        }
                    }
                } else {
                    currentState = State.GRAIN_TO_HOPPER;
                    s.log("currentState: " + currentState);
                    break;
                }
                break;
            case GRAIN_TO_HOPPER:
                // Put grain in hopper
                s.sleep(Calculations.random(700, 950));
                if (!s.getInventory().isItemSelected()) {
                    if (s.getInventory().interact("Grain", "Use")) {
                        s.sleepUntil(() -> s.getInventory().isItemSelected(), Calculations.random(1200, 1400));
                    }
                } else {
                    GameObject hopper = s.getGameObjects().closest("Hopper");
                    if (hopper.interact("Use")) {
                        walkingSleep();
                        s.sleepUntil(() -> s.getLocalPlayer().getAnimation() == -1, Calculations.random(2000, 3000));
                        currentState = State.HOPPER_CONTROLS;
                        s.log("currentState: " + currentState);
                        break;
                    }
                }
                break;
            case HOPPER_CONTROLS:
                // Operate hopper controls
                GameObject hopperControls = s.getGameObjects().closest("Hopper controls");
                if (hopperControls != null) {
                    if (hopperControls.isOnScreen()) {
                        if (hopperControls.interact("Operate")) {
                            walkingSleep();
                            s.sleepUntil(() -> s.getLocalPlayer().getAnimation() == -1, Calculations.random(2000, 3000));
                            currentState = State.DESCEND_LADDER;
                            s.log("currentState: " + currentState);
                            break;
                        }
                    } else {
                        s.getCamera().rotateToEntityEvent(hopperControls);
                    }
                }
                break;
            case DESCEND_LADDER:
                // Descend ladder
                if (s.getLocalPlayer().getTile().getZ() != 0) {
                    GameObject ladder = s.getGameObjects().closest("Ladder");
                    if (ladder != null) {
                        if (ladder.isOnScreen()) {
                            if (ladder.interact("Climb-down")) {
                                s.sleepUntil(() -> s.getLocalPlayer().getAnimation() == -1, Calculations.random(1400, 1800));
                            }
                        } else {
                            s.getCamera().rotateToEntityEvent(ladder);
                        }
                    }
                } else {
                    currentState = State.GET_FLOUR;
                    s.log("currentState: " + currentState);
                    break;
                }
                break;
            case GET_FLOUR:
                // Put flour in pot
                s.sleep(Calculations.random(700, 950));
                if (!s.getInventory().isItemSelected()) {
                    if (s.getInventory().interact("Pot", "Use")) {
                        s.sleepUntil(() -> s.getInventory().isItemSelected(), Calculations.random(1200, 1400));
                    }
                } else {
                    GameObject flourBin = s.getGameObjects().closest("Flour bin");
                    if (flourBin.interact("Use")) {
                        walkingSleep();
                        s.sleepUntil(() -> s.getInventory().contains("Pot of flour"), Calculations.random(2000, 3000));
                        currentState = State.CHECK_ITEMS;
                        s.log("currentState: " + currentState);
                        break;
                    }
                }
                break;
            case RUN_COOK:
                // Run to cook
                if (CookArea.contains(s.getLocalPlayer())) {
                    if (s.getPlayerSettings().getConfig(29) == 0) {
                        currentState = State.START_QUEST;
                        s.log("currentState: " + currentState);
                        break;
                    }
                    if (s.getPlayerSettings().getConfig(29) == 1) {
                        currentState = State.HAND_IN_ITEMS;
                        s.log("currentState: " + currentState);
                        break;
                    }
                } else {
                    runToArea(CookArea);
                    randomCameraMovement();
                }


            case HAND_IN_ITEMS:
                // Complete the quest
                s.sleep(Calculations.random(700, 950));
                if (s.getDialogues().getOptions() == null){
                    talkTo("Cook");
                }

                s.sleep(Calculations.random(700, 950));
                Widget questComplete = s.getWidgets().getWidget(277);
                if (questComplete != null && questComplete.isVisible()) {
                    questComplete.getChild(15).interact();
                    currentState = State.COMPLETE;
                    s.log("currentState: " + currentState);
                    break;
                }
                break;
            case COMPLETE:
                cooksAssistantCompleted = true;
                s.log("Cook's Assistant successfully completed!");
        }
        return 600;
    }

    private void talkTo(String name) {
        if(!s.getDialogues().canContinue()){
            final NPC guide = s.getNpcs().closest(name);
            if(guide != null){
                if(guide.isOnScreen()){
                    if(guide.interact("Talk-to")){
                        walkingSleep();
                        s.sleepUntil(() -> s.getDialogues().canContinue(),Calculations.random(1200,1600));
                    }
                }
                else{
                    s.getWalking().walk(guide);
                    walkingSleep();
                }
            }
        } else {
            log("Clicking continue");
            s.getDialogues().clickContinue();
            s.sleep(600, 900);
        }
    }

    private State getState() {
        if (s.getPlayerSettings().getConfig(QUEST_PROG) == 1) {
            return State.CHECK_ITEMS;
        }
        if (s.getPlayerSettings().getConfig(QUEST_PROG) == 2) {
            return State.COMPLETE;
        }
        return State.RUN_COOK;
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

    private void walkingSleep(){
        s.sleepUntil(() -> s.getLocalPlayer().isMoving(), Calculations.random(1200, 1600));
        s.sleepUntil(() -> !s.getLocalPlayer().isMoving(), Calculations.random(2400, 3600));
    }

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

    public boolean isCooksAssistantCompleted() {
        return cooksAssistantCompleted;
    }
}
