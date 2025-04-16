package edu.wsu.cs320.commands;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import edu.wsu.cs320.GoogleCalToDiscord;
import edu.wsu.cs320.RP.DiscordInterface;
import edu.wsu.cs320.RP.DiscordRichPresence;
import edu.wsu.cs320.config.ConfigManager;
import edu.wsu.cs320.config.ConfigValues;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;


import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandList {
    private int pageNumber;
    private final GoogleCalendarServiceHandler calHandler;
    private final static List<CommandListener> listeners = new ArrayList<>();

    public CommandList(GoogleCalendarServiceHandler handler){
        calHandler = handler;
    }

    public interface CommandListener{
        void selectedCalendarUpdate(String calName);
    }

    /**
     * Changes the type of the rich presence status the user gets shown. (mostly useless)
     */
    public void presenceTypeCommand(DiscordRichPresence discordRichPresence, SlashCommandInteractionEvent event){
        OptionMapping presenceOptions = event.getOption("presence_type");
        String presenceResponse = presenceOptions.getAsString();

        Activity activity = discordRichPresence.getDiscordActivityState();

        Map<String, ActivityType> activityTypes = new HashMap<>();
        activityTypes.put("Playing", ActivityType.PLAYING);
        activityTypes.put("Watching", ActivityType.WATCHING);
        activityTypes.put("Listening", ActivityType.LISTENING);
        activityTypes.put("Competing", ActivityType.COMPETING);

        ActivityType type = activityTypes.get(presenceResponse);
        System.out.println(type);
        activity.setType(type);

        discordRichPresence.setDiscordActivityState(activity);

        event.reply("Changed presence type to: " + presenceResponse).setEphemeral(true).queue();
    }

    /**
     * Shows the next event on the google calendar to the user
     */
    public void nextEventCommand( SlashCommandInteractionEvent event){
        ConfigManager config3 = new ConfigManager(ConfigValues.CONFIG_FILENAME);
        String curCalendar = config3.get(ConfigValues.GOOGLE_CALENDAR_ID);
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
                event.reply("Next event: "
                        + "**["+ events.get(0).getSummary() +"](" +events.get(0).getHtmlLink()+ ")**"
                ).setEphemeral(true).queue();
            } else {
                event.reply("No upcoming events.").setEphemeral(true).queue();
            }
        }
    }

    /**
     * Starts the next event on the selected google calendar.
     * The event start time is set to 'now' and the original end time is the same.
     * Only works to show the very next event on the calendar as the rich presence status
     */
    public void startNextEventCommand(DiscordRichPresence discordRichPresence, SlashCommandInteractionEvent event){
        ConfigManager config2 = new ConfigManager(ConfigValues.CONFIG_FILENAME);
        String curCalendar = config2.get(ConfigValues.GOOGLE_CALENDAR_ID);
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

            if (events.isEmpty()){
                event.reply("No upcoming events.").setEphemeral(true).queue();
                return;
            }

            event.reply("Event changed to: **"+ events.get(0).getSummary() + "**").setEphemeral(true).queue();

            // Format Datetime to string ( it seems counterintuitive but is needed )
            String time = events.get(0).getEnd().toString();
            time = time.substring(1, time.length() - 1);
            String[] dateTime = time.split("\"");

            String format = dateTime[1];
            String endTime = dateTime[3];
            discordRichPresence.pauseEventUpdatesFromCalendar(format, endTime);

            Instant nowUtc = Instant.now();

            // Change the start time to now
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            String formattedNow = formatter.format(nowUtc);
            DateTime dateTimeEvent = new DateTime(formattedNow);
            EventDateTime eventDateTime = new EventDateTime().setDateTime(dateTimeEvent);

            Event eventF = events.get(0);
            eventF.setStart(eventDateTime);
            discordRichPresence.updateActivityWithCalendarEvent(eventF);

        }
    }

    /**
     * Gives the user a calendar selection menu and replies with what calendar the user has selected.
     */
    public void selectCalendarCommand( SlashCommandInteractionEvent event){
        if (calHandler == null) {
            event.reply("Google Calendar not authenticated! Please sign in first.").setEphemeral(true).queue();
        } else {
            List<String> calendarNames = getCalendarNames(getCalList(calHandler));
            event.reply("Please select a calendar.").addActionRow(getCalendarMenu(calendarNames)).setEphemeral(true).queue();
        }
    }

    /**
     * Gives the user a calendar selection menu and replies with what calendar the user has selected.
     */
    public void sleepCommand(DiscordRichPresence discordRichPresence, SlashCommandInteractionEvent event){
        OptionMapping sleepDays = event.getOption("days");
        long response = sleepDays.getAsLong();

        if (response < 0){
            event.reply("**"+response+"** is invalid. Days must be a positive number").setEphemeral(true).queue();
            return;
        }

        LocalDate sleepDate = LocalDate.now().plusDays(response);
        discordRichPresence.pauseEventUpdatesFromCalendar("date", sleepDate.toString());
        discordRichPresence.updateActivityWithCalendarEvent(null);

        event.reply("Now sleeping for **" + response + "** days.").setEphemeral(true).queue();
    }

    /**
     * Resets all changes made to the rich presence status made by other commands.
     */
    public void resetCommand(DiscordRichPresence discordRichPresence, DiscordInterface discordInterface, SlashCommandInteractionEvent event){
        event.reply("Reset calendar settings").setEphemeral(true).queue();
        discordInterface.killBot();
        discordRichPresence.stopDiscordActivity();
        discordInterface.run();
    }

    /**
     * Attempts to change images if possible given by the user as keys.
     */
    public void setImageCommand(DiscordRichPresence discordRichPresence, SlashCommandInteractionEvent event){
        OptionMapping largeImageOpt = event.getOption("large-image");
        OptionMapping smallImageOpt = event.getOption("small-image");
        if (smallImageOpt == null && largeImageOpt == null){
            event.reply("Please provide an image URL.").setEphemeral(true).queue();
            return;
        }
        String imageLarge;
        String imageSmall;
        if (largeImageOpt != null){imageLarge = largeImageOpt.getAsString();}
        else {imageLarge = null;}
        if (smallImageOpt != null){imageSmall = smallImageOpt.getAsString();}
        else {imageSmall = null;}

        discordRichPresence.updateActivityWithImages(imageLarge, imageSmall);
        event.reply("Attempted to change images.\n-# *If images are unchanged, make sure your images are square and at least 512x512*").setEphemeral(true).queue();
    }

    /**
     * Calendar selection handler for selecting a calendar.
     */
    public void calendarPickerStringSelection(DiscordRichPresence discordRichPresence, StringSelectInteractionEvent event){
        if (event.getComponentId().equals("choose-calendar")) {
            String selection = event.getValues().get(0);
            if (selection.equals("Next Page")){
                this.pageNumber += 1;
                List<String> calendarNames = getCalendarNames(getCalList(calHandler));
                event.editMessage("Showing **" + selection + "**. Please select a calendar")
                        .setActionRow(getCalendarMenu(calendarNames)).queue();

            } else if (selection.equals("Previous Page")){
                this.pageNumber -= 1;
                List<String> calendarNames = getCalendarNames(getCalList(calHandler));
                event.editMessage("Showing **" + selection + "**. Please select a calendar")
                        .setActionRow(getCalendarMenu(calendarNames)).queue();
            } else {
                List<CalendarListEntry> calList = getCalList(calHandler);
                String calID = null;

                for (CalendarListEntry entry : calList) {
                    if (entry.getSummary() != null && entry.getSummary().equals(selection)) {
                        calID = entry.getId();
                        break;
                    }
                }

                try {
                    GoogleCalToDiscord.config.put(ConfigValues.GOOGLE_CALENDAR_ID, calID);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                discordRichPresence.setGoogleCalendar();
                notifyCalChange(selection);

                event.editMessage("**" + selection + "** is your selected calendar.").queue();
                event.editSelectMenu(null).queue();

            }
        }
    }

    // Helper methods for commands

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

        List<String> calendarListTemp = calendarNamesList.get(this.pageNumber);

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

    public void addCommandListener(CommandListener listener){
        listeners.add(listener);
    }

    private void notifyCalChange(String calName){
        for (CommandListener listener : listeners){
            listener.selectedCalendarUpdate(calName);
        }
    }

}


