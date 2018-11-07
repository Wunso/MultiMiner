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

public class WitchsPotion {
    AbstractScript s;

    public WitchsPotion (AbstractScript ctx) {
        this.s = ctx;
    }

    // Current state
    private State currentState;

    // Quest progress id
    private final int QUEST_PROG = 67;

    // Randomize counter
    private int randomizeCounter = 0;

    // Item booleans
    private boolean hasOnion = false;
    private boolean hasEyeOfNewt = false;
    private boolean hasBurntMeat = false;
    private boolean hasRatsTail = false;

    // Witch's Potion completed (default false)
    private boolean witchsPotionCompleted = false;

    private int counter = 0;

    private enum State {
        START_QUEST,
        CHECK_ITEMS,
        RUN_HETTY,
        RUN_RATS,
        GET_TAIL,
        RUN_GIANTRATS,
        GET_MEAT,
        RUN_TAVERN,
        BURN_MEAT,
        CHECK_MEAT,
        RUN_FIELD,
        GET_ONION,
        RUN_MAGICSHOP,
        GET_EYEOFNEWT,
        TURN_IN_ITEMS,
        DRINK_CAULDRON,
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

        //  Start running if energy is atleast above 30
        startRunning();

        switch(currentState) {
            case RUN_HETTY:
                // Run to witch's house
                Tile hetty = new Tile(2966, 3206, 0);
                if (s.getLocalPlayer().getTile().distance(hetty) > 1){
                    s.getWalking().walk(hetty);
                    walkingSleep();
                } else {
                    if (s.getPlayerSettings().getConfig(QUEST_PROG) == 0) {
                        currentState = State.START_QUEST;
                        s.log("CURRENT STATE: " + currentState);
                        break;
                    } else if (s.getPlayerSettings().getConfig(QUEST_PROG) == 1) {
                        currentState = State.TURN_IN_ITEMS;
                        s.log("CURRENT STATE: " + currentState);
                        break;
                    }
                }
                break;

            case START_QUEST:
                // Talk to Hetty to start the quest
                s.sleep(Calculations.random(700, 950));
                if (s.getDialogues().getOptions() == null){
                    talkTo("Hetty");
                } else if (s.getDialogues().getOptions().length == 2) {
                    s.getDialogues().chooseOption("I am in search of a quest.");
                } else if (s.getDialogues().getOptions().length == 3) {
                    s.getDialogues().chooseOption("Yes, help me become one with my darker side.");
                } else {
                    if (s.getDialogues().canContinue()) {
                        s.getDialogues().clickContinue();
                    }
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
                        currentState = State.RUN_HETTY;
                        s.log("CURRENT STATE: " + currentState);
                        break;
                    case 1:
                        // Check items
                        hasRatsTail = s.getInventory().contains("Rat's tail");
                        hasBurntMeat = s.getInventory().contains("Burnt meat");
                        hasEyeOfNewt = s.getInventory().contains("Eye of newt");
                        hasOnion = s.getInventory().contains("Onion");

                        // If no quest items are gathered, get rat's tail first
                        if (!hasRatsTail && !hasOnion &&  !hasBurntMeat && !hasEyeOfNewt) {
                            // Run to rats to get rat' tail
                            currentState = State.RUN_RATS;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                        // If rat's tail is gathered, get burnt meat next
                        if (hasRatsTail && !hasOnion &&  !hasBurntMeat && !hasEyeOfNewt) {
                            // Bot has raw meat already, go burn it
                            if (s.getInventory().contains("Raw rat meat")) {
                                currentState = State.RUN_TAVERN;
                                s.log("CURRENT STATE: " + currentState);
                                break;
                            } else if (s.getInventory().contains("Cooked meat")) {
                                currentState = State.RUN_TAVERN;
                                s.log("CURRENT STATE: " + currentState);
                                break;
                            } else {
                                // Run to giant rats to get raw rat meat
                                currentState = State.RUN_GIANTRATS;
                                s.log("CURRENT STATE: " + currentState);
                                break;
                            }
                        }
                        // If rat's tail and burnt meat is gathered, get onion
                        if (hasRatsTail && !hasOnion &&  hasBurntMeat && !hasEyeOfNewt) {
                            // Run to magic shop to get eye of newt
                            currentState = State.RUN_FIELD;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                        // Gather the last item, the eye of newt
                        if (hasRatsTail && hasOnion &&  hasBurntMeat && !hasEyeOfNewt) {
                            // Run to magic shop to get eye of newt
                            currentState = State.RUN_MAGICSHOP;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                        // Turn in items if all are gathered
                        if (hasRatsTail && hasOnion &&  hasBurntMeat && hasEyeOfNewt) {
                            currentState = State.RUN_HETTY;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                        break;
                }
                break;
            case RUN_RATS:
                // Run to the archery house to kill rats
                s.sleep(Calculations.random(700, 950));
                Tile rats = new Tile(2956, 3204, 0);
                if (s.getLocalPlayer().getTile().distance(rats) > 1){
                    s.getWalking().walk(rats);
                    walkingSleep();
                } else {
                    currentState = State.GET_TAIL;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case GET_TAIL:
                s.sleep(Calculations.random(700, 950));
                if (!s.getInventory().contains("Rat's tail")) {
                    // Attack rat
                    NPC rat = s.getNpcs().closest(npc -> npc.getName().equals("Rat") && !npc.isInCombat());
                    if (rat != null){
                        if (rat.interact("Attack")){
                            randomCameraMovement();
                            s.sleepUntil(() -> s.getLocalPlayer().getInteractingCharacter() != null,Calculations.random(2400, 3600));
                            while (s.getLocalPlayer().getInteractingCharacter() != null) {
                                s.sleepUntil(() -> rat.getInteractingCharacter() == null, Calculations.random(2400, 3600));
                            }
                        }
                    }
                    s.sleep(Calculations.random(700, 950));
                    // Loot tail if it's on the floor
                    if (s.getLocalPlayer().getInteractingCharacter() == null) {
                        GroundItem ratsTail = s.getGroundItems().closest("Rat's tail");
                        if (ratsTail != null) {
                            if (ratsTail.interact("Take")) {
                                s.sleepUntil(() -> s.getInventory().contains("Rat's tail"), Calculations.random(2400, 3600));
                            }
                        }
                    }
                } else {
                    currentState = State.CHECK_ITEMS;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case RUN_GIANTRATS:
                // Run to giant rats
                s.sleep(Calculations.random(700, 950));
                Tile giantRats = new Tile(2997, 3196, 0);
                if (s.getLocalPlayer().getTile().distance(giantRats) > 5){
                    s.getWalking().walk(giantRats);
                    walkingSleep();
                } else {
                    currentState = State.GET_MEAT;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case GET_MEAT:
                s.sleep(Calculations.random(700, 950));
                if (!s.getInventory().contains("Raw rat meat")) {
                    // Attack giant rat
                    NPC rat = s.getNpcs().closest(npc -> npc.getName().equals("Giant rat") && !npc.isInCombat());
                    if (rat != null){
                        if (rat.isOnScreen()) {
                            if (rat.interact("Attack")){
                                randomCameraMovement();
                                s.sleepUntil(() -> s.getLocalPlayer().getInteractingCharacter() != null,Calculations.random(2400, 3600));
                                while (s.getLocalPlayer().getInteractingCharacter() != null) {
                                    s.sleepUntil(() -> rat.getInteractingCharacter() == null, Calculations.random(2400, 3600));
                                }
                            }
                        } else {
                            s.getCamera().rotateToEntityEvent(rat);
                        }
                    }
                    s.sleep(Calculations.random(700, 950));
                    // Loot raw meat if it's on the floor
                    if (s.getLocalPlayer().getInteractingCharacter() == null) {
                        GroundItem ratsTail = s.getGroundItems().closest("Raw rat meat");
                        if (ratsTail != null) {
                            if (ratsTail.isOnScreen()) {
                                if (ratsTail.interact("Take")) {
                                    s.sleepUntil(() -> s.getInventory().contains("Raw rat meat"), Calculations.random(2400, 3600));
                                }
                            }  else {
                                s.getCamera().rotateToEntityEvent(ratsTail);
                            }
                        }
                    }
                } else {
                    currentState = State.CHECK_ITEMS;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case RUN_TAVERN:
                // Run to the range
                s.sleep(Calculations.random(700, 950));
                Tile tavern = new Tile(2969, 3210, 0);
                if (s.getLocalPlayer().getTile().distance(tavern) > 1){
                    s.getWalking().walk(tavern);
                    walkingSleep();
                } else {
                    currentState = State.BURN_MEAT;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case BURN_MEAT:
                // Cook raw rat meat
                s.sleep(Calculations.random(700, 950));
                if (!s.getInventory().isItemSelected()) {
                    if (s.getInventory().contains("Raw rat meat")) {
                        if (s.getInventory().interact("Raw rat meat", "Use")) {
                            s.sleepUntil(() -> s.getInventory().isItemSelected(), Calculations.random(1200, 1400));
                        }
                    } else if (s.getInventory().contains("Cooked meat")) {
                        if (s.getInventory().interact("Cooked meat", "Use")) {
                            s.sleepUntil(() -> s.getInventory().isItemSelected(), Calculations.random(1200, 1400));
                        }
                    }
                } else {
                    s.sleep(Calculations.random(700, 950));
                    GameObject range = s.getGameObjects().closest("Range");
                    if (range.interact("Use")) {
                        walkingSleep();
                        s.sleepUntil(() -> s.getLocalPlayer().getAnimation() == -1, Calculations.random(2000, 3000));
                        currentState = State.CHECK_MEAT;
                        s.log("currentState: " + currentState);
                        break;
                    }
                }
                break;
            case CHECK_MEAT:
                s.sleep(Calculations.random(700, 950));
                // Check if meat is burnt or not
                if (s.getInventory().contains("Cooked meat")) {
                    currentState = State.BURN_MEAT;
                    s.log("currentState: " + currentState);
                    break;
                } else if (s.getInventory().contains("Burnt meat")) {
                    currentState = State.CHECK_ITEMS;
                    s.log("currentState: " + currentState);
                    break;
                }
                break;
            case RUN_FIELD:
                // Run to the onion field
                s.sleep(Calculations.random(700, 950));
                Tile field = new Tile(2955, 3255, 0);
                if (s.getLocalPlayer().getTile().distance(field) > 1){
                    s.getWalking().walk(field);
                    walkingSleep();
                } else {
                    currentState = State.GET_ONION;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case GET_ONION:
                // Pick the onion
                s.sleep(Calculations.random(700, 950));
                GameObject onion = s.getGameObjects().closest("Onion");
                if (onion != null){
                    if (onion.isOnScreen()) {
                        if (onion.interact("Pick")){
                            s.sleepUntil(() -> s.getInventory().contains("Onion"),Calculations.random(2400, 3600));
                            currentState = State.CHECK_ITEMS;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                    } else {
                        s.getCamera().rotateToEntityEvent(onion);
                    }
                }
                break;
            case RUN_MAGICSHOP:
                // Run to magic shop
                s.sleep(Calculations.random(700, 950));
                Tile magicshop = new Tile(3014, 3258, 0);
                if (s.getLocalPlayer().getTile().distance(magicshop) > 1){
                    s.getWalking().walk(magicshop);
                    walkingSleep();
                } else {
                    currentState = State.GET_EYEOFNEWT;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case GET_EYEOFNEWT:
                // Buy the eye of newt
                s.sleep(Calculations.random(700, 950));
                NPC betty = s.getNpcs().closest("Betty");
                if (betty != null)  {
                    if (betty.isOnScreen()) {
                        if (betty.interact("Trade")) {
                            s.sleep(Calculations.random(3000, 4000));
                            WidgetChild eyeOfNewt = s.getWidgets().getWidgetChild(300, 16, 15);
                            eyeOfNewt.interact("Buy 1");
                            s.sleepUntil(() -> s.getInventory().contains("Eye of newt"),Calculations.random(2400, 3600));
                            s.sleep(Calculations.random(700, 950));
                            currentState = State.CHECK_ITEMS;
                            s.log("CURRENT STATE: " + currentState);
                            break;
                        }
                    } else {
                        s.getCamera().rotateToEntityEvent(betty);
                    }
                }
                break;
            case TURN_IN_ITEMS:
                // Turn in all the items
                s.sleep(Calculations.random(700, 950));
                talkTo("Hetty");
                if (s.getPlayerSettings().getConfig(QUEST_PROG) == 2) {
                    currentState = State.DRINK_CAULDRON;
                    s.log("CURRENT STATE: " + currentState);
                    break;
                }
                break;
            case DRINK_CAULDRON:
                // Drink from the cauldron
                s.sleep(Calculations.random(700, 950));
                GameObject cauldron = s.getGameObjects().closest("Cauldron");
                if (cauldron != null) {
                    if (cauldron.isOnScreen()) {
                        if (cauldron.interact("Drink From")) {
                            s.sleepUntil(() -> s.getDialogues().canContinue(),Calculations.random(2400, 3600));
                            if (s.getDialogues().canContinue()) {
                                s.sleep(Calculations.random(2000, 3000));
                                s.getDialogues().clickContinue();
                            }
                            s.sleep(Calculations.random(2000, 3000));
                            Widget questComplete = s.getWidgets().getWidget(277);
                            if (questComplete != null && questComplete.isVisible()) {
                                questComplete.getChild(15).interact();
                                currentState = State.COMPLETE;
                                s.log("currentState: " + currentState);
                                break;
                            }
                        }
                    } else {
                        s.getCamera().rotateToEntityEvent(cauldron);
                    }
                }
                break;

            case COMPLETE:
                witchsPotionCompleted = true;
                s.log("Witch's Potion successfully completed!");
                break;
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
                        s.sleepUntil(() -> s.getDialogues().canContinue(), Calculations.random(1200,1600));
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

    // Get the current quest state
    private State getState() {
        // Quest started
        if (s.getPlayerSettings().getConfig(QUEST_PROG) == 1) {
            return State.CHECK_ITEMS;
        }
        // Turned in items
        if (s.getPlayerSettings().getConfig(QUEST_PROG) == 2) {
            return State.DRINK_CAULDRON;
        }
        // Quest completed
        if (s.getPlayerSettings().getConfig(QUEST_PROG) == 3) {
            return State.COMPLETE;
        }
        return State.RUN_HETTY;

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

    public boolean isWitchsPotionCompleted() {
        return witchsPotionCompleted;
    }

    // Start running again if energy is atleast above 30
    private void startRunning() {
        if(!s.getWalking().isRunEnabled() && s.getWalking().getRunEnergy() > Calculations.random(30,50)){
            s.getWalking().toggleRun();
        }
    }
}
