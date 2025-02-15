package edu.wsu.cs320.gui.control;


import javax.swing.*;

import edu.wsu.cs320.gui.GoogleAuthWindow.GoogleAuthWindow;

public class GuiController {

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

    private void closeGUI() {
        window.setVisible(false);
        if (guiPanel != null) window.remove(guiPanel);
    }

    public void destroy(){
        window.dispose();
    }


}
