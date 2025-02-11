package edu.wsu.cs320.gui.control;

import javax.swing.*;
import java.net.URI;
import java.util.Calendar;

public interface ResponsiveGUI {

    JFrame guiFrame = null;

    default URI getURI() {
        return null;
    }

    default String[] getAuthData() {
        return null;
    }

    default Calendar getSelectedCalendar() {
        return null;
    }
}
