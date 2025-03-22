package edu.wsu.cs320.commands;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
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
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SlashCommandInteractions extends ListenerAdapter {
    private final Presence richPresence;
    private String curCalendar;
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
    public void setCurrentCalendar(String googleCal){
        curCalendar = googleCal;
    }
    public void setPage(int number) {pageNumber = number;}
    private int getPage() {return pageNumber;}

    private List<CalendarListEntry> getCalList(GoogleCalendarServiceHandler handler){
        try {
            return handler.getCalendarList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getCalendarNames(List<CalendarListEntry> calendarList){
        return calendarList.stream()
        .map(CalendarListEntry::getSummary)
        .collect(Collectors.toList());
    }

    public StringSelectMenu getCalendarMenu(List<String> calendarNames){
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
            case "event_info":
                ConfigManager config = new ConfigManager(ConfigValues.CONFIG_FILENAME);
                curCalendar = config.get(ConfigValues.GOOGLE_CALENDAR_ID);
                if (calHandler == null){
                    event.reply("Google Calendar not authenticated! Please sign in first.").setEphemeral(true).queue();
                } else if (curCalendar == null) {
                    event.reply("No calendar selected! Please select a calendar first.").setEphemeral(true).queue();
                } else {
                    List<Event> events;
                    try {
                        events = calHandler.getUpcomingEvents(curCalendar);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (!events.isEmpty()){
                        event.reply("**EVENT INFO: **\n"+ events.get(0).toString() + "\n" + events.get(0).getEnd().toString()).setEphemeral(true).queue();
                    } else {
                        event.reply("No upcoming events.").setEphemeral(true).queue();
                    }
                }
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

            case "show_next_event":
                ConfigManager config3 = new ConfigManager(ConfigValues.CONFIG_FILENAME);
                curCalendar = config3.get(ConfigValues.GOOGLE_CALENDAR_ID);
                if (calHandler == null){
                    event.reply("Google Calendar not authenticated! Please sign in first.").setEphemeral(true).queue();
                } else if (curCalendar == null) {
                    event.reply("No calendar selected! Please select a calendar first.").setEphemeral(true).queue();
                } else {
                    List<Event> events;
                    try {
                        events = calHandler.getUpcomingEvents(curCalendar);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (!events.isEmpty()){
                        event.reply("Next event: **["+ events.get(0).getSummary() +"](" +events.get(0).getHtmlLink()+ ")**").setEphemeral(true).queue();
                    } else {
                        event.reply("No upcoming events.").setEphemeral(true).queue();
                    }
                }
                break;
            case "next_event":
                ConfigManager config2 = new ConfigManager(ConfigValues.CONFIG_FILENAME);
                curCalendar = config2.get(ConfigValues.GOOGLE_CALENDAR_ID);
                if (calHandler == null){
                    event.reply("Google Calendar not authenticated! Please sign in first.").setEphemeral(true).queue();
                } else if (curCalendar == null) {
                    event.reply("No calendar selected! Please select a calendar first.").setEphemeral(true).queue();
                } else {
                    List<Event> events;
                    try {
                        events = calHandler.getUpcomingEvents(curCalendar);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (!events.isEmpty() && events.size() > eventCount){
                        eventCount++;
                        event.reply("Event changed to: **"+ events.get(eventCount - 1).getSummary() + "**").setEphemeral(true).queue();

                        // Format Datetime to string ( it seems counterintuitive but is needed
                        String time = events.get(eventCount - 1).getEnd().toString();
                        time = time.substring(1, time.length() - 1);
                        String[] dateTime = time.split("\"");

                        String format = dateTime[1];
                        String endTime = dateTime[3];
                        richPresence.pauseEventUpdates(format, endTime);

                        Instant nowUtc = Instant.now();

                        // Change the start time to now
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
                        String formattedNow = formatter.format(nowUtc);
                        DateTime dateTimeEvent = new DateTime(formattedNow);
                        EventDateTime eventDateTime = new EventDateTime().setDateTime(dateTimeEvent);

                        Event eventF = events.get(eventCount - 1);
                        eventF.setStart(eventDateTime);
                        richPresence.calendarEventUpdater(eventF);
                    } else {
                        event.reply("No more upcoming events.").setEphemeral(true).queue();
                    }
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
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
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
                ConfigManager config = new ConfigManager(ConfigValues.CONFIG_FILENAME);
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
        OptionData PresenceType = new OptionData(OptionType.STRING, "presence_type", "Select the presence type", true);
        String[] choices = {"Playing", "Watching", "Listening", "Competing"};
        for (String choice : choices) {
            PresenceType.addChoice(choice, choice);
        }


        String[] commandList = {"event_info","presence_type", "show_next_event","next_event", "select_calendar"};
        String[] commandDescriptions = {
                "Debugging command",
                "Changes Presence Type",
                "Shows next calendar event",
                "Immediately displays the next calendar event",
                "Select a calendar to display"
        };
        OptionData[] options = {null, PresenceType, null, null , null};
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
