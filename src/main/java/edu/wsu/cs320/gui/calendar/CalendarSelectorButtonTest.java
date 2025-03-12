package edu.wsu.cs320.gui.calendar;

import com.google.api.services.calendar.model.CalendarListEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarSelectorButtonTest {

    // Blackbox. Makes sure buttons can't be created using null.
    @Test
    public void disallowsNullCalendars() {
        CalendarSelectorButton button;
        try {
            button = new CalendarSelectorButton(null);
        } catch (NullPointerException e) {
            return;
        }
        assertNotNull(button.calendar);
    }

    // Black-Box. Makes sure the button actually stores the calendar it's instantiated with.
    @Test
    public void properlyStoresCalendar() {
        CalendarListEntry entry = new CalendarListEntry();
        entry.setSummary("Test Summary");
        entry.setDescription("Test Description");
        CalendarSelectorButton button = new CalendarSelectorButton(entry);
        assertEquals(entry, button.calendar);
    }

    // Blackbox. Makes sure that the button's name is set by the CalendarListEntry's name (aka summary).
    @Test
    public void correctName() {
        CalendarListEntry entry = new CalendarListEntry();
        entry.setSummary("Test Summary");
        entry.setDescription("Test Description");
        CalendarSelectorButton button = new CalendarSelectorButton(entry);
        assertEquals(entry.getSummary(), button.getText());
    }


    // Blackbox. Makes sure that the button's tooltip is set by the CalendarListEntry's description.
    @Test
    public void correctDescription() {
        CalendarListEntry entry = new CalendarListEntry();
        entry.setSummary("Test Summary");
        entry.setDescription("Test Description");
        CalendarSelectorButton button = new CalendarSelectorButton(entry);
        assertEquals(entry.getDescription(), button.getToolTipText());
    }


}
