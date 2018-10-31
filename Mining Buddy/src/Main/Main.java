package Main;

import Quests.CooksAssistant;
import Skills.MiningBot;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import javax.swing.*;
import java.awt.*;

@ScriptManifest(author = "ins1d3", category = Category.MINING, description = "It does some things.", name = "MultiScript", version = 1.0)
public class Main extends AbstractScript {
    // Cook's Assistant
    CooksAssistant CooksAssistant = new CooksAssistant();

    // MiningBot
    MiningBot MiningBot = new MiningBot();

    // Keeps script selection
    private String selectedScript;

    public boolean minerIsRunning;
    private boolean questerIsRunning;
    private boolean isCooksAssistant;

    @Override
    public void onStart() {
        getClient().disableIdleCamera();
        getClient().disableIdleMouse();

        MiningBot.onStart();
        CooksAssistant.onStart();

        createScriptSelection();
    }

    @Override
    public int onLoop() {

        // Does Cook's Assistant Quest
        if (isCooksAssistant && questerIsRunning) {
            CooksAssistant.onLoop();
        }

        // Run if minerscript is launched
        if (minerIsRunning) {
            MiningBot.onLoop();
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
            MiningBot.onPaint(g);
        }
    }

    // SCRIPT SELECTION GUI
    private void createScriptSelection() {
        // CREATE SELECTION FRAME
        JFrame selectionFrame = new JFrame();
        selectionFrame.setTitle("Select your script");
        selectionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        selectionFrame.setLocationRelativeTo(getClient().getInstance().getCanvas());
        selectionFrame.setPreferredSize(new Dimension(400, 100));
        selectionFrame.getContentPane().setLayout(new BorderLayout());
        selectionFrame.pack();

        // UPPER GUI
        // Add components
        JPanel scriptPanel = new JPanel();
        scriptPanel.setLayout(new GridLayout(0, 2));

        final JComboBox scriptComboBox = new JComboBox<>(new String[]{
                "Mining", "Questing"
        });

        // Select script label
        JLabel selectScript = new JLabel();
        selectScript.setText("Script:");
        scriptPanel.add(selectScript);

        // Add script combo box
        scriptPanel.add(scriptComboBox);

        selectionFrame.getContentPane().add(scriptPanel, BorderLayout.CENTER);
        // Lower GUI
        JPanel selectionButtonPanel = new JPanel();
        selectionButtonPanel.setLayout(new FlowLayout());

        JButton selectionButton = new JButton();
        selectionButton.setText("Select");
        selectionButton.addActionListener(e -> {
            selectedScript = String.valueOf(scriptComboBox.getSelectedItem());
            if (selectedScript == "Mining") {
                MiningBot.createMiningGUI();
            }
            if (selectedScript == "Questing") {
                createQuestingGUI();
                questerIsRunning = true;

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
        questingFrame.setTitle("Select your script");
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
        selectScript.setText("quest:");
        questingPanel.add(selectScript);

        // Add quest combo box
        questingPanel.add(questComboBox);

        questingFrame.getContentPane().add(questingPanel, BorderLayout.CENTER);
        // Lower GUI
        JPanel selectionButtonPanel = new JPanel();
        selectionButtonPanel.setLayout(new FlowLayout());

        JButton selectionButton = new JButton();
        selectionButton.setText("Start");
        selectionButton.addActionListener(e -> {
            selectedScript = String.valueOf(questComboBox.getSelectedItem());
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

    }

    private void doRomeoAndJuliet() {

    }
}
