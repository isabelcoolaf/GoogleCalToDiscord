package edu.wsu.cs320.gui.calendar;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.LinkedList;

/**
 * White-box branch coverage of feedCalendarList.
 * <pre>
 *     public void feedCalendarList(CalendarList calList) {
 *         entryPanel.removeAll();
 *         for (CalendarListEntry entry : calList.getItems()) addButton(entry);
 *         entryButtonGroup.clearSelection();
 *     }
 * </pre>
 */
public class CalendarSelectorTest {

    JFrame window;
    CalendarSelector selector;

    @BeforeEach
    public void setup() {
        selector = new CalendarSelector();
        window = new JFrame();
        window.setContentPane(selector.getGuiPanel());
        window.setVisible(true);
    }

    @AfterEach
    public void cleanup() {
        window.dispose();
    }


    private void feedNoButtons() {
        CalendarList list = new CalendarList();
        list.setItems(new LinkedList<>());
        selector.feedCalendarList(list);
    }

    private void feedOneButton() {
        CalendarList list = new CalendarList();
        LinkedList<CalendarListEntry> entries = new LinkedList<>();
        entries.add(new CalendarListEntry().setSummary("Some button").setDescription("Some description"));
        list.setItems(entries);
        selector.feedCalendarList(list);
    }

    // White-Box condition where an empty CalendarList is used
    @Test
    public void testNoButtons() {
        feedNoButtons();
        assertNoButtons();
    }

    // Tests condition for when a one-entry CalendarList is used
    @Test
    public void testOneButton() {
        feedOneButton();
        assertOneButton();
    }

    // Tests condition for when a multiple-entry CalendarList is used
    @Test
    public void testMultipleButtons() {
        LinkedList<CalendarListEntry> entries = feedMultipleButtons();
        assertMultipleButtons(entries.size());
    }

    @NotNull
    private LinkedList<CalendarListEntry> feedMultipleButtons() {
        CalendarList list = new CalendarList();
        LinkedList<CalendarListEntry> entries = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            entries.add(new CalendarListEntry().setSummary("Some button").setDescription("Some description"));
        }
        list.setItems(entries);
        selector.feedCalendarList(list);
        return entries;
    }

    // Tests condition for when a multiple-list entry is replaced by a no-list entry and vice versa
    @Test
    public void overrideNoneMultiple() {
        feedMultipleButtons();
        // Multiple -> None
        feedNoButtons();
        assertNoButtons();
        // None -> Multiple
        LinkedList<CalendarListEntry> entries = feedMultipleButtons();
        assertMultipleButtons(entries.size());
    }

    // Tests condition for when a no-list entry is replaced by a one-list entry and vice versa
    @Test
    public void overrideNoneOne() {
        feedOneButton();
        // one button -> no button
        feedNoButtons();
        assertNoButtons();
        // no button -> one button
        feedOneButton();
        assertNoButtons();
    }

    // Tests condition for when a multiple-list entry is replaces by a one-list entry and vice versa
    @Test
    public void overrideOneMultiple() {
        feedMultipleButtons();
        // Multiple -> one
        feedOneButton();
        assertOneButton();
        // One -> Multiple
        LinkedList<CalendarListEntry> entries = feedMultipleButtons();
        assertMultipleButtons(entries.size());
    }

    private void assertNoButtons() {
        if (!selector.buttons.isEmpty()) {
            throw new AssertionError("Selector should have had 0 buttons, but instead had " + selector.buttons.size() + ".");
        }
    }

    private void assertOneButton() {
        if (selector.buttons.size() != 1) {
            throw new AssertionError("Selector should have had 1 button, but instead had " + selector.buttons.size() + ".");
        }
    }

    private void assertMultipleButtons(int length) {
        if (selector.buttons.size() != length) {
            throw new AssertionError("Selector should have created " + length + " buttons, but instead had " + selector.buttons.size() + " buttons.");
        }
    }

}
