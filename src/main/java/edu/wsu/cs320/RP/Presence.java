package edu.wsu.cs320.RP;


import com.google.api.services.calendar.model.Event;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import edu.wsu.cs320.config.ConfigValues;
import edu.wsu.cs320.googleapi.CalendarPollingService;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;

import edu.wsu.cs320.GoogleCalToDiscord;

public class Presence {
    private String appID;
    private Activity RP;
    private Core updater;
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

    private void calendarEventUpdater(Event event){
        if (event != null) {
            try (Activity activity = new Activity()){
                activity.setType(ActivityType.WATCHING);
                activity.setDetails(event.getSummary());
                activity.setState(event.getDescription());
                activity.timestamps().setStart(Instant.ofEpochMilli(event.getStart().getDateTime().getValue()));
                activity.timestamps().setEnd(Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()));
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

        // application ID
        BigInteger ID = new BigInteger(appID);

        try(CreateParams params = new CreateParams()){
            params.setClientID(ID.longValue());
            params.setFlags(CreateParams.getDefaultFlags());

            try(Core core = new Core(params)){
                updater = core;
                GoogleCalendarServiceHandler handler = new GoogleCalendarServiceHandler(GoogleCalToDiscord.googleOAuthManager.getCredentials());
                CalendarPollingService pollingService = new CalendarPollingService(handler, GoogleCalToDiscord.config.get(ConfigValues.GOOGLE_CALENDAR_ID));
                pollingService.start();

                Event eventStart = pollingService.getCurrentEvent();
                calendarEventUpdater(eventStart);

                // DO NOT TOUCH THIS LOOP (it will break things)
                Event lastEvent = eventStart;
                while(true) {
                    // Does not update the activity unless the event has changed
                    Event event = pollingService.getCurrentEvent();
                    if (event != null && !event.equals(lastEvent)){
                        lastEvent = event;
                        calendarEventUpdater(event);
                    } else if (event == null && lastEvent != null) {
                        lastEvent = null;
                        calendarEventUpdater(null);
                    }
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

}

