package Main;

import Quests.CooksAssistant;
import Quests.RomeoJuliet;
import Quests.WitchsPotion;
import Skills.MiningBot;
import Skills.Combat;
import TutorialIsland.TutorialIsland;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import javax.swing.*;
import java.awt.*;

@ScriptManifest(author = "ins1d3", category = Category.MINING, description = "It does some things.", name = "MultiScript", version = 1.1)
public class Main extends AbstractScript {
    // QUESTS
    // Cook's Assistant
    private CooksAssistant cooksAssistant;

    // Witch's Potion
    private WitchsPotion witchsPotion;

    // Romeo & Juliet
    private RomeoJuliet romeoJuliet;

    // SKILL BOTS
    // MiningBot
    private MiningBot miningBot;

    // Combat trainer
    private Combat combat;

    // Tutorial Island
    private TutorialIsland tutorialIsland;

    // Keeps script selection
    private String selectedScript;

    // VARS
    // Skills
    public boolean minerIsRunning;
    public boolean combatIsRunning;

    // Quests
    private boolean questerIsRunning;
    private boolean isCooksAssistant;
    private boolean isWitchsPotion;
    private boolean isRomeoJuliet;

    // Tutorial island
    private boolean isTutorialIsland;

    @Override
    public void onStart() {
        log("Starting Script");

        getClient().disableIdleCamera();
        getClient().disableIdleMouse();

        // Quests initialize
        cooksAssistant = new CooksAssistant(this);
        witchsPotion = new WitchsPotion(this);
        romeoJuliet = new RomeoJuliet(this);

        // Skills initialize
        miningBot = new MiningBot(this);
        combat = new Combat(this);

        // Tutorial island initialize
        tutorialIsland = new TutorialIsland(this);

        // Quests
        cooksAssistant.onStart();
        witchsPotion.onStart();
        romeoJuliet.onStart();

        // Skills
        miningBot.onStart();
        combat.onStart();

        // Tutorial island
        tutorialIsland.onStart();

        createScriptSelection();
    }

    @Override
    public int onLoop() {
        // Does Cook's Assistant Quest
        if (isCooksAssistant && questerIsRunning) {
            cooksAssistant.onLoop();
        }

        // Does Witch's Potion if Cook's Assistant is completed
        if (isWitchsPotion && questerIsRunning && cooksAssistant.isCooksAssistantCompleted()) {
            witchsPotion.onLoop();
        }

        // Does Romeo and Juliet if Cook's Assistant & Witch's Potion is completed
        if (isWitchsPotion && questerIsRunning && cooksAssistant.isCooksAssistantCompleted() && witchsPotion.isWitchsPotionCompleted()) {
            romeoJuliet.onLoop();
        }

        // Run if minerscript is launched
        if (minerIsRunning) {
            miningBot.onLoop();
        }

        if (combatIsRunning) {
            combat.onLoop();
        }

        // Run tutorial island
        if (isTutorialIsland) {
            tutorialIsland.onLoop();
        }

        return 600;
    }

    @Override
    public void onExit() {
        super.onExit();
    }

    @Override
    public void onPaint(Graphics g) {

        // Run miner paint
        if (minerIsRunning) {
            miningBot.onPaint(g);
        }
    }

    // SCRIPT SELECTION GUI
    private void createScriptSelection() {
        // CREATE SELECTION FRAME
        JFrame selectionFrame = new JFrame();
        selectionFrame.setTitle("Script selection");
        selectionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        selectionFrame.setLocationRelativeTo(getClient().getInstance().getCanvas());
        selectionFrame.setPreferredSize(new Dimension(400, 100));
        selectionFrame.getContentPane().setLayout(new BorderLayout());
        selectionFrame.pack();

        // UPPER GUI
        // Add components
        JPanel scriptPanel = new JPanel();
        scriptPanel.setLayout(new GridLayout(0, 2));

        // Script selection screen
        final JComboBox scriptComboBox = new JComboBox<>(new String[]{
                "Combat","Mining", "Questing", "Tutorial Island"
        });

        // Select script label
        JLabel selectScript = new JLabel();
        selectScript.setText("Script:");
        scriptPanel.add(selectScript);

        // Add script combo box
        scriptPanel.add(scriptComboBox);

        // Add panel to the frame
        selectionFrame.getContentPane().add(scriptPanel, BorderLayout.CENTER);

        // Lower GUI
        JPanel selectionButtonPanel = new JPanel();
        selectionButtonPanel.setLayout(new FlowLayout());

        JButton selectionButton = new JButton();
        selectionButton.setText("Select");
        selectionButton.addActionListener(e -> {
            selectedScript = String.valueOf(scriptComboBox.getSelectedItem());
            if (selectedScript == "Mining") {
                miningBot.createMiningGUI();
            }
            if (selectedScript == "Questing") {
                createQuestingGUI();
                questerIsRunning = true;

            }
            if (selectedScript == "Tutorial Island") {
                isTutorialIsland = true;
                log("isTutorialIsland: " + isTutorialIsland);
            }
            if (selectedScript == "Combat") {
                combatIsRunning = true;
                log("combatIsRunning: " + combatIsRunning);
            }
            selectionFrame.dispose();
        });
        selectionButtonPanel.add(selectionButton);
        selectionFrame.getContentPane().add(selectionButtonPanel, BorderLayout.SOUTH);
        selectionFrame.setVisible(true);
    }

    // QUESTING GUI
    private void createQuestingGUI() {
        final JComboBox questComboBox = new JComboBox<>(new String[]{
                "7 QP"
        });

        // CREATE SELECTION FRAME
        JFrame questingFrame = new JFrame();
        questingFrame.setTitle("Quest selection");
        questingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        questingFrame.setLocationRelativeTo(getClient().getInstance().getCanvas());
        questingFrame.setPreferredSize(new Dimension(400, 100));
        questingFrame.getContentPane().setLayout(new BorderLayout());
        questingFrame.pack();

        // UPPER GUI
        // Add components
        JPanel questingPanel = new JPanel();
        questingPanel.setLayout(new GridLayout(0, 2));

        // Select quest label
        JLabel selectScript = new JLabel();
        selectScript.setText("Quest:");
        questingPanel.add(selectScript);

        // Add quest combo box
        questingPanel.add(questComboBox);

        questingFrame.getContentPane().add(questingPanel, BorderLayout.CENTER);
        // Lower GUI
        JPanel selectionButtonPanel = new JPanel();
        selectionButtonPanel.setLayout(new FlowLayout());

        // Create start button
        JButton selectionButton = new JButton();
        selectionButton.setText("Start");
        selectionButton.addActionListener(e -> {
            selectedScript = String.valueOf(questComboBox.getSelectedItem());
            // Automatically get 7 QP with 3 quests
            if (selectedScript == "7 QP") {
                doCooksAssistant();
                doWitchsPotion();
                doRomeoAndJuliet();
            }
            questingFrame.dispose();
        });
        selectionButtonPanel.add(selectionButton);
        questingFrame.getContentPane().add(selectionButtonPanel, BorderLayout.SOUTH);
        questingFrame.setVisible(true);
    }

    private void doCooksAssistant() {
        isCooksAssistant = true;
    }

    private void doWitchsPotion() {
        isWitchsPotion = true;
    }

    private void doRomeoAndJuliet() {
        isRomeoJuliet = true;
    }
}
