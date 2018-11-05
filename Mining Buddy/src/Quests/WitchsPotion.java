package Quests;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.NPC;
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

    // Randomize counter
    private int randomizeCounter = 0;

    // Witch's Potion completed (default false)
    private boolean witchsPotionCompleted = false;

    private int counter = 0;

    private enum State {
        START_QUEST,
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

            case COMPLETE:
                witchsPotionCompleted = true;
                s.log("Witch's Potion successfully completed!");
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

    private State getState() {
        return State.START_QUEST;

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

    public boolean isWitchsPotionCompleted() {
        return witchsPotionCompleted;
    }
}
