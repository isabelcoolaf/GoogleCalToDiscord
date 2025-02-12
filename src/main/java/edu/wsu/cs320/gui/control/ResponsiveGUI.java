package edu.wsu.cs320.gui.control;

import javax.swing.*;
import java.net.URI;
import com.google.api.services.calendar.model.Calendar;

public interface ResponsiveGUI {

    JPanel guiGuiPanel();

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
