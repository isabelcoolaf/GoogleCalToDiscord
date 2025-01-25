package edu.wsu.cs320.gui.calendar;

import javax.swing.*;

import com.google.api.services.calendar.model.Calendar;

public class CalendarSelector {
    private JLabel selectCalendarLabel;
    private JPanel mainPanel;
    private JPanel entryPanel;
    private CalendarSelectorEntry[] entries;
    private ButtonGroup entryButtonGroup = new ButtonGroup();

    void addEntry(Calendar calendar) {
        CalendarSelectorEntry newEntry = new CalendarSelectorEntry();
        newEntry.calendar = calendar;
        newEntry.setText(calendar.getSummary());
        newEntry.setToolTipText(calendar.getDescription());
        entryButtonGroup.add(newEntry);
    }
}
