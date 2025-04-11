package edu.wsu.cs320.gui.calendar;

import com.google.api.services.calendar.model.CalendarListEntry;

import javax.swing.JRadioButton;

/**
 * A JRadioButton with an extra field for storing a CalendarListEntry.
 *
 * @see CalendarSelector
 */
public class CalendarSelectorButton extends JRadioButton {
    /** The calendar this button represents. */
    public CalendarListEntry calendar;

    /** Create a new button with calendar information filled into the text and tooltip. */
    public CalendarSelectorButton(CalendarListEntry cal) {
        this.calendar = cal;
        this.setText(cal.getSummary());
        this.setToolTipText(cal.getDescription());
    }

    @Override
    public String toString() {
        return "CalendarSelectorButton (Calendar: " + calendar.getId() + ")";
    }
}
