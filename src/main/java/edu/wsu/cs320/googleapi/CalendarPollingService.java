package edu.wsu.cs320.googleapi;

import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CalendarPollingService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> taskObj;
    private final GoogleCalendarServiceHandler handler;
    private String calendarID;

    private Event currentEvent;
    private final List<Event> currentReminders = new ArrayList<>();

    // Makes live calendar data easily accessible. Once start() is called, you can continually call getCurrentEvent() and getCurrentReminders()
    // to receive each from live data without blocking. Refreshes every minute at xx:00.
    // DOES NOT SUPPORT "primary" CALENDAR ID KEYWORD. Pass in an actual calendar ID.
    public CalendarPollingService(GoogleCalendarServiceHandler handler, String calendarID) {
        this.handler = handler;
        this.calendarID = calendarID;
    }

    public boolean start() {
        if (taskObj != null && !taskObj.isCancelled()) {
            return false;
        }
        task();
        taskObj = scheduler.scheduleAtFixedRate(this::loop, 0, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    public void stop() {
        if (taskObj != null) {
            taskObj.cancel(true);
        }
    }

    private void loop() {
        // Only run the loop on each minute barrier
        if (LocalTime.now().getSecond() == 0) {
            task();
        }
    }

    private void task() {
        System.out.println("Running background polling task...");
        long currentEpochMs = Instant.now().truncatedTo(ChronoUnit.MINUTES).toEpochMilli();

        List<Event> upcomingEvents;
        List<CalendarListEntry> calendarList;
        try {
            upcomingEvents = handler.getUpcomingEvents(calendarID);
            calendarList = handler.getCalendarList();
        } catch (IOException e) {
            System.out.println("Error in background polling task: ");
            e.printStackTrace();
            return;
        }
        CalendarListEntry calendar = null;
        for (CalendarListEntry entry : calendarList) {
            if (entry.getId().equals(calendarID)) {
                calendar = entry;
                break;
            }
        }
        if (calendar == null) {
            System.out.println("Error in background polling task: Calendar is null. Deleted?");
            return;
        }

        boolean setCurrent = false;
        for (Event event : upcomingEvents) {
            // Ignore all-day events
            if (event.getStart().getDateTime() == null) {
                continue;
            }
            long eventStartTimeEpochMs = event.getStart().getDateTime().getValue();
            if (!setCurrent) {
                // Did this event start before right now or right now?
                if (eventStartTimeEpochMs <= currentEpochMs) {
                    currentEvent = event;
                    setCurrent = true;
                }
            }

            List<EventReminder> reminders = new ArrayList<>();
            if (event.getReminders().getOverrides() != null) {
                reminders = event.getReminders().getOverrides();
            }
            // Use the default reminder setting for the calendar?
            if (event.getReminders().getUseDefault()) {
                reminders = calendar.getDefaultReminders();
            }

            for (EventReminder reminder : reminders) {
                // Are we at a reminder time?
                if ((eventStartTimeEpochMs - (reminder.getMinutes()*60*1000)) == currentEpochMs) {
                    currentReminders.add(event);
                    break;
                }
            }
        }
        if (!setCurrent) {
            currentEvent = null;
        }
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public List<Event> getCurrentReminders() {
        List<Event> reminders = new ArrayList<>(currentReminders);
        currentReminders.clear();
        return reminders;
    }

    public void setCalendarID(String newCalendarID) {
        this.calendarID = newCalendarID;
    }
}
