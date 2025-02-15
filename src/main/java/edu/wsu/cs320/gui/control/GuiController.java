package edu.wsu.cs320.gui.control;


import javax.swing.*;

import com.google.api.services.calendar.model.Calendar;

import java.net.URI;

public class GuiController {

    public enum StateEnum {
        AUTH,
        SELECT,
        CUSTOMIZE
    }

    private StateEnum state;
    private boolean guiSpawned = false;
    private JFrame window = null;
    private JPanel currentGUI;
    // TODO: Make currentGUI its own class for JPanel management

    public GuiController() {
        window = new JFrame();
        window.setVisible(false);
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
    public void spawnGUI() {
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

    public void closeGUI() {
        //TODO: method should destroy its given JPanel, assuming it has one
        if (!guiSpawned) return;
        window.setVisible(false);
        window.remove(currentGUI);
        guiSpawned = false;
    }

    public void freezeInput() {
        //TODO: Lock user out of meaningful interaction with the GUI
    }


}
