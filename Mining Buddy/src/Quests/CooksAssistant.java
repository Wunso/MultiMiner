package Quests;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import static Quests.CooksAssistant.QuestStep.*;

public class CooksAssistant extends AbstractScript {
    // Cook's Assistant
    CooksAssistant CooksAssistant = new CooksAssistant();
    private QuestStep currentStep = RUN_START;
    // Cook's Assistant Areas
    Area CookArea = new Area(3206, 3217, 3211, 3213, 0);

    // Cooks requirements
    public static final int BUCKET_OF_MILK = 1927;
    public static final int BUCKET = 1925;
    public static final int POT_OF_FLOUR = 1933;
    public static final int EGG = 1944;
    public static final int WHEAT = 1947;
    public static final int POT = 1931;
    public static final int[] REQUIRED_ITEMS_FOR_COMPLETION = new int[]{BUCKET_OF_MILK, POT_OF_FLOUR, EGG};

    private boolean hasAlreadyRetrievedMilk;
    private boolean hasAlreadyRetrievedFlour;
    private boolean hasAlreadyRetrievedEgg;

    public enum QuestStep {
        RUN_START,
        START_QUEST,
        COOK_DIALOGUE_START_QUEST,
        CHECK_ITEMS,
        GATHER_TOOLS,
        PICK_UP_TOOLS,
        CHECK_BANK,
        TURN_IN_ITEMS,
        GATHER_EGG,
        GATHER_MILK,
        GATHER_FLOUR,
        GATHER_WHEAT,
        GRIND_WHEAT,
        /*
        The walking api can't handle going into lumbridge castle basement at this time. This will be a solution until this is resolved.
         */
        TAKE_BUCKET,
        /*
        another issue with walking to tiles when on the 3rd floor of the castle pathfinding was not taking tile Z in consideration.
         */
        RESOLVE_ITEM_PICK_UP_BANK_ISSUE,
        PULL_MILL_LEVER,
        PICKUP_FLOUR,
        PICKING_TOOL,
        FINISHED
    }

    @Override
    public void onStart() {

    }

    @Override
    public int onLoop() {
        switch (currentStep) {
            case RUN_START:
                if (CookArea.contains(getLocalPlayer())) {
                    currentStep = START_QUEST;
                } else {
                    log("Running to cooks area");
                    if (getWalking().walk(CookArea.getRandomTile())) {
                        sleep(Calculations.random(1000, 2500));
                    }
                }
            case START_QUEST:
                NPC cook = getNpcs().closest("Cook");
                if (!cook.isOnScreen()) {
                    getCamera().rotateToEntityEvent(cook);
                } else {
                    cook.interact("Talk-to");
                    currentStep = COOK_DIALOGUE_START_QUEST;
                }
            case COOK_DIALOGUE_START_QUEST:
                Dialogues dialogue = getDialogues();
                if (!dialogue.inDialogue()) {
                    currentStep = START_QUEST;
                } else if (dialogue.canContinue()) {
                    dialogue.continueDialogue();
                } else {
                    dialogue.chooseOption(dialogue.getOptionIndexContaining("I know where to") > -1 ? 4 : 1);
                }
                currentStep = CHECK_ITEMS;
            case CHECK_ITEMS:
                if (!getTabs().isOpen(Tab.INVENTORY)) {
                    getTabs().open(Tab.INVENTORY);
                    break;
                }
                Inventory inventory = getInventory();

                if (inventory.containsAll(REQUIRED_ITEMS_FOR_COMPLETION)) {
                    currentStep = TURN_IN_ITEMS;
                }

                if (!inventory.contains(BUCKET_OF_MILK) && !hasAlreadyRetrievedMilk || !inventory.contains(POT_OF_FLOUR) && !hasAlreadyRetrievedFlour || !inventory.contains(EGG) && !hasAlreadyRetrievedEgg) {
                    currentStep = GATHER_TOOLS;
                } else {
                    currentStep = TURN_IN_ITEMS;
                }
            case GATHER_TOOLS:
                inventory = getInventory();

                if (!inventory.contains(BUCKET_OF_MILK, BUCKET) && !hasAlreadyRetrievedMilk) {
                    currentStep = TAKE_BUCKET;
                }
            case TAKE_BUCKET:
                Tile current = getLocalPlayer().getTile();
                inventory = getInventory();

                if (CookArea.contains(getLocalPlayer()) && current.getY() < 9000) {
                    if (!inventory.contains(BUCKET)) {
                        GameObject trapDoor = getGameObjects().closest("Trapdoor");
                        if (!trapDoor.isOnScreen()) {

                            getCamera().rotateToEntityEvent(trapDoor);
                        }
                        trapDoor.interact("Climb-down");
                    }

                } else {
                    if (current.getY() < 9000) {
                        log("Running to cooks area");
                        if (getWalking().walk(CookArea.getRandomTile())) {
                            sleep(Calculations.random(1000, 2500));
                        }
                    }
                }

                // Find and take bucket
                if (current.getY() > 9000 && !inventory.contains(BUCKET)) {
                    GroundItem bucket = getGroundItems().closest("Bucket");
                    if (bucket != null) {
                        if (!bucket.isOnScreen()) {
                            getCamera().rotateToEntityEvent(bucket);
                        } else {
                            bucket.interact("Take");
                        }
                    }
                }

                // Go back to ladder
                if (current.getY() > 9000 && inventory.contains(BUCKET)) {
                    GameObject ladder = getGameObjects().closest("Ladder");
                    if (!ladder.isOnScreen()) {
                        getCamera().rotateToEntityEvent(ladder);
                    }
                    ladder.interact("Climb-up");
                }
                if (current.getY() < 9000 && inventory.contains(BUCKET)) {
                    currentStep = GATHER_TOOLS;
                }

        }
        return 600;
    }
}
