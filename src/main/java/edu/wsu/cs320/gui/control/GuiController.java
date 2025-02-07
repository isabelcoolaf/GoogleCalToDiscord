package edu.wsu.cs320.gui.control;

import javax.swing.*;

public class GuiController {

    public enum StateEnum {
        AUTH,
        SELECT,
        CUSTOMIZE
    }

    private StateEnum state;
    private boolean guiSpawned = false;
    private JFrame currentGUI = null;
    // TODO: Make currentGUI its own class for JPanel management

    public GuiController() {
        currentGUI = new JFrame();
        currentGUI.setVisible(false);
    }


    public void setState(StateEnum newState) {
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

    public void spawnGUI() {
        //TODO: method should spawn GUI based on state
        currentGUI.setVisible(true);
        guiSpawned = true;
    }

    public void closeGUI() {
        //TODO: method should destroy its given JPanel, assuming it has one
        currentGUI.setVisible(false);
        guiSpawned = false;
    }

    public void freezeInput() {
        //TODO: Lock user out of meaningful interaction with the GUI
    }


}
