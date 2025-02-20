package edu.wsu.cs320.commands;

import com.google.api.services.calendar.model.Event;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import edu.wsu.cs320.RP.Presence;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlashCommandInteractions extends ListenerAdapter {
    private final Presence richPresence;
    private com.google.api.services.calendar.model.Calendar curCalendar;
    private GoogleCalendarServiceHandler calHandler;

    // Presence required so that commands can alter the data of the activity
    public SlashCommandInteractions(Presence RP) {
        richPresence = RP;
    }
    public void setGoogleCalendarHandler(GoogleCalendarServiceHandler handler){
        calHandler = handler;
    }
    public void setCurrentCalendar(com.google.api.services.calendar.model.Calendar googleCal){
        curCalendar = googleCal;
    }
    public com.google.api.services.calendar.model.Calendar getCurCalendar() { return curCalendar; }
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        System.out.println("Command used \"" + event.getName() + "\"");
        switch (event.getName()) {
            case "command_example":
                OptionMapping options = event.getOption("insert_option_name");
                String response = options.getAsString();

                event.reply("reply to command with option: " + response).setEphemeral(true).queue();
                break;
            case "presence_type":
                OptionMapping presenceOptions = event.getOption("presence_type");
                String presenceResponse = presenceOptions.getAsString();

                Activity activity = richPresence.getActivityState();

                Map<String, ActivityType> activityTypes = new HashMap<>();
                activityTypes.put("Playing", ActivityType.PLAYING);
                activityTypes.put("Watching", ActivityType.WATCHING);
                activityTypes.put("Listening", ActivityType.LISTENING);
                activityTypes.put("Streaming", ActivityType.STREAMING);
                activityTypes.put("Competing", ActivityType.COMPETING);

                ActivityType type = activityTypes.get(presenceResponse);
                activity.setType(type);

                richPresence.setActivityState(activity);

                event.reply("Changed presence type to: " + presenceResponse).setEphemeral(true).queue();
                break;

            case "show_next_event":
                if (calHandler == null){
                    event.reply("Google Calendar not authenticated! Please sign in first.").setEphemeral(true).queue();
                } else if (curCalendar == null) {
                    event.reply("No calendar selected! Please select a calendar first.").setEphemeral(true).queue();
                } else {
                    List<Event> events;
                    try {
                        events = calHandler.getUpcomingEvents(curCalendar.getId());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    event.reply("Next event: "+ events.get(0).toString()).setEphemeral(true).queue();
                }
        }
    }


    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        List<CommandData> commands = new ArrayList<>();

        // names must be lowercase
        // command names must match command functionality options
        OptionData option_example = new OptionData(OptionType.STRING, "insert_option_name", "insert_description", true);

        OptionData PresenceType = new OptionData(OptionType.STRING, "presence_type", "Select the presence type", true);
        String[] choices = {"Playing", "Watching", "Listening", "Streaming", "Competing"};
        for (String choice : choices) {
            PresenceType.addChoice(choice, choice);
        }


        String[] commandList = {"command_example", "presence_type", "show_next_event"};
        String[] commandDescriptions = {"command_example", "Changes Presence Type", "Shows next calendar event"};
        OptionData[] options = {option_example, PresenceType, null};
        for (int i = 0; i < commandList.length; i++) {
            if (options[i] != null){
                commands.add(Commands.slash(commandList[i], commandDescriptions[i])
                        .setContexts(InteractionContextType.ALL)
                        .setIntegrationTypes(IntegrationType.USER_INSTALL)
                        .addOptions(options[i])
                );
            } else {
                commands.add(Commands.slash(commandList[i], commandDescriptions[i])
                        .setContexts(InteractionContextType.ALL)
                        .setIntegrationTypes(IntegrationType.USER_INSTALL));
            }
        }


        event.getJDA().updateCommands().addCommands(commands).queue();

    }
}