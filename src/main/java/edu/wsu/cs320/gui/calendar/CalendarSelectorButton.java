package edu.wsu.cs320.gui.calendar;

import com.google.api.services.calendar.model.CalendarListEntry;

import javax.swing.JRadioButton;

public class CalendarSelectorButton extends JRadioButton {
    public CalendarListEntry calendar;

    public CalendarSelectorButton(CalendarListEntry cal) {
        this.calendar = cal;
        this.setText(cal.getSummary());
        this.setToolTipText(cal.getDescription());
    }
}
