package edu.wsu.cs320.gui.calendar;

import javax.swing.*;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import edu.wsu.cs320.gui.control.GuiResponse;
import edu.wsu.cs320.gui.control.ResponsiveGUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Allows the user to select a calendar from a given list.
 *
 * @see CalendarList
 * @see CalendarListEntry
 */
public class CalendarSelector implements ResponsiveGUI {
    private JLabel selectCalendarLabel;
    private JPanel mainPanel;
    private JButton confirmButton;
    private JPanel entryPanel;
    public LinkedList<CalendarSelectorButton> buttons;
    private final ButtonGroup entryButtonGroup = new ButtonGroup();
    private CompletableFuture<GuiResponse<CalendarListEntry>> pendingResponse;

    public CalendarSelector() {
        confirmButton.addActionListener(event -> completeResponse());
        buttons = new LinkedList<>();
    }

    private void addButton(CalendarListEntry cal) {
        CalendarSelectorButton newEntry = new CalendarSelectorButton(cal);
        buttons.add(newEntry);
        entryButtonGroup.add(newEntry);
    }

    /**
     * Fill the window with CalendarSelectorButtons for each entry in the CalendarList.
     *
     * @param calList The list of calendars to display.
     * @see CalendarSelectorButton
     */
    public void feedCalendarList(CalendarList calList) {
        entryPanel.removeAll();
        buttons.clear();
        for (CalendarListEntry entry : calList.getItems()) addButton(entry);
        entryButtonGroup.clearSelection();
    }

    private void completeResponse() {
        if (pendingResponse == null) return; // No response to complete
        CalendarSelectorButton selected = (CalendarSelectorButton) entryButtonGroup.getSelection();
        if (selected == null) return; // Can't complete response if nothing selected
        pendingResponse.complete(new GuiResponse<CalendarListEntry>(GuiResponse.ResponseCode.OK, selected.calendar));
    }


    @Override
    public JPanel getGuiPanel() {
        return mainPanel;
    }

    /**
     * Request a calendar be selected from the window, blocking the thread until a response is received.
     *
     * @return If the response was successful, returns a GuiResponse with code OK and a dataPacket field of the selected calendar.
     * Otherwise, returns a GuiResponse with a different code and a null dataPacket.
     */
    @Override
    public GuiResponse<CalendarListEntry> getResponse() {
        this.pendingResponse = new CompletableFuture<>();
        GuiResponse<CalendarListEntry> result;
        try {
            result = this.pendingResponse.get();
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            return new GuiResponse<CalendarListEntry>(GuiResponse.ResponseCode.CANCELLED, null);
        }
        return result;
    }

    @Override
    public void onWindowClose() {
        if (this.pendingResponse != null) {
            pendingResponse.complete(new GuiResponse<CalendarListEntry>(GuiResponse.ResponseCode.WINDOW_CLOSED, null));
        }
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        selectCalendarLabel = new JLabel();
        selectCalendarLabel.setText("Select a calendar:");
        mainPanel.add(selectCalendarLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        confirmButton = new JButton();
        confirmButton.setText("Confirm");
        mainPanel.add(confirmButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        entryPanel = new JPanel();
        entryPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPane1.setViewportView(entryPanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
