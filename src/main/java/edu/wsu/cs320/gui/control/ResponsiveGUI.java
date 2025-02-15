package edu.wsu.cs320.gui.control;

import javax.swing.*;

public interface ResponsiveGUI {

    JPanel getGuiPanel();

    GuiResponse getResponse();

    void onWindowClose();


}
