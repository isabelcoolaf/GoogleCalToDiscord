package edu.wsu.cs320.gui.control;

import javax.swing.*;

import com.google.api.services.calendar.model.CalendarList;
import edu.wsu.cs320.gui.GoogleAuthWindow.GoogleAuthWindow;
import edu.wsu.cs320.gui.calendar.CalendarSelector;

public class GuiController {

    private JFrame window;
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
                    "Please fill in all fields.",
                    "Incomplete Input", JOptionPane.WARNING_MESSAGE);
        }
        closeGUI();
        return resp;
    }

    public GuiResponse<String> getCalendarFromList(CalendarList cals) {
        CalendarSelector selector = new CalendarSelector();
        selector.feedCalendarList(cals);
        GuiResponse<String> resp = selector.getResponse();
        closeGUI();
        return resp;
    }


    private void openGUI(ResponsiveGUI newGui) {
        gui = newGui;
        guiPanel = gui.getGuiPanel();
        window.setContentPane(guiPanel);
        window.setSize(300, 200);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private void closeGUI() {
        window.setVisible(false);
        if (guiPanel != null) window.remove(guiPanel);
    }

    public void destroy() {
        window.dispose();
    }


}
