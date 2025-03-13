package edu.wsu.cs320.gui.calendar;

import com.google.api.services.calendar.model.CalendarListEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Blackbox test for the instantiation of CalendarSelectorButtons.
 * One of the functional requirements of the project is:
 * <pre>When the user has authenticated with Google, a GUI opens that allows the user to select one calendar out of all calendars on their Google account.</pre>
 * This requirement is not fulfilled if the calendar selector's buttons are created incorrectly.
 * Therefore, we stress test the instantiation of the buttons.
 */
public class CalendarSelectorButtonTest {


    // Makes sure the button properly handles a null argument.
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

    // Makes sure the button actually stores the calendar it's instantiated with.
    @Test
    public void correctlyStoresCalendarInField() {
        CalendarListEntry entry = new CalendarListEntry();
        entry.setSummary("Test Summary");
        entry.setDescription("Test Description");
        CalendarSelectorButton button = new CalendarSelectorButton(entry);
        assertEquals(entry, button.calendar);
    }

    // Makes sure that the button's text and tooltip are properly extracted from a given entry's name (AKA summary).
    @Test
    public void correctlySetsButtonLabelAndDescription() {
        CalendarListEntry entry = new CalendarListEntry()//
                .setSummary("Test Summary")//
                .setDescription("Test Description");//
        CalendarSelectorButton button = new CalendarSelectorButton(entry);
        assertEquals(entry.getSummary(), button.getText());
        assertEquals(entry.getDescription(), button.getToolTipText());
    }
}
