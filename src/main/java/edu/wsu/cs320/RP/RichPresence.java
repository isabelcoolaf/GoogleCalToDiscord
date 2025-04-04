package edu.wsu.cs320.RP;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import de.jcm.discordgamesdk.user.DiscordUser;
import edu.wsu.cs320.GoogleCalToDiscord;
import edu.wsu.cs320.config.ConfigValues;
import edu.wsu.cs320.googleapi.CalendarPollingService;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import org.jsoup.Jsoup;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class RichPresence {

    private Activity richPrsenceActivity;
    private Core updateHandler;
    private Boolean updateEventsFromCalendar = true;
    private Event lastEvent;
    private long eventEndTime;
    private boolean runDiscordActivity = true;
    private CalendarPollingService calendarEventPoll;

    public void startDiscordActivity(String applicationID){
        try(CreateParams params = new CreateParams()){
            params.setClientID(new BigInteger(applicationID).longValue());
            params.setFlags(CreateParams.getDefaultFlags());

            try(Core core = new Core(params)){
                updateHandler = core;
                setGoogleCalendar();

                Event eventStart = calendarEventPoll.getCurrentEvent();
                updateActivityWithCalendarEvent(eventStart);
                lastEvent = eventStart;

                // TOUCH THIS LOOP (it will break things)
                while(runDiscordActivity) {
                    calendarEventChangeCheck();

                    eventReminders();

                    core.runCallbacks();

                    try {
                        Thread.sleep(20);
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public void stopDiscordActivity(){
        runDiscordActivity = false;
        calendarEventPoll.stop();
    }
    public void updateActivityWithCalendarEvent(Event event){
        if (event != null) {
            try (Activity activity = new Activity()){
                activity.setType(ActivityType.WATCHING);
                activity.setDetails(event.getSummary());
                activity.setState(Jsoup.parse(event.getDescription()).text());
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                if (start != null && end != null){
                    activity.timestamps().setStart(Instant.ofEpochMilli(start.getValue()));
                    activity.timestamps().setEnd(Instant.ofEpochMilli(end.getValue()));
                }
                updateActivity(activity);
            }
        } else {
            try (Activity activity = new Activity()){
                activity.setType(ActivityType.WATCHING);
                activity.setDetails("No Current Event");
                activity.setState("Calendar empty at this timeslot");
                updateActivity(activity);
            }
        }
    }
    public void setGoogleCalendar(){
        if (calendarEventPoll != null) calendarEventPoll.stop();
        GoogleCalendarServiceHandler handler = new GoogleCalendarServiceHandler(GoogleCalToDiscord.googleOAuthManager.getCredentials());
        CalendarPollingService pollingService = new CalendarPollingService(handler, GoogleCalToDiscord.config.get(ConfigValues.GOOGLE_CALENDAR_ID));
        pollingService.start();
        calendarEventPoll = pollingService;
    }
    
    public void pauseEventUpdatesFromCalendar(String type, String end){
        if (type.equals("date")){
            LocalDate date = LocalDate.parse(end);
            eventEndTime = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        }
        if (type.equals("dateTime")){
            try {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(end);
                Instant instant = offsetDateTime.toInstant();
                eventEndTime =  instant.toEpochMilli();

            } catch (Exception e) {
                System.err.println("Time Parse error: " + e.getMessage());
                return;
            }
        }
        if (eventEndTime > Instant.now().toEpochMilli()) updateEventsFromCalendar = false;
        if (!updateEventsFromCalendar) System.out.println(" - Paused Status Updating - ");
    }
    
    public Activity getDiscordActivityState(){
        return richPrsenceActivity;
    }
    
    public void setDiscordActivityState(Activity state){
        updateActivity(state);
    }
    // Handle google calendar event reminders
    private void eventReminders(){
        List<Event> reminders = calendarEventPoll.getCurrentReminders();
        for (Event upcomingEvent : reminders) {
            String message = String.format(":bell: **Reminder:** %s :bell:\nYour event is starting <t:%d:R>\n[Link to event](<%s>)",
                    upcomingEvent.getSummary(),
                    upcomingEvent.getStart().getDateTime().getValue()/1000,
                    upcomingEvent.getHtmlLink());
            GoogleCalToDiscord.discordInterface.sendMessageToUser(getCurrentDiscordUser().getUserId(), message);
        }
    }

    private DiscordUser getCurrentDiscordUser(){
        return this.updateHandler.userManager().getCurrentUser();
    }

    private void updateActivity(Activity activity){
        richPrsenceActivity = activity;
        updateHandler.activityManager().updateActivity(richPrsenceActivity);
    }
    
    private void calendarEventChangeCheck(){
        // check time to re-enable calendar updating
        eventEndTime = checkTime(eventEndTime);
        if (updateEventsFromCalendar){
            // Does not update the activity unless the event has changed
            Event event = calendarEventPoll.getCurrentEvent();
            if (event != null && !event.equals(lastEvent)){
                lastEvent = event;
                updateActivityWithCalendarEvent(event);
            } else if (event == null && lastEvent != null) {
                lastEvent = null;
                updateActivityWithCalendarEvent(null);
            }
        }
    }

    private long checkTime(long end){
        if (!updateEventsFromCalendar && end != 0){
            updateEventsFromCalendar = end <= Instant.now().toEpochMilli();
            if (updateEventsFromCalendar){
                lastEvent = new Event();
                System.out.println(" - Resumed Status Updating - ");
                return 0;
            }
        }
        return end;
    }

}
