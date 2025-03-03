package edu.wsu.cs320.RP;


import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;

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
                try(Activity activity = new Activity()){
                    RP = activity;
                    core.activityManager().updateActivity(RP);
                }

//              DO NOT TOUCH THIS LOOP (it will break things)
                while(true) {
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

