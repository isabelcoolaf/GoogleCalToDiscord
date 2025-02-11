package edu.wsu.cs320.gui.calendar;

import com.google.api.services.calendar.model.Calendar;

import javax.swing.JRadioButton;

public class CalendarSelectorButton extends JRadioButton {
    public Calendar calendar;

    public CalendarSelectorButton(Calendar cal) {
        this.calendar = cal;
        this.setText(cal.getSummary());
        this.setToolTipText(cal.getDescription());
    }
}
