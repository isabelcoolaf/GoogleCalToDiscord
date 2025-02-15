package edu.wsu.cs320.gui.control;


import javax.swing.*;

import edu.wsu.cs320.gui.GoogleAuthWindow.GoogleAuthWindow;

public class GuiController {

    public enum StateEnum {
        AUTH,
        SELECT,
        CUSTOMIZE
    }

    private StateEnum state;
    private boolean guiSpawned = false;
    private JFrame window = null;
    ResponsiveGUI gui;
    private JPanel guiPanel;
    // TODO: Make currentGUI its own class for JPanel management

    public GuiController() {
        window = new JFrame();
        window.setVisible(false);
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                gui.onWindowClose();
                closeGUI();
            }
        });
    }

    public GuiResponse<String[]> getAuthData() {
        GoogleAuthWindow auth = new GoogleAuthWindow();
        openGUI(auth);
        GuiResponse<String[]> resp = new GuiResponse<>(GuiResponse.ResponseCode.WINDOW_CLOSED, null);
        while (gui != null) {
            resp = auth.getResponse();
            if (resp.status != GuiResponse.ResponseCode.INCOMPLETE_DATA) break;
            JOptionPane.showMessageDialog(
                    window,
                    "Please provide a key and client ID.",
                    "Incomplete Input", JOptionPane.WARNING_MESSAGE);
        }
        closeGUI();
        return resp;
    }

    private void openGUI(ResponsiveGUI newGui) {
        gui = newGui;
        guiPanel = gui.getGuiPanel();
        window.setContentPane(guiPanel);
        window.setSize(300, 150);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }


    public void changeState(StateEnum newState) {
        if (guiSpawned) closeGUI();
        state = newState;
        if (guiSpawned) spawnGUI();
    }

    public StateEnum getState() {
        return state;
    }

    public boolean hasGUIOpen() {
        return guiSpawned;
    }

    /**
     * Spawn a new GUI based on the controller's state.
     * If a GUI is already spawned, it will replace it.
     */
    private void spawnGUI() {
        if (guiSpawned) {
            closeGUI();
        }
        switch (state) {
            case AUTH:
                ; // Open GoogleAuthWindow
            case SELECT:
                ; // Open CalendarSelector
            case CUSTOMIZE:
                ; // Open Customizer
        }
        window.setVisible(true);
        guiSpawned = true;
    }

    private void closeGUI() {
        window.setVisible(false);
        if (guiPanel != null) window.remove(guiPanel);
    }


}
