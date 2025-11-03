package com.tonic.launcher.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SplashScreen extends JWindow {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 350;
    private static final int PROGRESS_BAR_HEIGHT = 30;
    private static final int CORNER_RADIUS = 20;

    private static final Color BACKGROUND_COLOR = new Color(30, 35, 45);
    private static final Color PROGRESS_BG_COLOR = new Color(45, 52, 65);
    private static final Color PROGRESS_FILL_COLOR = new Color(70, 130, 200);
    private static final Color PROGRESS_BORDER_COLOR = new Color(90, 150, 220);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TITLE_COLOR = new Color(150, 200, 255);
    private static final Color ERROR_COLOR = new Color(220, 60, 60);

    private final SplashPanel splashPanel;
    private int stageProgress = 0;  // Progress of current stage (0-100)
    private int overallProgress = 0; // Overall progress (0-100)
    private String stageText = "Initializing...";
    private boolean isError = false;
    private final JButton closeButton;

    public SplashScreen() {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        splashPanel = new SplashPanel();
        splashPanel.setBounds(0, 0, WIDTH, HEIGHT);
        layeredPane.add(splashPanel, JLayeredPane.DEFAULT_LAYER);

        // Create close button (initially hidden)
        closeButton = createCloseButton();
        closeButton.setVisible(false);
        layeredPane.add(closeButton, JLayeredPane.PALETTE_LAYER);

        setContentPane(layeredPane);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        // Make window rounded (works on Java 11+)
        setShape(new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, CORNER_RADIUS, CORNER_RADIUS));
    }

    /**
     * Set the stage progress (0-100) for the current operation
     */
    public void setStageProgress(int progress, String text) {
        this.stageProgress = Math.max(0, Math.min(100, progress));
        this.stageText = text;
        SwingUtilities.invokeLater(splashPanel::repaint);
    }

    /**
     * Set the overall progress (0-100) for the entire launcher process
     */
    public void setOverallProgress(int progress) {
        this.overallProgress = Math.max(0, Math.min(100, progress));
        SwingUtilities.invokeLater(splashPanel::repaint);
    }

    /**
     * Set both stage and overall progress
     */
    public void setProgress(int stageProgress, int overallProgress, String text) {
        this.stageProgress = Math.max(0, Math.min(100, stageProgress));
        this.overallProgress = Math.max(0, Math.min(100, overallProgress));
        this.stageText = text;
        SwingUtilities.invokeLater(splashPanel::repaint);
    }

    // Deprecated - kept for backwards compatibility
    @Deprecated
    public void setProgressAndStatus(int progress, String text) {
        setStageProgress(progress, text);
    }

    public void setError(String message) {
        this.stageText = message;
        this.isError = true;
        SwingUtilities.invokeLater(() -> {
            closeButton.setVisible(true);
            closeButton.repaint();
            splashPanel.repaint();
            revalidate();
            repaint();
        });
    }

    public boolean isError() {
        return isError;
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
                g2d.setStroke(new BasicStroke(3));
                int padding = 12;
                g2d.drawLine(padding, padding, getWidth() - padding, getHeight() - padding);
                g2d.drawLine(getWidth() - padding, padding, padding, getHeight() - padding);
            }
        };

        button.setForeground(Color.WHITE);
        button.setBackground(ERROR_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBounds(WIDTH - 50, 10, 40, 40);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 80, 80));
                button.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ERROR_COLOR);
                button.repaint();
            }
        });

        button.addActionListener(e -> System.exit(0));

        return button;
    }

    private class SplashPanel extends JPanel {
        private BufferedImage splashIcon;

        public SplashPanel() {
            setBackground(BACKGROUND_COLOR);
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            loadImages();
        }

        private void loadImages() {
            try {
                splashIcon = ImageIO.read(getClass().getResourceAsStream("/com/tonic/launcher/icon_splash.png"));
            } catch (IOException e) {
                System.err.println("Failed to load splash icon: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Enable anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int centerX = getWidth() / 2;

            // Draw large splash icon in background with transparency
            if (splashIcon != null) {
                int bgIconSize = 300;
                int bgIconX = centerX - bgIconSize / 2;
                int bgIconY = (getHeight() / 2) - bgIconSize / 2;

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                g2d.drawImage(splashIcon, bgIconX, bgIconY, bgIconSize, bgIconSize, null);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            int y = 60;

            // Draw title "VitaLite"
            g2d.setColor(TITLE_COLOR);
            Font titleFont = new Font("Arial", Font.BOLD, 48);
            g2d.setFont(titleFont);
            FontMetrics titleMetrics = g2d.getFontMetrics(titleFont);
            String title = "VitaLite";
            int titleWidth = titleMetrics.stringWidth(title);
            g2d.drawString(title, centerX - titleWidth / 2, y);
            y += 45;

            // Draw version
            g2d.setColor(new Color(150, 160, 180));
            Font subtitleFont = new Font("Arial", Font.PLAIN, 14);
            g2d.setFont(subtitleFont);
            String subtitle = "Launcher v2.1";
            FontMetrics subtitleMetrics = g2d.getFontMetrics(subtitleFont);
            int subtitleWidth = subtitleMetrics.stringWidth(subtitle);
            g2d.drawString(subtitle, centerX - subtitleWidth / 2, y);
            y += 50;

            // === STAGE PROGRESS BAR (Top) ===
            int barWidth = 460;
            int barX = centerX - barWidth / 2;
            int barY = y;

            // Draw stage progress bar background
            g2d.setColor(PROGRESS_BG_COLOR);
            g2d.fillRoundRect(barX, barY, barWidth, PROGRESS_BAR_HEIGHT, 15, 15);

            // Draw stage progress bar border
            g2d.setColor(PROGRESS_BORDER_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(barX, barY, barWidth, PROGRESS_BAR_HEIGHT, 15, 15);

            // Draw stage progress fill
            if (stageProgress > 0) {
                int fillWidth = (int) ((barWidth - 4) * (stageProgress / 100.0));
                g2d.setColor(PROGRESS_FILL_COLOR);
                g2d.fillRoundRect(barX + 2, barY + 2, fillWidth, PROGRESS_BAR_HEIGHT - 4, 12, 12);

                // Add gradient effect
                GradientPaint gradient = new GradientPaint(
                        barX, barY, new Color(90, 150, 220, 100),
                        barX, barY + PROGRESS_BAR_HEIGHT, new Color(70, 130, 200, 0)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(barX + 2, barY + 2, fillWidth, PROGRESS_BAR_HEIGHT / 2, 12, 12);
            }

            // Draw stage text in center of progress bar
            Color statusColor = isError ? ERROR_COLOR : TEXT_COLOR;
            g2d.setColor(statusColor);
            Font statusFont = new Font("Arial", Font.BOLD, 11);
            g2d.setFont(statusFont);
            FontMetrics statusMetrics = g2d.getFontMetrics(statusFont);
            int textWidth = statusMetrics.stringWidth(stageText);
            int textX = centerX - textWidth / 2;
            int textY = barY + (PROGRESS_BAR_HEIGHT / 2) + (statusMetrics.getAscent() / 2) - 2;

            // Draw text shadow for better visibility
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(stageText, textX + 1, textY + 1);
            g2d.setColor(statusColor);
            g2d.drawString(stageText, textX, textY);

            y = barY + PROGRESS_BAR_HEIGHT + 30;

            // === OVERALL PROGRESS BAR (Bottom) ===
            int overallBarY = y;

            // Draw overall progress bar background
            g2d.setColor(PROGRESS_BG_COLOR);
            g2d.fillRoundRect(barX, overallBarY, barWidth, PROGRESS_BAR_HEIGHT, 15, 15);

            // Draw overall progress bar border
            g2d.setColor(PROGRESS_BORDER_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(barX, overallBarY, barWidth, PROGRESS_BAR_HEIGHT, 15, 15);

            // Draw overall progress fill
            if (overallProgress > 0) {
                int fillWidth = (int) ((barWidth - 4) * (overallProgress / 100.0));
                g2d.setColor(new Color(100, 180, 100)); // Different color for overall
                g2d.fillRoundRect(barX + 2, overallBarY + 2, fillWidth, PROGRESS_BAR_HEIGHT - 4, 12, 12);

                // Add gradient effect
                GradientPaint gradient = new GradientPaint(
                        barX, overallBarY, new Color(120, 200, 120, 100),
                        barX, overallBarY + PROGRESS_BAR_HEIGHT, new Color(100, 180, 100, 0)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(barX + 2, overallBarY + 2, fillWidth, PROGRESS_BAR_HEIGHT / 2, 12, 12);
            }

            // Draw "Overall Progress: X%" text
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(statusFont);
            String overallText = "Overall Progress: " + overallProgress + "%";
            int overallTextWidth = statusMetrics.stringWidth(overallText);
            int overallTextX = centerX - overallTextWidth / 2;
            int overallTextY = overallBarY + (PROGRESS_BAR_HEIGHT / 2) + (statusMetrics.getAscent() / 2) - 2;

            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(overallText, overallTextX + 1, overallTextY + 1);
            g2d.setColor(TEXT_COLOR);
            g2d.drawString(overallText, overallTextX, overallTextY);
        }
    }
}