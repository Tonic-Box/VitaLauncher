package com.tonic.launcher.ui;

import com.tonic.launcher.util.LauncherConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.SpinnerNumberModel;

public class LauncherSettingsPanel extends JFrame {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 650;

    private static final Color BACKGROUND_COLOR = new Color(30, 35, 45);
    private static final Color PANEL_COLOR = new Color(40, 45, 55);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(150, 160, 180);
    private static final Color BUTTON_COLOR = new Color(70, 130, 200);
    private static final Color BUTTON_HOVER_COLOR = new Color(90, 150, 220);
    private final LauncherConfig config = new LauncherConfig();

    // Checkboxes
    private JCheckBox noPluginsCheckbox;
    private JCheckBox minCheckbox;
    private JCheckBox noMusicCheckbox;
    private JCheckBox incognitoCheckbox;
    private JCheckBox rsdumpCheckbox;
    private JCheckBox proxyCheckbox;
    private JCheckBox mouseHookCheckbox;

    // Text fields
    private JTextField rsdumpField;
    private JTextField proxyField;
    private JTextField loginField;

    // Combo box
    private JComboBox<String> loginTypeCombo;

    // Memory settings
    private JCheckBox maxMemoryCheckbox;
    private JSpinner maxMemorySpinner;

    // Login hint label
    private JLabel loginHintLabel;

    // JVM Args
    private DefaultListModel<String> jvmArgsListModel;
    private JList<String> jvmArgsList;

    // Launch callback
    private LaunchCallback launchCallback;

    private Point initialClick;

    public LauncherSettingsPanel() {
        setTitle("VitaLite Launcher");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);

        initComponents();

