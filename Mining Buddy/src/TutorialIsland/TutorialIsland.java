package TutorialIsland;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.*;

import static org.dreambot.api.methods.MethodProvider.log;
import static org.dreambot.api.methods.magic.Normal.WIND_STRIKE;

public class TutorialIsland {
    AbstractScript s;

    // Ids
    private final int TUT_PROG = 281;

    // Ore id
    public static final int[] TIN_IDS = new int[] {10080, 0, 0};
    public static final int[] COPPER_IDS = new int[] {10079, 0, 0};

    // Areas
    Area PondArea = new Area(3100, 3099, 3104, 3093, 0);
    Area GateArea = new Area(3090, 3091, 3091, 3094, 0);
    Area CookDoor = new Area(3079, 3086, 3081,3083);
    Area CookDoor_2 = new Area(3073, 3089, 3074,3091);
    Area QuestHouse = new Area(3087, 3126, 3084,3127);

    private int counter = 0;

    // Tutorial island state
    private State currentState;

    // Randomize counter
    private int randomizeCounter = 0;

    public TutorialIsland (AbstractScript ctx) {
        this.s = ctx;
    }

    private enum State {
        DO_TUTORIAL
    }

    public void onStart() {

    }

    public int onLoop() {
        // Run getState()
        currentState = getState();

        // Run roof check once
        if (counter == 0) {
            if (s.getClientSettings().roofsEnabled()) {
                disableRoof();
            }
            counter++;
        }

        switch (currentState) {
            case DO_TUTORIAL:
                // Get current state
                int conf = s.getPlayerSettings().getConfig(TUT_PROG);
                s.log("CURRENT STATE: " + conf);
                if (s.getLocalPlayer().getAnimation() != -1 || s.getLocalPlayer().isMoving()) {
                    randomCameraMovement();
                }
                switch (conf) {
                    case 7:
                        // Talk to npc
                        talkTo("Gielinor Guide");
                        break;
                    case 10:
                        // Go through door
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            s.sleep(Calculations.random(700, 950));
                            if (!s.getWalking().isRunEnabled()) {
                                s.getWalking().toggleRun();
                            }
                            GameObject door = s.getGameObjects().closest("Door");
                            if (door != null) {
                                if (door.interact("Open")) {
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 10, Calculations.random(1600, 2000));
                                }
                            }
                        }
                        break;
                    case 20:
                        // Talk to survival expert
                        if (PondArea.contains(s.getLocalPlayer())) {
                            s.sleep(Calculations.random(700, 950));
                            talkTo("Survival Expert");
                            break;
                        } else {
                            runToArea(PondArea);
                            randomCameraMovement();
                        }
                    case 30:
                        // Open inventory
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            s.sleep(Calculations.random(700, 950));
                            s.getTabs().openWithMouse(Tab.INVENTORY);
                        }
                        break;
                    case 40:
                        // Go fishing
                        s.sleep(Calculations.random(700, 950));
                        NPC fishingSpot = s.getNpcs().closest("Fishing spot");

