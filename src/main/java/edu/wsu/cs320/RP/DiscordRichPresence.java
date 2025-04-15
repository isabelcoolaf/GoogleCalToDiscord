package edu.wsu.cs320.RP;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
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
import java.time.*;
import java.util.List;

public class DiscordRichPresence {

    private Activity richPrsenceActivity;
    private Core updateHandler;
    private Boolean updateEventsFromCalendar = true;
    private Event lastEvent;
    private long eventEndTime;
    private boolean runDiscordActivity = true;
    private CalendarPollingService calendarEventPoll;


    /**
     * Starts the rich presence activity that is displayed and updated in discord.
     * Runs the update loop to update the calendar from input or commands.
     */
    public void startDiscordActivity(String applicationID){
        CreateParams params = new CreateParams();
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
        } catch (RuntimeException err){
            System.err.println("Failed to connect to Discord servers: " + err.getMessage());
            throw err;
        }
    }

    /**
     * Stops the rich presence from displaying in discord and stops polls from calendar polling.
     */
    public void stopDiscordActivity(){
        runDiscordActivity = false;
        calendarEventPoll.stop();
    }

    /**
     * Updates the discord rich presence activity to display a given calendar event.
     */
    public void updateActivityWithCalendarEvent(Event event){
        if (event != null) {
            Activity activity = new Activity();
            activity.setType(ActivityType.WATCHING);
            activity.setDetails(event.getSummary());
            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();
            if (start != null && end != null){
                activity.timestamps().setStart(Instant.ofEpochMilli(start.getValue()));
                activity.timestamps().setEnd(Instant.ofEpochMilli(end.getValue()));
            }
            if (event.getDescription() != null)
                activity.setState(Jsoup.parse(event.getDescription()).text());
            updateActivity(activity);
        } else {
            Activity activity = new Activity();
            activity.setType(ActivityType.WATCHING);
            activity.setDetails("No Current Event");
            activity.setState("Calendar empty at this timeslot");
            updateActivity(activity);
        }
    }

    /**
     * Updates rich presence activity with images from asset library in developer portal given a set of image URLs.
     * *Due to API limitations, an error will not be thrown here even if there is no such image key to select
     */
    public void updateActivityWithImages(String largeImageURL, String smallImageURL){
        Activity state = getDiscordActivityState();
        if (largeImageURL != null) state.assets().setLargeImage(largeImageURL);
        if (smallImageURL != null) state.assets().setSmallImage(smallImageURL);
        setDiscordActivityState(state);
    }

    /**
     * Updates the calendar from the configuration settings for the rich presence status.
     */
    public void setGoogleCalendar(){
        if (calendarEventPoll == null) {
            GoogleCalendarServiceHandler handler = new GoogleCalendarServiceHandler(GoogleCalToDiscord.googleOAuthManager.getCredentials());
            calendarEventPoll = new CalendarPollingService(handler, GoogleCalToDiscord.config.get(ConfigValues.GOOGLE_CALENDAR_ID));
        }
        calendarEventPoll.setCalendarID(GoogleCalToDiscord.config.get(ConfigValues.GOOGLE_CALENDAR_ID));
    }

    /**
     * Pauses the rich presence status from updating.
     * - 'type' changes the format of provided end time as either an end date or an end dateTime
     * - 'end' is the specified end time for when to resume updating the rich presence status
     */
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

            } catch (DateTimeException err) {
                System.err.println("Time Parse error: " + err.getMessage());
                throw err;
            }
        }
        if (eventEndTime > Instant.now().toEpochMilli()) updateEventsFromCalendar = false;
        if (!updateEventsFromCalendar) System.out.println(" - Paused Status Updating - ");
    }

    /**
     * Handles event reminders for the user sending them a direct message for when there is an upcoming event with notifications on.
     */
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

    /**
     * Checks to see if there have been any changes to the event.
     * If there have been then the rich presence status is updated.
     */
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

    public Activity getDiscordActivityState(){
        return richPrsenceActivity;
    }

    public void setDiscordActivityState(Activity state){
        updateActivity(state);
    }

}