        // Make window rounded
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, 20, 20));
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Title panel with drag support
        JPanel titlePanel = createTitlePanel();
        makeDraggable(titlePanel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Settings panel with wrapper to show border
        JPanel settingsWrapper = new JPanel(new BorderLayout());
        settingsWrapper.setBackground(BACKGROUND_COLOR);
        settingsWrapper.setBorder(new EmptyBorder(0, 0, 10, 0));
        JPanel settingsPanel = createSettingsPanel();
        settingsWrapper.add(settingsPanel, BorderLayout.NORTH);
        mainPanel.add(settingsWrapper, BorderLayout.CENTER);

        // Launch button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 10, 20));
        panel.setLayout(new BorderLayout());

        // Close button
        JButton closeButton = createCloseButton();
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closePanel.setBackground(BACKGROUND_COLOR);
        closePanel.add(closeButton);

        // Title content
        JPanel titleContent = new JPanel();
        titleContent.setBackground(BACKGROUND_COLOR);
        titleContent.setLayout(new BoxLayout(titleContent, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("VitaLite");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(150, 200, 255));

        JLabel subtitleLabel = new JLabel("Launch Settings");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(LABEL_COLOR);

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleContent.add(titleLabel);
        titleContent.add(Box.createVerticalStrut(5));
        titleContent.add(subtitleLabel);

        panel.add(closePanel, BorderLayout.NORTH);
        panel.add(titleContent, BorderLayout.CENTER);

        return panel;
    }

    private JButton createCloseButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Draw X
                g2d.setColor(getForeground());
                g2d.setStroke(new BasicStroke(2));
                int padding = 10;
                g2d.drawLine(padding, padding, getWidth() - padding, getHeight() - padding);
                g2d.drawLine(getWidth() - padding, padding, padding, getHeight() - padding);
            }
        };

        button.setPreferredSize(new Dimension(35, 35));
        button.setForeground(new Color(150, 160, 180));
        button.setBackground(new Color(40, 45, 55));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(220, 60, 60));
                button.setForeground(Color.WHITE);
                button.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(40, 45, 55));
                button.setForeground(new Color(150, 160, 180));
                button.repaint();
            }
        });

        button.addActionListener(e -> System.exit(0));

        return button;
    }

    private void makeDraggable(JPanel panel) {
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                // Get location of window
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                setLocation(X, Y);
            }
        });
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setLayout(new BorderLayout(20, 0));

        // Boolean options in two columns
        JPanel booleanOptionsPanel = new JPanel(new GridLayout(2, 2, 15, 10));
        booleanOptionsPanel.setBackground(PANEL_COLOR);

        noPluginsCheckbox = createStyledCheckbox("Disable Plugins", "Disables loading of core plugins");
        minCheckbox = createStyledCheckbox("Minimal Memory", "Runs JVM with minimal allocated memory");
        noMusicCheckbox = createStyledCheckbox("Disable Music", "Prevent the loading of music tracks");
        incognitoCheckbox = createStyledCheckbox("Incognito Mode", "Visually display as 'RuneLite' instead of 'VitaLite'");
        mouseHookCheckbox = createStyledCheckbox("Disable RL DLL", "Disable RuneLites mouse hook DLL from loading or being called.");

        //load default states
        noPluginsCheckbox.setSelected(config.isNoPlugins());
        minCheckbox.setSelected(config.isMin());
        noMusicCheckbox.setSelected(config.isNoMusic());
        incognitoCheckbox.setSelected(config.isIncognito());
        mouseHookCheckbox.setSelected(config.isMouseHook());

        //listeners
        noPluginsCheckbox.addActionListener(e -> config.setNoPlugins(noPluginsCheckbox.isSelected()));
        minCheckbox.addActionListener(e -> config.setMin(minCheckbox.isSelected()));
        noMusicCheckbox.addActionListener(e -> config.setNoMusic(noMusicCheckbox.isSelected()));
        incognitoCheckbox.addActionListener(e -> config.setIncognito(incognitoCheckbox.isSelected()));
        mouseHookCheckbox.addActionListener(e -> config.setMouseHook(mouseHookCheckbox.isSelected()));

        booleanOptionsPanel.add(noPluginsCheckbox);
        booleanOptionsPanel.add(minCheckbox);
        booleanOptionsPanel.add(noMusicCheckbox);
        booleanOptionsPanel.add(incognitoCheckbox);
        booleanOptionsPanel.add(mouseHookCheckbox);

        // Memory settings panel
        JPanel memoryPanel = createMemoryPanel();

        // String options with text fields
        JPanel stringOptionsPanel = new JPanel();
        stringOptionsPanel.setLayout(new BoxLayout(stringOptionsPanel, BoxLayout.Y_AXIS));
        stringOptionsPanel.setBackground(PANEL_COLOR);

        FieldPanelComponents rsdumpComponents = createFieldPanel("RS Dump Path:", "Path to dump the gamepack to");
        rsdumpCheckbox = rsdumpComponents.checkbox;
        rsdumpField = rsdumpComponents.textField;
        rsdumpCheckbox.setSelected(config.isRsDump());
        rsdumpField.setText(config.getRsDumpPath());
        rsdumpCheckbox.addActionListener(e -> config.setRsDump(rsdumpCheckbox.isSelected()));
        rsdumpField.getDocument().addDocumentListener((SimpleDocumentListener) e -> config.setRsDumpPath(rsdumpField.getText()));
        stringOptionsPanel.add(rsdumpComponents.panel);
        stringOptionsPanel.add(Box.createVerticalStrut(12));

        FieldPanelComponents proxyComponents = createFieldPanel("Proxy Server:", "ip:port or ip:port:username:password");
        proxyCheckbox = proxyComponents.checkbox;
        proxyField = proxyComponents.textField;
        proxyCheckbox.addActionListener(e -> config.setProxy(proxyCheckbox.isSelected()));
        proxyField.getDocument().addDocumentListener((SimpleDocumentListener) e -> config.setProxyData(proxyField.getText()));
        proxyCheckbox.setSelected(config.isProxy());
        proxyField.setText(config.getProxyData());
        stringOptionsPanel.add(proxyComponents.panel);

        // Login section
        stringOptionsPanel.add(Box.createVerticalStrut(12));
        JPanel loginPanel = createLoginPanel();
        stringOptionsPanel.add(loginPanel);

        // Login hint label
        stringOptionsPanel.add(Box.createVerticalStrut(3));
        JPanel loginHintPanel = createLoginHintPanel();
        stringOptionsPanel.add(loginHintPanel);

        // JVM Args section
        stringOptionsPanel.add(Box.createVerticalStrut(15));
        JPanel jvmArgsPanel = createJvmArgsPanel();
        stringOptionsPanel.add(jvmArgsPanel);

        // Add sections to main panel with spacing
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(PANEL_COLOR);
        topSection.add(booleanOptionsPanel);
        topSection.add(Box.createVerticalStrut(15));
        topSection.add(memoryPanel);
        topSection.add(Box.createVerticalStrut(20));

        panel.add(topSection, BorderLayout.NORTH);
        panel.add(stringOptionsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMemoryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(PANEL_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Checkbox
        maxMemoryCheckbox = new JCheckBox("Max Memory:");
        maxMemoryCheckbox.setFont(new Font("Arial", Font.PLAIN, 13));
        maxMemoryCheckbox.setForeground(TEXT_COLOR);
        maxMemoryCheckbox.setBackground(PANEL_COLOR);
        maxMemoryCheckbox.setFocusPainted(false);
        maxMemoryCheckbox.setToolTipText("Set maximum JVM memory (-Xmx)");
        maxMemoryCheckbox.setSelected(config.isMaxMemoryEnabled());
        maxMemoryCheckbox.addActionListener(e -> {
            config.setMaxMemoryEnabled(maxMemoryCheckbox.isSelected());
            maxMemorySpinner.setEnabled(maxMemoryCheckbox.isSelected());
        });

        int memoryValue = config.getMaxMemoryValue();
        memoryValue = Math.max(768, Math.min(8192, memoryValue));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                memoryValue, // initial value
                320,  // min
                8192, // max
                64    // step
        );
        maxMemorySpinner = new JSpinner(spinnerModel);
        maxMemorySpinner.setFont(new Font("Arial", Font.PLAIN, 12));
        maxMemorySpinner.setPreferredSize(new Dimension(80, 25));
        maxMemorySpinner.setEnabled(config.isMaxMemoryEnabled());
        maxMemorySpinner.addChangeListener(e -> {
            config.setMaxMemoryValue((Integer) maxMemorySpinner.getValue());
        });

        // Style spinner
        JComponent editor = maxMemorySpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
            spinnerEditor.getTextField().setBackground(new Color(50, 55, 65));
            spinnerEditor.getTextField().setForeground(TEXT_COLOR);
            spinnerEditor.getTextField().setCaretColor(TEXT_COLOR);
        }

        // MB label
        JLabel mbLabel = new JLabel(" MB");
        mbLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        mbLabel.setForeground(LABEL_COLOR);

        panel.add(maxMemoryCheckbox);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(maxMemorySpinner);
        panel.add(mbLabel);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 0));
        panel.setBackground(PANEL_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Label
        JLabel label = new JLabel("Login:");
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        label.setForeground(TEXT_COLOR);
        label.setPreferredSize(new Dimension(120, 25));

        // Text field
        loginField = new JTextField();
        loginField.setFont(new Font("Arial", Font.PLAIN, 12));
        loginField.setBackground(new Color(50, 55, 65));
        loginField.setForeground(TEXT_COLOR);
        loginField.setCaretColor(TEXT_COLOR);
        loginField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 65, 75)),
            new EmptyBorder(5, 8, 5, 8)
        ));
        loginField.setToolTipText("Email or username for login");
        loginField.setText(config.getLoginString());
        loginField.getDocument().addDocumentListener((SimpleDocumentListener) e -> config.setLoginString(loginField.getText()));

        // Combo box
        String[] loginTypes = {"Legacy", "Jagex"};
        loginTypeCombo = new JComboBox<>(loginTypes);
        loginTypeCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        loginTypeCombo.setBackground(new Color(50, 55, 65));
        loginTypeCombo.setForeground(TEXT_COLOR);
        loginTypeCombo.setFocusable(false);
        loginTypeCombo.setPreferredSize(new Dimension(100, 30));
        loginTypeCombo.setSelectedItem(config.getLoginType());
        loginTypeCombo.addActionListener(e -> {
            config.setLoginType((String) loginTypeCombo.getSelectedItem());
            updateLoginHintText();
        });

        // Combo box styling
        loginTypeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(70, 130, 200) : new Color(50, 55, 65));
                setForeground(TEXT_COLOR);
                setBorder(new EmptyBorder(5, 8, 5, 8));
                return this;
            }
        });

        // Layout
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(PANEL_COLOR);
        leftPanel.add(label);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 0));
        centerPanel.setBackground(PANEL_COLOR);
        centerPanel.add(loginField, BorderLayout.CENTER);
        centerPanel.add(loginTypeCombo, BorderLayout.EAST);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLoginHintPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        loginHintLabel = new JLabel();
        loginHintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        loginHintLabel.setForeground(LABEL_COLOR);

        // Set initial text based on current login type
        updateLoginHintText();

        panel.add(loginHintLabel, BorderLayout.WEST);

        return panel;
    }

    private void updateLoginHintText() {
        if (loginHintLabel == null) return;

        String loginType = (String) loginTypeCombo.getSelectedItem();
        if ("Legacy".equals(loginType)) {
            loginHintLabel.setText("Format: user:pass");
        } else if ("Jagex".equals(loginType)) {
            loginHintLabel.setText("Format: sessionID:characterID:displayName (or path to RL credentials file)");
        }
    }

    private JPanel createJvmArgsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 8));
        panel.setBackground(PANEL_COLOR);

        // Title label
        JLabel titleLabel = new JLabel("JVM Arguments:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_COLOR);

        // Initialize list model and load from config
        jvmArgsListModel = new DefaultListModel<>();
        List<String> savedArgs = config.getJvmArgs();
        for (String arg : savedArgs) {
            jvmArgsListModel.addElement(arg);
        }

        // Create list
        jvmArgsList = new JList<>(jvmArgsListModel);
        jvmArgsList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        jvmArgsList.setBackground(new Color(50, 55, 65));
        jvmArgsList.setForeground(TEXT_COLOR);
        jvmArgsList.setSelectionBackground(new Color(70, 130, 200));
        jvmArgsList.setSelectionForeground(Color.WHITE);
        jvmArgsList.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Scroll pane for list
        JScrollPane scrollPane = new JScrollPane(jvmArgsList);
        scrollPane.setPreferredSize(new Dimension(0, 120));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 65, 75)));
        scrollPane.setBackground(new Color(50, 55, 65));

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonsPanel.setBackground(PANEL_COLOR);

        JButton addButton = createSmallButton("Add");
        JButton removeButton = createSmallButton("Remove");
        JButton resetButton = createSmallButton("Reset to Defaults");

        addButton.addActionListener(e -> addJvmArg());
        removeButton.addActionListener(e -> removeSelectedJvmArg());
        resetButton.addActionListener(e -> resetJvmArgsToDefaults());

        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);
        buttonsPanel.add(resetButton);

        // Assemble panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PANEL_COLOR);
        topPanel.add(titleLabel, BorderLayout.WEST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setBackground(new Color(60, 70, 85));
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(5, 10, 5, 10));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 70, 85));
            }
        });

        return button;
    }

    private void addJvmArg() {
        String input = JOptionPane.showInputDialog(
                this,
                "Enter JVM argument:",
                "Add JVM Argument",
                JOptionPane.PLAIN_MESSAGE
        );

        if (input != null && !input.trim().isEmpty()) {
            jvmArgsListModel.addElement(input.trim());
            saveJvmArgsToConfig();
        }
    }

    private void removeSelectedJvmArg() {
        int selectedIndex = jvmArgsList.getSelectedIndex();
        if (selectedIndex != -1) {
            jvmArgsListModel.remove(selectedIndex);
            saveJvmArgsToConfig();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an argument to remove.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void resetJvmArgsToDefaults() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Reset JVM arguments to defaults?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            jvmArgsListModel.clear();
            List<String> defaults = LauncherConfig.getDefaultJvmArgs();
            for (String arg : defaults) {
                jvmArgsListModel.addElement(arg);
            }
            saveJvmArgsToConfig();
        }
    }

    private void saveJvmArgsToConfig() {
        List<String> args = new ArrayList<>();
        for (int i = 0; i < jvmArgsListModel.size(); i++) {
            args.add(jvmArgsListModel.getElementAt(i));
        }
        config.setJvmArgs(args);
    }

    private JCheckBox createStyledCheckbox(String text, String tooltip) {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setFont(new Font("Arial", Font.PLAIN, 13));
        checkbox.setForeground(TEXT_COLOR);
        checkbox.setBackground(PANEL_COLOR);
        checkbox.setFocusPainted(false);
        checkbox.setToolTipText(tooltip);
        return checkbox;
    }

    private FieldPanelComponents createFieldPanel(String labelText, String placeholder) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 0));
        panel.setBackground(PANEL_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JCheckBox checkbox = new JCheckBox();
        checkbox.setBackground(PANEL_COLOR);
        checkbox.setFocusPainted(false);

        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setBackground(new Color(50, 55, 65));
        textField.setForeground(TEXT_COLOR);
        textField.setCaretColor(TEXT_COLOR);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 65, 75)),
            new EmptyBorder(5, 8, 5, 8)
        ));
        textField.setEnabled(false);
        textField.setToolTipText(placeholder);

        checkbox.addActionListener(e -> textField.setEnabled(checkbox.isSelected()));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        label.setForeground(TEXT_COLOR);
        label.setPreferredSize(new Dimension(120, 25));

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelPanel.setBackground(PANEL_COLOR);
        labelPanel.add(checkbox);
        labelPanel.add(Box.createHorizontalStrut(5));
        labelPanel.add(label);

        panel.add(labelPanel, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);

        return new FieldPanelComponents(panel, checkbox, textField);
    }

    private static class FieldPanelComponents {
        JPanel panel;
        JCheckBox checkbox;
        JTextField textField;

        FieldPanelComponents(JPanel panel, JCheckBox checkbox, JTextField textField) {
            this.panel = panel;
            this.checkbox = checkbox;
            this.textField = textField;
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 20, 20, 20));

        JButton launchButton = new JButton("Launch");
        launchButton.setFont(new Font("Arial", Font.BOLD, 16));
        launchButton.setPreferredSize(new Dimension(150, 40));
        launchButton.setBackground(BUTTON_COLOR);
        launchButton.setForeground(Color.WHITE);
        launchButton.setFocusPainted(false);
        launchButton.setBorderPainted(false);
        launchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        launchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                launchButton.setBackground(BUTTON_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                launchButton.setBackground(BUTTON_COLOR);
            }
        });

        launchButton.addActionListener(e -> onLaunch());

        panel.add(launchButton);

        return panel;
    }

    private void onLaunch() {
        List<String> args = buildCliArguments();

        if (launchCallback != null) {
            launchCallback.onLaunch(args);
        }

        dispose();
    }

    public List<String> buildCliArguments() {
        List<String> args = new ArrayList<>();

        if (rsdumpCheckbox.isSelected() && !rsdumpField.getText().trim().isEmpty()) {
            args.add("--rsdump");
            args.add(rsdumpField.getText().trim());
        }

        if (noPluginsCheckbox.isSelected()) {
            args.add("-noPlugins");
        }

        if (minCheckbox.isSelected()) {
            args.add("-min");
        }

        if (noMusicCheckbox.isSelected()) {
            args.add("-noMusic");
        }

        if (incognitoCheckbox.isSelected()) {
            args.add("-incognito");
        }

        if (proxyCheckbox.isSelected() && !proxyField.getText().trim().isEmpty()) {
            args.add("--proxy");
            args.add(proxyField.getText().trim());
        }

        if (!loginField.getText().trim().isEmpty()) {
            String loginType = (String) loginTypeCombo.getSelectedItem();
            if ("Legacy".equals(loginType)) {
                args.add("--legacyLogin");
            } else if ("Jagex".equals(loginType)) {
                args.add("--jagexLogin");
            }
            args.add(loginField.getText().trim());
        }

        return args;
    }

    public void setLaunchCallback(LaunchCallback callback) {
        this.launchCallback = callback;
    }

    @FunctionalInterface
    public interface LaunchCallback {
        void onLaunch(List<String> args);
    }

    interface SimpleDocumentListener extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }
}
