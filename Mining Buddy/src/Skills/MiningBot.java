package Skills;

import Main.Main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class MiningBot {
    AbstractScript s;

    public MiningBot (AbstractScript ctx) {
        this.s = ctx;
    }

    // Instantiate
    Main main = new Main();

    // MINING AREAS
    Area VarrockWestBankArea = new Area(3180,3446,3185,3433,0);
    Area VarrockWestMineArea = new Area(3183,3379,3172,3364,0);
    Area VarrockEastBankArea = new Area(3257, 3423, 3250, 3419, 0);
    Area VarrockEastMineArea = new Area(3280, 3371, 3292, 3360, 0);

    // Ore codes
    public static final int[] IDS_MINED = new int[] {7468,7469};
    public static final int[] TIN_IDS = new int[] {7486, 7485, 0, 0};
    public static final int[] COPPER_IDS = new int[] {7484, 7453, 0, 0};
    public static final int[] IRON_IDS = new int[] {7488, 7455, 0 , 0};
    public static final int[] CLAY_IDS = new int[] {7454,7487, 0, 0};
    public static final int[] SILVER_IDS = new int[] {7490, 0, 0, 0};
    public static final int[] TIN_COPPER_IDS = new int[] {7486, 7485, 7484, 7453};

    private int randomizeCounter = 0;

    // Miner strings
    private String selectedOre = "Tin";
    private String selectedPickaxe = "Bronze Pickaxe";
    private String selectedLocation = "Varrock West";

    final String[] calculatedOreAmount = new String[1];

    // Task tracking
    private int selectedOreMined = 0;

    private boolean hasNoticedFullInventory = false;
    private boolean isCooksAssistant;
    private boolean isPowermining;

    // Skill progress
    private int currentXp;
    private int currentLevel;
    private double currentLevelXp;
    private double nextLevelXp;
    private double percentTNL;


    final int[] XP_TABLE =
            {
                    0, 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154,
                    1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018,
                    5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833,
                    16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224,
                    41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721,
                    101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254,
                    224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428,
                    496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895,
                    1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068,
                    2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294,
                    4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614,
                    8771558, 9684577, 10692629, 11805606, 13034431, 200000000
            };
    // Script runtime vars
    private long timeBegan;
    private long timeRan;

    public void onStart() {
        timeBegan = System.currentTimeMillis();
        selectedOreMined = 0;
    }

    public int onLoop() {
        if (Integer.valueOf(calculatedOreAmount[0]) == selectedOreMined) {
            s.getTabs().open(Tab.LOGOUT);
            s.sleep(Calculations.random(1000, 2000));
            WidgetChild Logout = s.getWidgets().getChildWidget(182, 7);
            Logout.interact("Logout");
            s.stop();
        } else {
            if (!s.getInventory().isFull()) {
                if (selectedLocation == "Varrock West") {
                    // Check if player is in the mining area, else run to it.
                    if (VarrockWestMineArea.contains(s.getLocalPlayer())) {
                        switch (selectedOre) {
                            case "Tin":
                                mineOre(TIN_IDS, IDS_MINED);
                                break;
                            case "Iron":
                                mineOre(IRON_IDS, IDS_MINED);
                                break;
                            case "Clay":
                                mineOre(CLAY_IDS, IDS_MINED);
                                break;
                            case "Silver":
                                mineOre(SILVER_IDS, IDS_MINED);
                                break;
                        }
                    } else {
                        s.log("Running to mining area");
                        if (s.getWalking().walk(VarrockWestMineArea.getRandomTile())) {
                            s.sleep(Calculations.random(1000, 2500));
                        }
                    }
                }
                if (selectedLocation == "Varrock East") {
                    if (VarrockEastMineArea.contains(s.getLocalPlayer())) {
                        s.log(selectedOre);
                        switch (selectedOre) {
                            case "Tin":
                                mineOre(TIN_IDS, IDS_MINED);
                                break;
                            case "Copper":
                                mineOre(COPPER_IDS, IDS_MINED);
                                break;
                            case "Tin & Copper":
                                mineOre(TIN_COPPER_IDS, IDS_MINED);
                                break;
                            case "Iron":
                                mineOre(IRON_IDS, IDS_MINED);
                                break;
                        }
                    } else {
                        s.log("Running to mining area");
                        if (s.getWalking().walk(VarrockEastMineArea.getRandomTile())) {
                            s.sleep(Calculations.random(1000, 2500));
                        }
                    }
                }
            }
            // Go bank if inventory is full & not powermining
            if (s.getInventory().isFull()) {
                if (!isPowermining) { // Check for powermining
                    if (selectedLocation == "Varrock West") {
                        if (VarrockWestBankArea.contains(s.getLocalPlayer())) {
                            bank();
                        } else {
                            randomizeCounter = Calculations.random(0, 3);
                            if (randomizeCounter == Calculations.random(0, 3)) {
                                if (hasNoticedFullInventory == true) {
                                    if (s.getWalking().walk(VarrockWestBankArea.getCenter())) {
                                        s.sleep(Calculations.random(1000, 2500));
                                    }
                                } else {
                                    switch (selectedOre)  {
                                        case "Tin":
                                            mineOre(TIN_IDS, IDS_MINED);
                                            break;
                                        case "Iron":
                                            mineOre(IRON_IDS, IDS_MINED);
                                            break;
                                        case "Clay":
                                            mineOre(CLAY_IDS, IDS_MINED);
                                            break;
                                        case "Silver":
                                            mineOre(SILVER_IDS, IDS_MINED);
                                            break;
                                    }
                                    hasNoticedFullInventory = true;
                                }
                            } else {
                                if (s.getWalking().walk(VarrockWestBankArea.getCenter())) {
                                    s.sleep(Calculations.random(1000, 2500));
                                }
                            }
                        }
                    }
                    if (selectedLocation == "Varrock East") {
                        if (VarrockEastBankArea.contains(s.getLocalPlayer())) {
                            bank();
                        } else {
                            randomizeCounter = Calculations.random(0, 3);
                            if (randomizeCounter == Calculations.random(0, 3)) {
                                if (hasNoticedFullInventory == true) {
                                    if (s.getWalking().walk(VarrockEastBankArea.getCenter())) {
                                        s.sleep(Calculations.random(1000, 2500));
                                    }
                                } else {
                                    s.log(selectedOre);
                                    switch(selectedOre) {
                                        case "Tin":
                                            mineOre(TIN_IDS, IDS_MINED);
                                            break;
                                        case "Copper":
                                            mineOre(COPPER_IDS, IDS_MINED);
                                            break;
                                        case "Tin & Copper":
                                            mineOre(TIN_COPPER_IDS, IDS_MINED);
                                            break;
                                        case "Iron":
                                            mineOre(IRON_IDS, IDS_MINED);
                                            break;
                                    }
                                    hasNoticedFullInventory = true;
                                }
                            } else {
                                if (s.getWalking().walk(VarrockEastBankArea.getCenter())) {
                                    s.sleep(Calculations.random(1000, 2500));
                                }
                            }
                        }
                    }
                } else {
                    // Drop everything in inventory if inventory is full
                    s.getInventory().dropAll();
                }
            }

            /// Random movements
            // Move the camera and mouse randomly
            randomizeCounter = Calculations.random(0, 8);
            if (randomizeCounter == Calculations.random(0, 8)) {
                s.log("Random camera rotation");
                s.getCamera().rotateTo(Calculations.random(2400), Calculations.random(s.getClient().getLowestPitch(), 384));
                s.sleep(Calculations.random(430,780));
            }
            randomizeCounter = Calculations.random(0, 6);
            if (randomizeCounter == Calculations.random(0, 6)) {
                if (!s.getLocalPlayer().isMoving()) {
                    // Random cursor movements
                    s.log("Random mouse movement");
                    s.sleep(Calculations.random(648, 946));
                    s.getMouse().move(s.getClient().getViewportTools().getRandomPointOnCanvas());
                }
            }
        }
        return 600;
    }

    private void mineOre(int[] VEIN_ID, int[] VEIN_ID_MINED) {
        // Mining Ore
        GameObject OreVein = s.getGameObjects().closest(gameObject -> gameObject != null && gameObject.getName().equals("Rocks") && (gameObject.getID() == VEIN_ID[0] || gameObject.getID() == VEIN_ID[1] || gameObject.getID() == VEIN_ID[2] || gameObject.getID() == VEIN_ID[3]));
        GameObject NextOreVein = s.getGameObjects().closest(gameObject -> gameObject != null && gameObject.getName().equals("Rocks") && (gameObject.getID() == VEIN_ID[0] || gameObject.getID() == VEIN_ID[1] || gameObject.getID() == VEIN_ID[2] || gameObject.getID() == VEIN_ID[3]) && OreVein.getTile() != gameObject.getTile());
        if (s.getLocalPlayer().getAnimation() == 625) {
            randomizeCounter = Calculations.random(0, 5);
            if (randomizeCounter == Calculations.random(0, 5)) {
                s.log("Random mouse movement");
                s. sleep(Calculations.random(250, 648));
                s.getMouse().move(s.getClient().getViewportTools().getRandomPointOnCanvas());
            }
            if (s.sleepUntil(() -> (OreVein.getID() == VEIN_ID_MINED[0] || OreVein.getID() == VEIN_ID_MINED[1]), Calculations.random(1000, 2000))) {
                if(NextOreVein != null && NextOreVein.isOnScreen()) {
                    if (NextOreVein.getClickablePoint() != null) {
                        if (NextOreVein.interact("Mine")) {
                            int Ores = s.getInventory().count(selectedOre);
                            s.log("Ores 2: " + Ores);
                            s.sleepUntil(() -> s.getInventory().count(selectedOre) > Ores || (NextOreVein.getID() == VEIN_ID_MINED[0] || NextOreVein.getID() == VEIN_ID_MINED[1]), Calculations.random(10000, 12000));
                            s.log("Ores 2 updated: " + s.getInventory().count(selectedOre));
                        }
                    } else {
                        s.getCamera().mouseRotateTo(Calculations.random(0,2048),Calculations.random(0,2048));
                    }
                }
            }
        }
        if ((OreVein.getID() == VEIN_ID[0] || OreVein.getID() == VEIN_ID[1] || OreVein.getID() == VEIN_ID[2] || OreVein.getID() == VEIN_ID[3]) && s.getLocalPlayer().getAnimation() == -1) {
            if (OreVein.isOnScreen()) {
                if (OreVein.getClickablePoint() != null) {
                    if (OreVein.interact("Mine")) {
                        randomizeCounter = Calculations.random(0, 2);
                        if (randomizeCounter == Calculations.random(0, 2)) {
                            s.sleep(Calculations.random(625, 985));
                            if (s.sleepUntil(() -> !s.getLocalPlayer().isMoving(), Calculations.random(4000, 6000))) {
                                s. log("Selecting next vein");
                                s.getMouse().move(NextOreVein.getTile());
                                s.sleep(Calculations.random(625, 985));
                            }
                        }
                        int Ores = s.getInventory().count(selectedOre);
                        s.log("Ores 1: " + Ores);
                        s.sleepUntil(() -> s.getInventory().count(selectedOre) > Ores || (OreVein.getID() == VEIN_ID_MINED[0] || OreVein.getID() == VEIN_ID_MINED[1]), Calculations.random(2000, 2500));
                        s.log("Ores 1 updated: " + s.getInventory().count(selectedOre));
                    }
                } else {
                    s.getCamera().mouseRotateTo(Calculations.random(0, 2048), Calculations.random(0, 2048));
                }
            }
        }
    }

    private void bank() {
        NPC banker = s.getNpcs().closest(npc -> npc != null && npc.hasAction("Bank"));
        if (banker.interact("Bank")) {
            s.log("Interacted with banker");
            if (s.sleepUntil(() -> s.getBank().isOpen(), Calculations.random(4000,7000))) {
                s.sleep(Calculations.random(700, 950));
                if (s.getBank().depositAllItems()) {
                    s.log("Deposited all items");
                    s.sleep(Calculations.random(700, 950));
                    if (s.sleepUntil(() -> !s.getInventory().isFull(), Calculations.random(6000, 8000))) {
                        s.sleep(Calculations.random(700, 950));
                    }
                }
            }
        }
    }
    // MINING GUI

    public void createMiningGUI() {
        // CREATE MINING FRAME
        final JComboBox oreComboBox = new JComboBox<>(new String[]{
                "Tin ore", "Iron ore", "Clay", "Silver ore"
        });
        final JComboBox pickaxeComboBox = new JComboBox<>(new String[]{
                "Bronze pickaxe", "Iron pickaxe", "Steel pickaxe", "Mithril pickaxe", "Adamantite pickaxe", "Rune pickaxe", "Dragon pickaxe"
        });
        final JComboBox locationComboBox = new JComboBox<>(new String[]{
                "Varrock West", "Varrock East"
        });

        final JLabel calculatedOreAmountLabel = new JLabel();

        final DefaultComboBoxModel varrockWestOreModel = new DefaultComboBoxModel(new String[]{"Tin ore", "Iron ore", "Clay", "Silver ore"});
        final DefaultComboBoxModel varrockEastOreModel = new DefaultComboBoxModel(new String[]{"Tin ore", "Copper ore", "Iron ore"});


        JFrame miningFrame = new JFrame();
        miningFrame.setTitle("OSRS MultiMiner");
        miningFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        miningFrame.setLocationRelativeTo(s.getClient().getInstance().getCanvas());
        miningFrame.setPreferredSize(new Dimension(400, 200));
        miningFrame.getContentPane().setLayout(new BorderLayout());
        miningFrame.pack();
        //miningFrame.setVisible(true);

        // Upper GUI
        // Add components to miningFrame
        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new GridLayout(0, 2));

        // Select location label
        JLabel selectLocation = new JLabel();
        selectLocation.setText("Location:");
        settingPanel.add(selectLocation);

        // Add location combo box
        locationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (locationComboBox.getSelectedItem() == "Varrock West") {
                    oreComboBox.setModel(varrockWestOreModel);
                    s.log("Setting varrock west combobox");
                } else if (locationComboBox.getSelectedItem() == "Varrock East") {
                    oreComboBox.setModel(varrockEastOreModel);
                }
                selectedLocation = String.valueOf(locationComboBox.getSelectedItem());
            }
        });
        settingPanel.add(locationComboBox);

        // Select ore label
        JLabel selectOreLabel = new JLabel();
        selectOreLabel.setText("Select ore(s):");
        settingPanel.add(selectOreLabel);

        // Add ore combo box
        oreComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedOre = String.valueOf(oreComboBox.getSelectedItem());
            }
        });
        settingPanel.add(oreComboBox);

        // Select pickaxe label
        JLabel selectPickaxeLabel = new JLabel();
        selectPickaxeLabel.setText("Select pickaxe:");
        settingPanel.add(selectPickaxeLabel);

        // Add pickaxe combo box
        pickaxeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedPickaxe = String.valueOf(pickaxeComboBox.getSelectedItem());
            }
        });
        settingPanel.add(pickaxeComboBox);

        // Amount of ores label
        JLabel oreAmount = new JLabel();
        oreAmount.setText("Amount to mine:");
        settingPanel.add(oreAmount);

        // Textbox amount of ores
        JTextField txtOreAmount = new JTextField();
        txtOreAmount.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (selectedOre == "Tin") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " tin ore(s)");
                    }
                }
                if (selectedOre == "Copper") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " copper ore(s)");
                    }
                }
                if (selectedOre == "Clay") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " clay");
                    }
                }
                if (selectedOre == "Iron") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " iron ore(s)");
                    }
                }
                if (selectedOre == "Silver") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " silver ore(s)");
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (selectedOre == "Tin") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " tin ore(s)");
                    }
                }
                if (selectedOre == "Copper") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " copper ore(s)");
                    }
                }
                if (selectedOre == "Clay") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " clay");
                    }
                }
                if (selectedOre == "Iron") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " iron ore(s)");
                    }
                }
                if (selectedOre == "Silver") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " silver ore(s)");
                    }
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (selectedOre == "Tin") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " tin ore(s)");
                    }
                }
                if (selectedOre == "Copper") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " copper ore(s)");
                    }
                }
                if (selectedOre == "Clay") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " clay");
                    }
                }
                if (selectedOre == "Iron") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " iron ore(s)");
                    }
                }
                if (selectedOre == "Silver") {
                    calculatedOreAmount[0] = txtOreAmount.getText();
                    if (calculatedOreAmount[0].matches("^[0-9]*$")) {
                        calculatedOreAmountLabel.setText(calculatedOreAmount[0] + " silver ore(s)");
                    }
                }
            }
        });
        settingPanel.add(txtOreAmount);

        JLabel calculation = new JLabel();
        calculation.setText("Total ores: ");
        settingPanel.add(calculation);

        settingPanel.add(calculatedOreAmountLabel);

        // Add mode checkbox
        JCheckBox modeCheckBox = new JCheckBox();
        modeCheckBox.setText("Powermining");
        modeCheckBox.addActionListener(
                e -> isPowermining = modeCheckBox.isSelected());
        settingPanel.add(modeCheckBox);

        miningFrame.getContentPane().add(settingPanel, BorderLayout.CENTER);
        // Lower GUI
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton startButton = new JButton();
        startButton.setText("Start");
        startButton.addActionListener(e -> {
            main.minerIsRunning = true;
            miningFrame.dispose();
        });
        buttonPanel.add(startButton);
        miningFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        miningFrame.setVisible(true);
    }

    public void onPaint(Graphics g) {
        s.onPaint(g);
        Graphics2D g2d = (Graphics2D) g;

        currentXp = s.getSkills().getExperience(Skill.MINING);
        currentLevel = s.getSkills().getRealLevel(Skill.MINING);
        currentLevelXp = XP_TABLE[currentLevel];
        nextLevelXp = XP_TABLE[currentLevel + 1];
        percentTNL = ((currentXp - currentLevelXp) / (nextLevelXp - currentLevelXp) * 100);

        // Circular progress bar test
        int alpha = 127; // 50% transparent
        Color fadedGray = new Color(59, 84, 89, 235);
        Color fadedGreen = new Color(99, 255, 115, alpha);

        Double d = new Double(-percentTNL * 3.6);
        int percentage = d.intValue();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        DecimalFormat df = new DecimalFormat("#.#");
        int progressRadius = 60;
        int borderThickness = 10;

        g2d.setColor(fadedGreen);
        g2d.fillArc((s.getClient().getInstance().getCanvas().getWidth() - s.getClient().getInstance().getCanvas().getWidth() / 3) / 2, 0, progressRadius, progressRadius, 90, percentage);
        g2d.setColor(fadedGray);
        g2d.fillArc(((s.getClient().getInstance().getCanvas().getWidth() - s.getClient().getInstance().getCanvas().getWidth() / 3) / 2) + (borderThickness / 2), (borderThickness / 2), progressRadius - borderThickness, progressRadius - borderThickness, 90, 360);
        String percentageString = df.format(percentTNL) + "%";
        g2d.setColor(Color.WHITE);
        g2d.drawString(percentageString, ((s.getClient().getInstance().getCanvas().getWidth() - s.getClient().getInstance().getCanvas().getWidth() / 3) / 2) + 5, progressRadius / 2);

        // Draw progress bar
            /*g.setColor(Color.GREEN);
            DecimalFormat df = new DecimalFormat("#.#");
            double width = ((((getClient().getInstance().getCanvas().getWidth()) - getClient().getInstance().getCanvas().getWidth() / 3) - 3) * 60 / 100);
            int intWidth = (int) width;
            g.fillRect(5,((getClient().getInstance().getCanvas().getHeight() / 3) * 2) - 10, intWidth, 10);

            // Set percentage in progress bar
            g.setColor(Color.YELLOW);
            g.drawString(df.format(percentTNL) + "%", ((getClient().getInstance().getCanvas().getWidth() - getClient().getInstance().getCanvas().getWidth() / 3) / 2) - 3, (getClient().getInstance().getCanvas().getHeight() / 3) * 2);
            */

        // Draw script runtime time
        g.setColor(Color.YELLOW);
        timeRan = System.currentTimeMillis() - this.timeBegan;
        g.drawString(ft(timeRan), 5, ((s.getClient().getInstance().getCanvas().getHeight() / 3) * 2));

        // Draw ores mined
        g.drawString("Total ores mined: " + selectedOreMined, 5, ((s.getClient().getInstance().getCanvas().getHeight() / 3) * 2) - 10);
    }

    private String ft(long duration){
        String res = "";
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                .toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                .toMinutes(duration));
        if (days == 0) {
            res = (hours + ":" + minutes + ":" + seconds);
        } else {
            res = (days + ":" + hours + ":" + minutes + ":" + seconds);
        }
        return res;
    }
}