                        s.sleep(Calculations.random(750, 1000));
                        Inventory inventory = s.getInventory();
                        if (inventory.contains("Small fishing net")) {
                            if (!fishingSpot.isOnScreen()) {
                                s.getCamera().rotateToEntityEvent(fishingSpot);
                            } else {
                                if (!s.getLocalPlayer().isMoving()) {
                                    if (s.getLocalPlayer().getAnimation() != 621) {
                                        s.sleep(Calculations.random(700, 950));
                                        if (fishingSpot.interact("Net")) {
                                            randomCameraMovement();
                                            s.sleepWhile(() -> !s.getInventory().contains("Raw shrimps"), Calculations.random(35000, 40000));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    case 50:
                        // Open stats tab
                        s.sleep(Calculations.random(700, 950));
                        if (s.getTabs().openWithMouse(Tab.STATS)) {
                            s.sleepWhile(() -> !s.getTabs().isOpen(Tab.STATS), Calculations.random(35000, 40000));
                        }
                        break;
                    case 60:
                        // Talk to survival expert to start woodcutting
                        s.sleep(Calculations.random(700, 950));
                        talkTo("Survival Expert");
                        break;
                    case 70:
                        s.sleep(Calculations.random(700, 950));
                        // Start woodcutting
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            GameObject tree = s.getGameObjects().closest(gameObject -> gameObject != null && gameObject.getName().equals("Tree"));
                            if (tree != null) {
                                if (!s.getLocalPlayer().isMoving() && s.getLocalPlayer().getAnimation() != 879) {
                                    if (!tree.isOnScreen()) {
                                        s.getCamera().rotateToEntityEvent(tree);
                                    } else {
                                        s.sleep(Calculations.random(700, 950));
                                        if (tree.interact("Chop down")) {
                                            randomCameraMovement();
                                            s.sleepWhile(() -> !s.getInventory().contains("Logs"), Calculations.random(35000, 40000));
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case 80:
                        // Run to gate area to avoid fires on the floor around the pond
                        if (GateArea.contains(s.getLocalPlayer())) {
                            if (s.getInventory().contains("Logs") && s.getInventory().contains("Raw shrimps")) {
                                // Make a fire
                                if (s.getTabs().isOpen(Tab.INVENTORY)) {
                                    if (s.getInventory().contains("Logs") && s.getInventory().contains("Tinderbox")) {
                                        if (!s.getLocalPlayer().isMoving() && s.getLocalPlayer().getAnimation() != 733) {
                                            s.sleep(Calculations.random(700, 950));
                                            if (!s.getInventory().isItemSelected()) {
                                                s.getInventory().interact("Tinderbox", "Use");
                                            }
                                            s.sleep(Calculations.random(700, 950));
                                            if (s.getInventory().isItemSelected()) {
                                                s.getInventory().interact("Logs", "Use");
                                            }
                                        }
                                    }
                                } else {
                                    s.sleep(Calculations.random(700, 950));
                                    s.getTabs().openWithMouse(Tab.INVENTORY);
                                }
                            } else {
                                // Wait until inventory contains logs
                                s.sleepWhile(() -> !s.getInventory().contains("logs"), Calculations.random(2000, 3600));
                                break;
                            }
                        } else {
                            runToArea(GateArea);
                            randomCameraMovement();
                        }
                        break;
                    case 90:
                        s.sleep(Calculations.random(700, 950));
                        // Cook shrimps
                        if (s.getTabs().isOpen(Tab.INVENTORY)) {
                            if (!s.getLocalPlayer().isMoving() && s.getLocalPlayer().getAnimation() != 897) {
                                GameObject fire = s.getGameObjects().closest("Fire");
                                s.sleep(Calculations.random(700, 950));
                                if (!s.getInventory().isItemSelected()) {
                                    s.getInventory().interact("Raw shrimps", "Use");
                                }
                                s.sleep(Calculations.random(700, 950));
                                if (s.getInventory().isItemSelected()) {
                                    s.sleepUntil(() -> fire.interact("Use"), Calculations.random(1000, 2000));
                                }
                            }
                        } else {
                            s.sleep(Calculations.random(700, 950));
                            s.getTabs().openWithMouse(Tab.INVENTORY);
                            break;
                        }
                        break;
                    case 120:
                        s.sleep(Calculations.random(700, 950));
                        // Run to gate area if not in it yet
                        if (!GateArea.contains(s.getLocalPlayer())) {
                            runToArea(GateArea);
                            randomCameraMovement();
                            break;
                        } else {
                            // Open gate
                            s.sleep(Calculations.random(700, 950));
                            GameObject gate = s.getGameObjects().closest("Gate");
                            if (gate != null) {
                                if (gate.interact("Open")) {
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 120, Calculations.random(1400, 1800));
                                }
                            }
                        }
                        //went through gate
                        break;
                    case 130:
                        // Run to cook door
                        s.sleep(Calculations.random(700, 950));
                        if (!CookDoor.contains(s.getLocalPlayer())) {
                            runToArea(CookDoor);
                            randomCameraMovement();
                            break;
                        } else {
                            // Open cook door
                            s.sleep(Calculations.random(700, 950));
                            GameObject cookDoor = s.getGameObjects().closest("Door");
                            if (cookDoor != null) {
                                if (cookDoor.interact("Open")) {
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 130, Calculations.random(1400, 1800));
                                }
                            }
                        }
                        break;
                    case 140:
                        // Talk to master chef to get ingredients
                        s.sleep(Calculations.random(700, 950));
                        talkTo("Master Chef");
                        break;
                    case 150:
                        // Make dough
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            if (!s.getInventory().isItemSelected()) {
                                if (s.getInventory().interact("Bucket of water", "Use")) {
                                    s.sleepUntil(() -> s.getInventory().isItemSelected(), Calculations.random(1200, 1400));
                                }
                            } else {
                                if (s.getInventory().interact("Pot of flour", "Use")) {
                                    s.sleepUntil(() -> s.getInventory().contains("Bread dough"), Calculations.random(1200, 1400));
                                }
                            }
                        }
                        break;
                    case 160:
                        // Bake the bread
                        s.sleep(Calculations.random(700, 950));
                        if (!s.getInventory().isItemSelected()) {
                            if (s.getInventory().interact("Bread dough", "Use")) {
                                s.sleepUntil(() -> s.getInventory().isItemSelected(), Calculations.random(1200, 1400));
                            }
                        } else {
                            GameObject range = s.getGameObjects().closest("Range");
                            if (range.interact("Use")) {
                                walkingSleep();
                                s.sleepUntil(() -> s.getInventory().contains("Bread"), Calculations.random(2000, 3000));
                            }
                        }
                        //cooked bread
                        break;
                    case 170:
                        s.sleep(Calculations.random(700, 950));
                        // Move to next door
                        if (CookDoor_2.contains(s.getLocalPlayer())) {
                            // Open cook door
                            s.sleep(Calculations.random(700, 950));
                            GameObject cookDoor2 = s.getGameObjects().closest("Door");
                            if (cookDoor2 != null) {
                                if (cookDoor2.interact("Open")) {
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 170, Calculations.random(1400, 1800));
                                }
                            }
                        } else {
                            runToArea(CookDoor_2);
                            randomCameraMovement();
                        }
                        break;
                    case 200:
                        // Turn on run
                        s.sleep(Calculations.random(700, 950));
                        WidgetChild wc = s.getWidgets().getChildWidget(160,22);
                        if(wc != null && wc.isVisible()){
                            wc.interact();
                            s.sleepUntil(() -> s.getWalking().isRunEnabled(),1200);
                        }
                        //turned on run in settings
                        break;
                    case 210:
                        // Run to quest house and enter
                        s.sleep(Calculations.random(700, 950));
                        if (QuestHouse.contains(s.getLocalPlayer())) {
                            // Open questhouse door
                            s.sleep(Calculations.random(700, 950));
                            GameObject questDoor = s.getGameObjects().closest("Door");
                            if (questDoor != null) {
                                if (questDoor.interact("Open")) {
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 210, Calculations.random(1400, 1800));
                                }
                            }
                        } else {
                            runToArea(QuestHouse);
                            randomCameraMovement();
                        }
                        break;
                    case 220:
                        s.sleep(Calculations.random(700, 950));
                        talkTo("Quest Guide");
                        s.getWalking().walk(s.getLocalPlayer().getTile());
                        break;
                    case 230:
                        // Open quest tab
                        s.sleep(Calculations.random(700, 950));
                        if (s.getTabs().openWithMouse(Tab.QUEST)) {
                            s.sleepWhile(() -> !s.getTabs().isOpen(Tab.QUEST), Calculations.random(35000, 40000));
                        }
                        break;
                    case 240:
                        // Get quest guide tutorial
                        s.sleep(Calculations.random(700, 950));
                        talkTo("Quest Guide");
                        break;
                    case 250:
                        // Climb down the ladder
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            GameObject ladder = s.getGameObjects().closest("Ladder");
                            if(ladder != null){
                                if(ladder.interact("Climb-down")){
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 250,Calculations.random(4000,6000));
                                }
                            }
                        }
                        break;
                    case 260:
                        // Run to mining area
                        s.sleep(Calculations.random(700, 950));
                        if (s.getLocalPlayer().getTile().distance(new Tile(3082, 9504,0)) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(new Tile(3082, 9504,0));
                            walkingSleep();
                        } else {
                            talkTo("Mining Instructor");
                        }
                        break;
                    case 270:
                        s.sleep(Calculations.random(700, 950));
                        // Get pickaxe
                        if(s.getDialogues().canContinue()) {
                            log("Clicking continue");
                            s.getDialogues().continueDialogue();
                            s.sleep(600, 900);
                        }
                        break;
                    case 300:
                        // Mine tin
                        s.sleep(Calculations.random(700, 950));
                        GameObject TinVein = s.getGameObjects().closest(gameObject -> gameObject != null && gameObject.getName().equals("Rocks") && gameObject.getID() == TIN_IDS[0]);

                        // Prospect the ore
                        /*if(!s.getDialogues().canContinue()){
                            if (TinVein.getID() == TIN_IDS[0] && s.getLocalPlayer().getAnimation() == -1) {
                                if (TinVein.isOnScreen()) {
                                    if (TinVein.getClickablePoint() != null) {
                                        if (TinVein.interact("Prospect")) {
                                            randomCameraMovement();
                                            walkingSleep();
                                            s.sleepUntil(() -> s.getDialogues().canContinue(),Calculations.random(1200,1600));
                                        }
                                    } else {
                                        s.getCamera().mouseRotateTo(Calculations.random(0, 2048), Calculations.random(0, 2048));
                                    }
                                }
                            }
                        } else {
                            log("Clicking continue");
                            s.getDialogues().continueDialogue();
                            s.sleep(600, 900);
                        }*/
                        s.sleep(Calculations.random(700, 950));
                        if(!s.getDialogues().canContinue()){
                            if (TinVein.getID() == TIN_IDS[0] && s.getLocalPlayer().getAnimation() == -1) {
                                if (TinVein.isOnScreen()) {
                                    if (TinVein.getClickablePoint() != null) {
                                        if (TinVein.interact("Mine")) {
                                            randomCameraMovement();
                                            s.sleepUntil(() -> s.getInventory().contains("Tin ore"), Calculations.random(4000, 6000));
                                        }
                                    } else {
                                        s.getCamera().mouseRotateTo(Calculations.random(0, 2048), Calculations.random(0, 2048));
                                    }
                                }
                            }
                        } else {
                            log("Clicking continue");
                            s.getDialogues().continueDialogue();
                            s.sleep(600, 900);
                        }
                        //inspected and mined tin
                        break;
                    case 310:
                        // Mine copper
                        if (s.getLocalPlayer().getTile().distance(new Tile(3084, 9502,0)) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(new Tile(3084, 9502,0));
                            walkingSleep();
                        } else {
                            GameObject CopperVein = s.getGameObjects().closest(gameObject -> gameObject != null && gameObject.getName().equals("Rocks") && gameObject.getID() == COPPER_IDS[0]);
                            s.sleep(Calculations.random(700, 950));

                            if (!s.getDialogues().canContinue()){
                                if (CopperVein.getID() == COPPER_IDS[0] && s.getLocalPlayer().getAnimation() == -1) {
                                    if (CopperVein.isOnScreen()) {
                                        if (CopperVein.getClickablePoint() != null) {
                                            if (CopperVein.interact("Mine")) {
                                                randomCameraMovement();
                                                s.sleepUntil(() -> s.getInventory().contains("Copper ore"), Calculations.random(4000, 6000));
                                            }
                                        } else {
                                            s.getCamera().mouseRotateTo(Calculations.random(0, 2048), Calculations.random(0, 2048));
                                        }
                                    }
                                }
                            } else {
                                log("Clicking continue");
                                s.getDialogues().continueDialogue();
                                s.sleep(600, 900);
                            }
                        }
                        break;
                    case 320:
                        // Smelt bronze
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            if(!s.getInventory().isItemSelected()){
                                s.sleep(1200,1800);
                                s.getInventory().interact("Tin ore", "Use");
                                s.sleepUntil(() -> s.getInventory().isItemSelected(),Calculations.random(800,1200));
                            } else {
                                GameObject furnace = s.getGameObjects().closest(10082);

                                if(furnace != null){
                                    if(furnace.interact("Use")){
                                        walkingSleep();
                                        s.sleepUntil(() -> s.getInventory().contains("Bronze bar"),Calculations.random(2000,3000));
                                    }
                                }
                            }
                        }
                        break;
                    case 330:
                        // Run to mining area
                        s.sleep(Calculations.random(700, 950));
                        if (s.getLocalPlayer().getTile().distance(new Tile(3082, 9504,0)) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(new Tile(3082, 9504,0));
                            walkingSleep();
                        } else {
                            talkTo("Mining Instructor");
                        }
                        break;
                    case 340:
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            s.sleep(Calculations.random(700, 950));
                            // Open anvil panel
                            if (!s.getInventory().isItemSelected()){
                                s.getInventory().interact("Bronze bar", "Use");
                                s.sleepUntil(() -> s.getInventory().isItemSelected(),1200);
                            } else {
                                GameObject furnace = s.getGameObjects().closest("Anvil");
                                if(furnace != null){
                                    if(furnace.interact("Use")){
                                        walkingSleep();
                                        s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 340,Calculations.random(2000,3000));
                                    }
                                }
                            }
                        }
                        break;
                    case 350:
                        s.sleep(Calculations.random(700, 950));
                        // Smith a knife
                        s.getWidgets().getChildWidget(312,2).interact();
                        s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 350,3000);
                        break;
                    case 360:
                        // Run to mining gate and open
                        s.sleep(Calculations.random(700, 950));
                        if (s.getLocalPlayer().getTile().distance(new Tile(3094, 9503,0)) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(new Tile(3094, 9503,0));
                            walkingSleep();
                        } else {
                            GameObject gate = s.getGameObjects().closest("Gate");
                            if(gate != null){
                                if(gate.interact("Open")){
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 360,Calculations.random(4000,6000));
                                }
                            }
                        }
                        break;
                    case 370:
                        // Run to combat area and talk to combat instructor
                        s.sleep(Calculations.random(700, 950));
                        if (s.getLocalPlayer().getTile().distance(new Tile(3104,9506,0)) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(new Tile(3104,9506,0));
                            walkingSleep();
                        } else {
                            talkTo("Combat Instructor");
                        }
                        break;
                    case 390:
                        // Open equipment tab
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            s.sleep(Calculations.random(700, 950));
                            if (s.getTabs().openWithMouse(Tab.EQUIPMENT)) {
                                s.sleepWhile(() -> !s.getTabs().isOpen(Tab.EQUIPMENT), Calculations.random(35000, 40000));
                            }
                        }
                        break;
                    case 400:
                        // Open equipment stats
                        s.sleep(Calculations.random(700, 950));
                        s.getWidgets().getChildWidget(387,17).interact();
                        s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 400,Calculations.random(1200,1600));
                        break;
                    case 405:
                        // Equip dagger
                        if (s.getInventory().interact("Bronze dagger", "Equip")){
                            s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 410,Calculations.random(1200,1600));
                        } else if (s.getInventory().interact("Bronze dagger", "Wield")){
                            s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 410,Calculations.random(1200,1600));
                        }
                        s.getWidgets().getChildWidget(84,4).interact();
                        break;
                    case 410:
                        // Run to combat area and talk to combat instructor
                        s.sleep(Calculations.random(700, 950));
                        if (s.getLocalPlayer().getTile().distance(new Tile(3104,9506,0)) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(new Tile(3104,9506,0));
                            walkingSleep();
                        } else {
                            talkTo("Combat Instructor");
                        }
                        break;
                    case 420:
                        // Open equipment tab
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            // Unequip knife
                            // Equip sword & shield
                            Item i = s.getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot());
                            if(i != null && i.getName().contains("dagger")){
                                if (s.getTabs().openWithMouse(Tab.EQUIPMENT)) {
                                    s.sleepWhile(() -> !s.getTabs().isOpen(Tab.EQUIPMENT), Calculations.random(35000, 40000));
                                }
                                s.getEquipment().unequip(EquipmentSlot.WEAPON);
                            } else {
                                s.sleep(Calculations.random(700, 950));
                                if (s.getTabs().openWithMouse(Tab.INVENTORY)) {
                                    s.sleepWhile(() -> !s.getTabs().isOpen(Tab.INVENTORY), Calculations.random(35000, 40000));
                                }
                                if (i != null){
                                    s.getInventory().interact("Wooden shield", "Wield");
                                    s.sleepUntil(() -> s.getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()) != null,Calculations.random(1200,1600));
                                } else {
                                    s.getInventory().interact("Bronze sword", "Wield");
                                    s.sleepUntil(() -> s.getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()) != null,Calculations.random(1200,1600));
                                }
                            }
                        }
                        break;
                    case 430:
                        // Open equipment tab
                        s.sleep(Calculations.random(700, 950));
                        if (s.getTabs().openWithMouse(Tab.COMBAT)) {
                            s.sleepWhile(() -> !s.getTabs().isOpen(Tab.COMBAT), Calculations.random(35000, 40000));
                        }
                        break;
                    case 440:
                        // Open rat gate
                        s.sleep(Calculations.random(700, 950));
                        if (s.getLocalPlayer().getTile().distance(new Tile(3111, 9519,0)) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(new Tile(3111, 9519,0));
                            walkingSleep();
                        } else {
                            GameObject gate = s.getGameObjects().closest("Gate");
                            if(gate != null){
                                if(gate.interact("Open")){
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 440,Calculations.random(4000,6000));
                                }
                            }
                        }
                        break;
                    case 450:
                        // Attack rat
                        NPC rat = s.getNpcs().closest(npc -> npc.getName().equals("Giant rat") && !npc.isInCombat());
                        if(rat != null){
                            if(rat.interact("Attack")){
                                walkingSleep();
                                s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 450,Calculations.random(1200,2000));
                            } else {
                                if(s.getCamera().getPitch() < Calculations.random(150,200)){
                                    s.getCamera().rotateToPitch(Calculations.random(200,360));
                                }
                            }
                        }
                        break;
                    case 460:
                        // Killed rat
                        randomCameraMovement();
                        s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 460,2400);
                        break;
                    case 470:
                        // Run to rat gate
                        if(!s.getMap().canReach(new Tile(3112,9518,0))){
                            GameObject gate = s.getGameObjects().closest("Gate");
                            if(gate != null){
                                if(gate.interact("Open")){
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getLocalPlayer().getTile().getX() == 3111,Calculations.random(1200,1400));
                                }
                            }
                        } else {
                            // Talk to combat instructor
                            talkTo("Combat instructor");
                        }
                        break;
                    case 480:
                        s.sleep(Calculations.random(700, 950));
                        if (s.getLocalPlayer().getTile().distance(new Tile(3108,9511,0)) > 1){
                            randomCameraMovement();
                            s.getWalking().walk(new Tile(3108,9511,0));
                            walkingSleep();
                        } else {
                            if (s.getDialogues().canContinue()){
                                s.getDialogues().clickContinue();
                                s.sleep(900,1200);
                            } else {
                                // Equip bow & arrow
                                // Attack rat
                                if(s.getEquipment().isSlotEmpty(EquipmentSlot.ARROWS.getSlot())){
                                    s.getInventory().interact("Bronze arrow", "Wield");
                                    s.sleepUntil(() -> s.getEquipment().isSlotFull(EquipmentSlot.ARROWS.getSlot()),Calculations.random(1200,1600));
                                }
                                else if(s.getInventory().contains("Shortbow")){
                                    s.getInventory().interact("Shortbow", "Wield");
                                    s.sleepUntil(() -> !s.getInventory().contains("Shortbow"),Calculations.random(1200,1600));
                                } else {
                                    rat = s.getNpcs().closest(npc -> npc.getName().equals("Giant rat") && !npc.isInCombat());
                                    if (rat != null){
                                        if(rat.interact("Attack")){
                                            s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 480,Calculations.random(2400, 3600));
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case 490:
                        // Killed rat
                        s.sleep(Calculations.random(700, 950));
                        randomCameraMovement();
                        if(s.getEquipment().isSlotEmpty(EquipmentSlot.ARROWS.getSlot())){
                            s.getInventory().interact("Bronze arrow", "Wield");
                            s.sleepUntil(() -> s.getEquipment().isSlotFull(EquipmentSlot.ARROWS.getSlot()),1200);
                        } else if (s.getInventory().contains("Shortbow")){
                            s.getInventory().interact("Shortbow", "Wield");
                            s.sleepUntil(() -> !s.getInventory().contains("Shortbow"),1200);
                        } else {
                            if(s.getLocalPlayer().getInteractingCharacter() == null){
                                rat = s.getNpcs().closest(npc -> npc.getName().equals("Giant rat") && !npc.isInCombat());
                                if(rat != null){
                                    if(rat.interact("Attack")){
                                        s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 480,Calculations.random(2400, 3600));
                                    }
                                }
                            }
                            else{
                                s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 490,Calculations.random(2400,3000));
                            }
                        }
                        break;
                    case 500:
                        // Go up ladder
                        if (s.getLocalPlayer().getTile().distance(new Tile(3112,9525,0)) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(new Tile(3112,9525,0));
                            walkingSleep();
                        } else {
                            GameObject gate = s.getGameObjects().closest("Ladder");
                            if (gate != null){
                                if(gate.interact("Climb-up")){
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 500,Calculations.random(2400,3600));
                                }
                            }
                        }
                        break;
                    case 510:
                        // Run to bank and finish dialogue
                        Tile t = new Tile(3122,3123,0);
                        if(s.getLocalPlayer().distance(t) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(t);
                            walkingSleep();
                        } else {
                            if(s.getDialogues().getOptionIndex("Yes.") > 0){
                                s.getDialogues().clickOption("Yes.");
                                s.sleepUntil(() -> s.getBank().isOpen(),Calculations.random(1200,1600));

                                s.getBank().depositAllItems();
                                s.sleepUntil(() -> s.getInventory().isEmpty(),Calculations.random(800,1200));

                                s.getBank().depositAllEquipment();
                                s.sleepUntil(() -> s.getEquipment().isEmpty(),Calculations.random(800,1200));

                                s.getBank().close();
                                s.sleepUntil(() -> !s.getBank().isOpen(), Calculations.random(800,1200));
                            } else {
                                if(!s.getDialogues().canContinue()){
                                    GameObject bankBooth = s.getGameObjects().closest("Bank booth");
                                    if(bankBooth != null){
                                        if(bankBooth.interact("Use")){
                                            s.sleepUntil(() -> s.getDialogues().canContinue(),Calculations.random(2400,3000));
                                        }
                                    }
                                } else {
                                    s.getDialogues().clickContinue();
                                    s.sleepUntil(() -> !s.getDialogues().canContinue(),Calculations.random(1200,1400));
                                }
                            }
                        }
                        break;
                    case 520:
                        s.sleep(Calculations.random(700, 950));
                        if (s.getBank().isOpen()) {
                            // Close bank
                            s.getBank().close();
                        } else {
                            // Talk to poll booth
                            if (s.getDialogues().canContinue()){
                                s.getDialogues().clickContinue();
                                s.sleep(900,1200);
                            } else {
                                GameObject pollBooth = s.getGameObjects().closest("Poll booth");
                                if (pollBooth != null) {
                                    pollBooth.interact("Use");
                                }
                            }
                        }
                        break;
                    case 525:
                        // Move to bank door
                        s.sleep(Calculations.random(700, 950));
                        Tile doorTile = new Tile(3124,3124,0);
                        if(s.getLocalPlayer().distance(doorTile) > 1){
                            randomCameraMovement();
                            s.getWalking().walk(doorTile);
                            walkingSleep();
                        } else {
                            GameObject door = s.getGameObjects().closest("Door");
                            if (door != null){
                                if(door.interact("Open")){
                                    walkingSleep();
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 525,Calculations.random(1600,2400));
                                }
                            }
                        }
                        break;
                    case 530:
                        // Talk to account guide
                        talkTo("Account Guide");
                        break;
                    case 531:
                        // Open account management tab
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            if (s.getTabs().openWithMouse(Tab.ACCOUNT_MANAGEMENT)) {
                                s.sleepWhile(() -> !s.getTabs().isOpen(Tab.ACCOUNT_MANAGEMENT), Calculations.random(35000, 40000));
                            }
                        }
                        break;
                    case 532:
                        // Talk to account guide
                        talkTo("Account Guide");
                        break;
                    case 540:
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            // Move to bank door
                            Tile doorTile2 = new Tile(3129,3124,0);
                            if(s.getLocalPlayer().distance(doorTile2) > 1){
                                randomCameraMovement();
                                s.getWalking().walk(doorTile2);
                                walkingSleep();
                            } else {
                                GameObject door = s.getGameObjects().closest("Door");
                                if (door != null){
                                    if(door.interact("Open")){
                                        walkingSleep();
                                        s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 540,Calculations.random(1600,2400));
                                    }
                                }
                            }
                        }
                        break;
                    case 550:
                        // Walk to church
                        s.sleep(Calculations.random(700, 950));
                        Tile church = new Tile(3123,3106,0);
                        if(s.getLocalPlayer().distance(church) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(church);
                            walkingSleep();
                        } else {
                            talkTo("Brother Brace");
                        }
                        break;
                    case 560:
                        // Open prayer tab
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            if (s.getTabs().openWithMouse(Tab.PRAYER)) {
                                s.sleepWhile(() -> !s.getTabs().isOpen(Tab.PRAYER), Calculations.random(35000, 40000));
                            }
                        }
                        break;
                    case 570:
                        // Talk to priest
                        talkTo("Brother Brace");
                        break;
                    case 580:
                        // Open friends tab
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            if (s.getTabs().openWithMouse(Tab.FRIENDS)) {
                                s.sleepWhile(() -> !s.getTabs().isOpen(Tab.FRIENDS), Calculations.random(35000, 40000));
                            }
                        }
                        break;
                    case 600:
                        // Talk to priest
                        talkTo("Brother Brace");
                        break;
                    case 610:
                        // Open church door
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            s.sleep(Calculations.random(700, 950));
                            Tile churchDoor = new Tile(3123,3103,0);
                            if(s.getLocalPlayer().distance(churchDoor) > 5){
                                randomCameraMovement();
                                s.getWalking().walk(churchDoor);
                                walkingSleep();
                            } else {
                                GameObject door = s.getGameObjects().closest("Door");
                                if (door != null){
                                    if(door.interact("Open")){
                                        walkingSleep();
                                        s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 610,Calculations.random(1600,2400));
                                    }
                                }
                            }
                        }
                        break;
                    case 620:
                        // Run to wizard
                        s.sleep(Calculations.random(700, 950));
                        Tile wizard = new Tile(3141,3089,0);
                        if(s.getLocalPlayer().distance(wizard) > 5){
                            randomCameraMovement();
                            s.getWalking().walk(wizard);
                            walkingSleep();
                        } else {
                            talkTo("Magic Instructor");
                        }
                        break;
                    case 630:
                        // Open magic tab
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            if (s.getTabs().openWithMouse(Tab.MAGIC)) {
                                s.sleepWhile(() -> !s.getTabs().isOpen(Tab.MAGIC), Calculations.random(35000, 40000));
                            }
                        }
                        break;
                    case 640:
                        talkTo("Magic Instructor");
                        break;
                    case 650:
                        // Click spell
                        // Click chicken
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        } else {
                            if(s.getLocalPlayer().getTile().distance(new Tile(3139,3091,0)) > 2){
                                randomCameraMovement();
                                s.getWalking().walk(new Tile(3139,3091,0));
                                walkingSleep();
                            }
                            else{
                                if(s.getMagic().castSpellOn(WIND_STRIKE, s.getNpcs().closest("Chicken"))){
                                    s.sleepUntil(() -> s.getPlayerSettings().getConfig(TUT_PROG) != 650,Calculations.random(1600,2400));
                                }
                            }
                        }
                        break;
                    case 670:
                        // Finish tutorial island
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().getOptions() == null){
                            talkTo("Magic Instructor");
                        } else if (s.getDialogues().getOptions().length == 2) {
                            s.getDialogues().clickOption(1);
                        } else {
                            s.getDialogues().chooseOption("No, I'm not planning to do that.");
                        }
                        break;
                    case 1000:
                        s.sleep(Calculations.random(700, 950));
                        if (s.getDialogues().canContinue()){
                            s.getDialogues().clickContinue();
                            s.sleep(900,1200);
                        }
                        // Equip sword & shield
                        if (s.getTabs().openWithMouse(Tab.INVENTORY)) {
                            s.sleepWhile(() -> !s.getTabs().isOpen(Tab.INVENTORY), Calculations.random(35000, 40000));
                        }
                        Item i = s.getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot());
                        s.sleep(Calculations.random(700, 950));
                        if (i != null){
                            s.getInventory().interact("Wooden shield", "Wield");
                            s.sleepUntil(() -> s.getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()) != null,Calculations.random(1200,1600));
                        } else {
                            s.getInventory().interact("Bronze sword", "Wield");
                            s.sleepUntil(() -> s.getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()) != null,Calculations.random(1200,1600));
                        }
                        break;

                }
        }

        //randomCameraMovement();
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
        return State.DO_TUTORIAL;
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
        randomizeCounter = Calculations.random(0, 4);
        if (randomizeCounter == Calculations.random(0, 4)) {
            log("Random camera rotation");
            s.getCamera().rotateTo(Calculations.random(2400), Calculations.random(s.getClient().getLowestPitch(), 384));
            s.sleep(Calculations.random(430,780));
        }
    }

    private void randomMouseMovement() {
        randomizeCounter = Calculations.random(0, 6);
        if (randomizeCounter == Calculations.random(0, 6)) {
            if (!s.getLocalPlayer().isMoving()) {
                // Random cursor movements
                log("Random mouse movement");
                s.sleep(Calculations.random(648, 946));
                s.getMouse().move(s.getClient().getViewportTools().getRandomPointOnCanvas());
            }
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
}
