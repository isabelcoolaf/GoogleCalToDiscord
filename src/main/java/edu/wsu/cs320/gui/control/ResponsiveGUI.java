package edu.wsu.cs320.gui.control;

import javax.swing.*;

/**
 * Implementations of this class:<br>
 * - Should be some sort of GUI contained within a single JPanel.<br>
 * - Allow communication of user input via creating GuiResponse instances.
 *
 * @see GuiController
 * @see GuiResponse
 */
public interface ResponsiveGUI {
    /**
     * Returns the JPanel containing the GUI.
     */
    JPanel getGuiPanel();

    /**
     * Await a response from the GUI.
     *
     * @see GuiResponse
     */
    GuiResponse getResponse();

    /**
     * Called by a GuiController whenever the user attempts to close the window.
     *
     * @see GuiController
     */
    void onWindowClose();
}
