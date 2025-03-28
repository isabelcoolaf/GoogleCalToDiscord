package edu.wsu.cs320.RP;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import de.jcm.discordgamesdk.user.DiscordUser;
import edu.wsu.cs320.config.ConfigValues;
import edu.wsu.cs320.googleapi.CalendarPollingService;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import edu.wsu.cs320.GoogleCalToDiscord;

public class Presence {
    private String appID;
    private Activity RP;
    private Core updater;
    private Boolean update = true;
    private Event lastEvent;
    private long endTime;
    private CalendarPollingService poll;
    public Presence(String ID){
        appID = ID;
    }
    public Activity getActivityState(){
        return RP;
    }
    public void setActivityState(Activity state){
        RP = state;
        updater.activityManager().updateActivity(RP);
    }

    public void setPoll(){
        if (poll != null) poll.stop();
        GoogleCalendarServiceHandler handler = new GoogleCalendarServiceHandler(GoogleCalToDiscord.googleOAuthManager.getCredentials());
        CalendarPollingService pollingService = new CalendarPollingService(handler, GoogleCalToDiscord.config.get(ConfigValues.GOOGLE_CALENDAR_ID));
        pollingService.start();
        poll = pollingService;
    }

    public void pauseEventUpdates(String type, String end){
        if (type.equals("date")){
            LocalDate date = LocalDate.parse(end);
            endTime = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        }
        if (type.equals("dateTime")){
            try {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(end);
                Instant instant = offsetDateTime.toInstant();
                endTime =  instant.toEpochMilli();

            } catch (Exception e) {
                System.err.println("Time Parse error: " + e.getMessage());
                return;
            }
        }
        if (endTime > Instant.now().toEpochMilli()) update = false;
        if (!update) System.out.println(" - Paused Status Updating - ");
    }

    private long checkTime(long end){
        if (!update && end != 0){
            update = end <= Instant.now().toEpochMilli();
            if (update){
                lastEvent = new Event();
                System.out.println(" - Resumed Status Updating - ");
                return 0;
            }
        }
        return end;
    }

    public void calendarEventUpdater(Event event){
        if (event != null) {
            try (Activity activity = new Activity()){
                activity.setType(ActivityType.WATCHING);
                activity.setDetails(event.getSummary());
                activity.setState(event.getDescription());
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                if (start != null && end != null){
                    activity.timestamps().setStart(Instant.ofEpochMilli(start.getValue()));
                    activity.timestamps().setEnd(Instant.ofEpochMilli(end.getValue()));
                }
                RP = activity;
                updater.activityManager().updateActivity(RP);
            }
        } else {
            try (Activity activity = new Activity()){
                activity.setType(ActivityType.WATCHING);
                activity.setDetails("No Current Event");
                activity.setState("Calendar empty at this timeslot");
                RP = activity;
                updater.activityManager().updateActivity(RP);
            }
        }
    }

    public void Activity() throws IOException{
        BigInteger ID = new BigInteger(appID);

        try(CreateParams params = new CreateParams()){
            params.setClientID(ID.longValue());
            params.setFlags(CreateParams.getDefaultFlags());

            try(Core core = new Core(params)){
                updater = core;
                setPoll();

                Event eventStart = poll.getCurrentEvent();
                calendarEventUpdater(eventStart);

                // TOUCH THIS LOOP (it will break things)
                lastEvent = eventStart;
                while(true) {
                    // check time to re-enable calendar updating
                    endTime = checkTime(endTime);
                    if (update){
                        // Does not update the activity unless the event has changed
                        Event event = poll.getCurrentEvent();
                        if (event != null && !event.equals(lastEvent)){
                            lastEvent = event;
                            calendarEventUpdater(event);
                        } else if (event == null && lastEvent != null) {
                            lastEvent = null;
                            calendarEventUpdater(null);
                        }
                    }
                    core.runCallbacks();

                    // Handle reminders
                    List<Event> reminders = poll.getCurrentReminders();
                    for (Event upcomingEvent : reminders) {
                        String message = String.format(":bell: **Reminder:** %s :bell:\nYour event is starting <t:%d:R>\n[Link to event](<%s>)",
                                upcomingEvent.getSummary(),
                                upcomingEvent.getStart().getDateTime().getValue()/1000,
                                upcomingEvent.getHtmlLink());
                        GoogleCalToDiscord.discordInterface.sendMessageToUser(getCurrentUser().getUserId(), message);
                    }

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

    public DiscordUser getCurrentUser() {
        return this.updater.userManager().getCurrentUser();
    }

}

