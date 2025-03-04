package edu.wsu.cs320.commands;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import edu.wsu.cs320.RP.Presence;
import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.config.ConfigValues;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SlashCommandInteractions extends ListenerAdapter {
    private final Presence richPresence;
    private Calendar curCalendar;
    private GoogleCalendarServiceHandler calHandler;
    private static int eventCount;
    private int pageNumber;

    // Presence required so that commands can alter the data of the activity
    public SlashCommandInteractions(Presence RP) {
        richPresence = RP;
    }
    public void setGoogleCalendarHandler(GoogleCalendarServiceHandler handler){
        calHandler = handler;
    }
    public void setCurrentCalendar(Calendar googleCal){
        curCalendar = googleCal;
    }
    private void setPage(int number) {pageNumber = number;}
    private int getPage() {return pageNumber;}

    private List<CalendarListEntry> getCalList(GoogleCalendarServiceHandler handler){
        try {
            return handler.getCalendarList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getCalendarNames(List<CalendarListEntry> calendarList){
                        return calendarList.stream()
                        .map(CalendarListEntry::getSummary)
                        .collect(Collectors.toList());
    }

    private StringSelectMenu getCalendarMenu(List<String> calendarNames){
        List<List<String>> calendarNamesList = new ArrayList<>();

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("choose-calendar");

        if (calendarNames.size() > 25){
            int listLength = calendarNames.size();

            for (int i = 0; i < listLength; i += 23) {
                int endIndex = Math.min(i + 23, listLength);
                List<String> chunk = new ArrayList<>();
                chunk.addAll(0,calendarNames.subList(i, endIndex));
                if (i > 0){
                    chunk.add(0, "Previous Page");
                }
                if (i + 23 < listLength){
                    chunk.add(chunk.size(), "Next Page");
                }
                calendarNamesList.add(chunk);
            }
        } else {
            calendarNamesList.add(calendarNames);
        }

        List<String> calendarListTemp = calendarNamesList.get(getPage());

        // This will break if someone names their calendar "Next/Previous Page"
        for (String name : calendarListTemp) {
            if (name.equals("Next Page")) {
                menuBuilder.addOptions(SelectOption.of(name, name)
                        .withEmoji(Emoji.fromUnicode("⏩")));
            } else if (name.equals("Previous Page")) {
                menuBuilder.addOptions(SelectOption.of(name, name)
                        .withEmoji(Emoji.fromUnicode("⏪")));
            } else {
                menuBuilder.addOptions(SelectOption.of(name, name));
            }

        }

        return menuBuilder.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        System.out.println("Command used \"" + event.getName() + "\"");
        switch (event.getName()) {
            case "command_example":
                OptionMapping options = event.getOption("insert_option_name");
                String response = options.getAsString();

                Activity activity2 = richPresence.getActivityState();
                richPresence.setTimeBar(activity2, Long.parseLong(response));

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
                activityTypes.put("Competing", ActivityType.COMPETING);

                ActivityType type = activityTypes.get(presenceResponse);
                System.out.println(type);
                activity.setType(type);

                richPresence.setActivityState(activity);

                event.reply("Changed presence type to: " + presenceResponse).setEphemeral(true).queue();
                break;

//                Fix these repeating events *Later
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
                break;
            case "next_event":
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
                    eventCount++;
                    event.reply("Event changed to: "+ events.get(eventCount).toString()).setEphemeral(true).queue();
                    Activity activityState = richPresence.getActivityState();
                    activityState.setDetails(events.get(0).toString());
                    richPresence.setActivityState(activityState);
                }
                break;
            case "select_calendar":
                if (calHandler == null) {
                    event.reply("Google Calendar not authenticated! Please sign in first.").setEphemeral(true).queue();
                } else {
                    List<String> calendarNames = getCalendarNames(getCalList(calHandler));
                    event.reply("Please select a calendar.").addActionRow(getCalendarMenu(calendarNames)).setEphemeral(true).queue();
                }
                break;
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("choose-calendar")) {
            String selection = event.getValues().get(0);
            if (selection.equals("Next Page")){
                setPage(getPage() + 1);
                List<String> calendarNames = getCalendarNames(getCalList(calHandler));
                event.editMessage("Showing **" + selection + "**. Please select a calendar")
                        .setActionRow(getCalendarMenu(calendarNames)).queue();

            } else if (selection.equals("Previous Page")){
                setPage(getPage() - 1);
                List<String> calendarNames = getCalendarNames(getCalList(calHandler));
                event.editMessage("Showing **" + selection + "**. Please select a calendar")
                        .setActionRow(getCalendarMenu(calendarNames)).queue();
            } else {
                ConfigManager config = new ConfigManager();
                List<CalendarListEntry> calList = getCalList(calHandler);
                String calID = null;

                for (CalendarListEntry entry : calList) {
                    if (entry.getSummary() != null && entry.getSummary().equals(selection)) {
                        calID = entry.getId();
                        break;
                    }
                }

                try {
                    config.put(ConfigValues.GOOGLE_CALENDAR_ID, calID);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                event.editMessage("**" + selection + "** is your selected calendar.").queue();
                event.editSelectMenu(null).queue();

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
        String[] choices = {"Playing", "Watching", "Listening", "Competing"};
        for (String choice : choices) {
            PresenceType.addChoice(choice, choice);
        }


        String[] commandList = {"command_example", "presence_type", "show_next_event", "select_calendar"};
        String[] commandDescriptions = {"command_example", "Changes Presence Type", "Shows next calendar event", "Select a calendar to display"};
        OptionData[] options = {option_example, PresenceType, null, null};
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
