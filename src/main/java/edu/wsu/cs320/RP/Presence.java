package edu.wsu.cs320.RP;


import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;

public class Presence {
    private Activity RP;
    private Core updater;

    public Activity getActivityState(){
        return RP;
    }
    public void setActivityState(Activity state){
        RP = state;
        updater.activityManager().updateActivity(RP);
    }

    public void Activity() throws IOException{

        // application ID
        BigInteger ID = new BigInteger("1329903401147826299");

        try(CreateParams params = new CreateParams()){
            params.setClientID(ID.longValue());
            params.setFlags(CreateParams.getDefaultFlags());

            try(Core core = new Core(params)){
                updater = core;
                try(Activity activity = new Activity()){
                    RP = activity;

                    activity.setType(ActivityType.WATCHING);
                    activity.setDetails("Title Text Here");
                    activity.setState("Description Here");

                    activity.timestamps().setStart(Instant.now());

                    activity.party().size().setMaxSize(1);
                    activity.party().size().setCurrentSize(1);

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

