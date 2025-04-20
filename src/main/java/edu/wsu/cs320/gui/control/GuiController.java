package edu.wsu.cs320.gui.control;

import javax.swing.*;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import edu.wsu.cs320.RP.DiscordInterface;
import edu.wsu.cs320.gui.Customizer.Customizer;
import edu.wsu.cs320.gui.auth.AuthForm;
import edu.wsu.cs320.gui.calendar.CalendarSelector;

import java.awt.image.BufferedImage;

/**
 * Controls the creation, destruction, and display of ResponsiveGUIs used to gather input from the user.
 *
 * @see GuiResponse
 * @see ResponsiveGUI
 */
public class GuiController {

    private final JFrame window;
    private ResponsiveGUI gui;
    private JPanel guiPanel;
    private Customizer customizer; // Customizer is persistent to allow consecutive image swaps

    /** Create a new controller with its window hidden. */
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

    /**
     * Displays a GUI that collects authentication data.<br>
     * Closes after getting a response. Make sure to check the response code before using the data.
     *
     * @see GuiResponse
     * @see AuthForm
     */
    public GuiResponse<String[]> getAuthData() {
        window.setResizable(false);
        AuthForm auth = new AuthForm();
        openGUI(auth);
        GuiResponse<String[]> resp = new GuiResponse<>(GuiResponse.ResponseCode.WINDOW_CLOSED, null);
        while (gui != null) {
            resp = auth.getResponse();
            if (resp.status != GuiResponse.ResponseCode.INCOMPLETE_DATA) break;
            JOptionPane.showMessageDialog(window, "Please fill in all fields.", "Incomplete Input", JOptionPane.WARNING_MESSAGE);
        }
        closeGUI();
        return resp;
    }

    /**
     * Display a GUI that allows the user to select a calendar from the given list.<br>
     * Closes after getting a response. Make sure to check the response code before using the data.
     *
     * @param cals The list of calendars to display to the user
     */
    public GuiResponse<CalendarListEntry> getCalendarFromList(CalendarList cals) {
        CalendarSelector selector = new CalendarSelector();
        openGUI(selector);
        selector.feedCalendarList(cals);
        GuiResponse<CalendarListEntry> resp = selector.getResponse();
        closeGUI();
        return resp;
    }


    /**
     * Gets a response from the customizer GUI, making a new one if necessary. <br>
     * NOTE: The customizer will stay open until it's told to go back to the calendar selector.<br>
     * Opening a new GUI before getting data response code <code,white-space:"nowrap">Customizer.CustomizerCode.BACK</code,white-space:"nowrap">
     * will likely cause unwanted side effects.
     *
     * @return The response received from the customizer GUI.
     */
    public GuiResponse<Customizer.CustomizerCode> accessCustomizer(DiscordInterface discordInterface) {
        // If customizer not already open, open it
        if (customizer == null) {
            customizer = new Customizer(window, discordInterface);
            customizer.startUpdateThread();
            openGUI(customizer, 350, 170);
            window.setResizable(false);
        }
        GuiResponse<Customizer.CustomizerCode> resp = customizer.getResponse();
        if (resp.data == Customizer.CustomizerCode.BACK) {
            closeGUI();
            customizer = null;
        }
        return resp;
    }

    public BufferedImage getCustomizerImage() {
        if (customizer == null) return null;
        return customizer.getImage();
    }


    private void openGUI(ResponsiveGUI newGui) {
        gui = newGui;
        guiPanel = gui.getGuiPanel();
        window.setContentPane(guiPanel);
        window.setSize(300, 200);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private void openGUI(ResponsiveGUI newGui, int width, int height) {
        gui = newGui;
        guiPanel = gui.getGuiPanel();
        window.setContentPane(guiPanel);
        window.setSize(width, height);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private void closeGUI() {
        window.setVisible(false);
        gui.onWindowClose();
        if (guiPanel != null) window.remove(guiPanel);
    }

    /** Destroy the window of the GuiController. The controller should not be used after this. */
    public void destroy() {
        window.dispose();
    }


}
