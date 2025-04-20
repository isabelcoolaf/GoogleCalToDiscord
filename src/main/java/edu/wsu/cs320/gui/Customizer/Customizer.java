package edu.wsu.cs320.gui.Customizer;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.jcm.discordgamesdk.activity.Activity;
import edu.wsu.cs320.RP.DiscordInterface;
import edu.wsu.cs320.gui.control.GuiResponse;
import edu.wsu.cs320.gui.control.ResponsiveGUI;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Customizer implements ResponsiveGUI<Customizer.CustomizerCode> {
    private JPanel mainPanel;
    private JPanel previewPanel;
    private JPanel buttonPanel;
    private JButton backButton;
    private JButton changeImageButton;
    private JPanel imagePanel;
    private JLabel usingLabel;
    private JLabel calendarName;
    private JPanel progressPanel;
    private JProgressBar progressBar;
    private JLabel calendarDescription;
    private JLabel progressLeft;
    private JLabel progressRight;
    private JPanel mainTextPanel;
    private JTextField urlInputField;
    private CompletableFuture<GuiResponse<CustomizerCode>> pendingResponse;
    private final DiscordInterface discord;
    private UpdateThread updateThread;

    private class UpdateThread extends Thread {
        private boolean shouldBeRunning = true;
        private final Object monitor = new Object();

        UpdateThread() {
            setName("Customizer update thread");
        }


        @Override
        public void run() {
            Activity activity;
            System.out.println("Customizer update thread started");
            while (shouldBeRunning) {
                try {
                    synchronized (monitor) {
                        monitor.wait(100); // Update every .1 seconds
                    }
                } catch (IllegalMonitorStateException e) {
                    shouldBeRunning = false;
                } catch (InterruptedException e) {
                    shouldBeRunning = false;
                    continue;
                }
                if (hasInvalidState()) continue;
                activity = discord.getRichPresence().getDiscordActivityState();
                Instant timeStart = null;
                Instant timeEnd = null;
                try {
                    timeStart = activity.timestamps().getStart();
                    timeEnd = activity.timestamps().getEnd();
                } catch (NullPointerException e) {
                    // Current activity does not have a time
                }
                mainPanel.invalidate();
                if (timeStart != null && timeEnd != null) {
                    Instant timeCurrent = Instant.now();
                    long timeElapsed = timeCurrent.getEpochSecond() - timeStart.getEpochSecond();
                    long timeTotal = timeEnd.getEpochSecond() - timeStart.getEpochSecond();
                    float progressRatio = (float) (timeElapsed / timeTotal);
                    int progress = (int) progressRatio * 1000;
                    String startString = String.format("%02d:%02d:%02d", timeElapsed / 3600, (timeElapsed / 60) % 60, timeElapsed % 60);
                    String endString = String.format("%02d:%02d:%02d", timeTotal / 3600, (timeTotal / 60) % 60, timeTotal % 60);
                    progressLeft.setText(startString);
                    progressRight.setText(endString);
                    progressBar.setValue(progress);
                    progressPanel.setVisible(true);
                } else {
                    progressPanel.setVisible(false);
                }
                calendarName.setText(activity.getDetails());
                calendarDescription.setText(activity.getState());
                mainPanel.validate();
                mainPanel.repaint();
            }
            System.out.println("Customizer update thread stopped");
        }

        private boolean hasInvalidState() {
            return !mainPanel.isVisible() || discord == null;
        }
    }

    public enum CustomizerCode {
        BACK, CHANGE_IMAGE
    }

    public Customizer(DiscordInterface discordInterface) {
        $$$setupUI$$$();
        discord = discordInterface;
        backButton.addActionListener(e -> {
            if (pendingResponse != null)
                pendingResponse.complete(new GuiResponse<>(GuiResponse.ResponseCode.OK, CustomizerCode.BACK));
        });

        changeImageButton.addActionListener(event -> {
            if (urlInputField.getText().length() > 256) {
                JOptionPane.showMessageDialog(mainPanel, "Request failed.\nDiscord disallows URLS over 256 characters.", "Link error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                new URL(urlInputField.getText());
            } catch (MalformedURLException e) {
                JOptionPane.showMessageDialog(mainPanel, "Request failed.\nInvalid link provided.", "", JOptionPane.ERROR_MESSAGE);
                return;
            }
            pendingResponse.complete(new GuiResponse<>(GuiResponse.ResponseCode.OK, CustomizerCode.CHANGE_IMAGE));
        });
    }

    public void updateImage(URL url) {

        BufferedImage image;
        try {
            image = ImageIO.read(url);
        } catch (IOException e) {
            return;
        }
        imagePanel.removeAll();
        Image scaled = image.getScaledInstance(60, 60, Image.SCALE_DEFAULT);
        ImageIcon icon = new ImageIcon(scaled);
        JLabel label = new JLabel(icon);
        imagePanel.setLayout(new GridLayout(0, 1));
        imagePanel.add(label);
        imagePanel.validate();
        imagePanel.repaint();
    }


    @Nullable
    public URL getImage() {
        try {
            return new URL(urlInputField.getText());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public void startUpdateThread() {
        updateThread = new UpdateThread();
        updateThread.start();
    }

    public void killUpdateThread() {
        if (updateThread == null) return;
        updateThread.interrupt();
    }

    public void stopUpdateThread() {
        if (updateThread == null) return;
        updateThread.shouldBeRunning = false;
    }

    @Override
    public JPanel getGuiPanel() {
        return mainPanel;
    }

    @Override
    public GuiResponse<CustomizerCode> getResponse() {
        pendingResponse = new CompletableFuture<>();
        GuiResponse<CustomizerCode> result = new GuiResponse<>(GuiResponse.ResponseCode.INCOMPLETE_DATA, null);
        try {
            result = pendingResponse.get();
        } catch (InterruptedException | ExecutionException e) {
            pendingResponse.complete(new GuiResponse<>(GuiResponse.ResponseCode.CANCELLED, null));
        }
        return result;
    }

    @Override
    public void onWindowClose() {
        if (pendingResponse == null) return;
        pendingResponse.complete(new GuiResponse<>(GuiResponse.ResponseCode.WINDOW_CLOSED, null));
        stopUpdateThread();
    }

//region autogenerated code

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, 0));
        mainPanel.setMinimumSize(new Dimension(340, 120));
        mainPanel.setPreferredSize(new Dimension(340, 120));
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(buttonPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        backButton = new JButton();
        backButton.setText("Back");
        buttonPanel.add(backButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        changeImageButton = new JButton();
        changeImageButton.setText("Change Image");
        buttonPanel.add(changeImageButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        urlInputField = new JTextField();
        urlInputField.setText("Enter a URL...");
        mainPanel.add(urlInputField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        previewPanel = new JPanel();
        previewPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 5, 0, 0), 3, 0));
        previewPanel.setBackground(new Color(-13026751));
        previewPanel.setForeground(new Color(-13026751));
        mainPanel.add(previewPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(340, 100), new Dimension(340, 100), new Dimension(340, 100), 0, false));
        imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        previewPanel.add(imagePanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, 60), new Dimension(60, 60), new Dimension(60, 60), 0, false));
        usingLabel = new JLabel();
        usingLabel.setBackground(new Color(-2104859));
        Font usingLabelFont = this.$$$getFont$$$(null, -1, -1, usingLabel.getFont());
        if (usingLabelFont != null) usingLabel.setFont(usingLabelFont);
        usingLabel.setForeground(new Color(-2565675));
        usingLabel.setText("Using GC2D");
        previewPanel.add(usingLabel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, 1, null, null, null, 0, false));
        mainTextPanel = new JPanel();
        mainTextPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTextPanel.setBackground(new Color(-13026751));
        mainTextPanel.setForeground(new Color(-13026751));
        previewPanel.add(mainTextPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, 1, 1, null, null, null, 0, false));
        calendarName = new JLabel();
        calendarName.setBackground(new Color(-13026751));
        calendarName.setForeground(new Color(-2039330));
        calendarName.setText("<Calendar Name>");
        mainTextPanel.add(calendarName, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        calendarDescription = new JLabel();
        calendarDescription.setBackground(new Color(-13026751));
        calendarDescription.setForeground(new Color(-988432));
        calendarDescription.setText("<Calendar Description>");
        mainTextPanel.add(calendarDescription, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressPanel = new JPanel();
        progressPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        progressPanel.setBackground(new Color(-13026751));
        progressPanel.setForeground(new Color(-13026751));
        mainTextPanel.add(progressPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        progressLeft = new JLabel();
        progressLeft.setBackground(new Color(-13026751));
        progressLeft.setForeground(new Color(-2039330));
        progressLeft.setText("00:00");
        progressPanel.add(progressLeft, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressRight = new JLabel();
        progressRight.setBackground(new Color(-13026751));
        progressRight.setForeground(new Color(-2039330));
        progressRight.setText("XX:XX");
        progressPanel.add(progressRight, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar = new JProgressBar();
        progressBar.setBackground(new Color(-13552840));
        progressBar.setMaximum(1000);
        progressPanel.add(progressBar, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /** @noinspection ALL */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    //endregion
}
