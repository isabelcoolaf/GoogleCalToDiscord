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

    // Takes a time in milliseconds and displays a time bar
    public void setTimeBar(Activity activity, long time){
        long startTime = Instant.now().toEpochMilli();
        long endTime = startTime + time;
        activity.timestamps().setStart(Instant.ofEpochSecond(startTime));
        activity.timestamps().setEnd(Instant.ofEpochSecond(endTime));
        updater.activityManager().updateActivity(RP);
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
                // DO NOT TOUCH THIS LOOP (it will break things)
                while(true) {
                    core.runCallbacks();
                    Event event = pollingService.getCurrentEvent();
                    if (event != null) {
                        try (Activity activity = new Activity()){
                            activity.setType(ActivityType.WATCHING);
                            activity.setDetails(event.getSummary());
                            activity.setState(event.getDescription());
                            activity.timestamps().setStart(Instant.ofEpochMilli(event.getStart().getDateTime().getValue()));
                            activity.timestamps().setEnd(Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()));
                            RP = activity;
                            core.activityManager().updateActivity(RP);
                        }
                    } else {
                        try (Activity activity = new Activity()){
                            activity.setType(ActivityType.WATCHING);
                            activity.setDetails("No Current Event");
                            activity.setState("Calendar empty at this timeslot");
                            RP = activity;
                            core.activityManager().updateActivity(RP);
                        }
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

}

