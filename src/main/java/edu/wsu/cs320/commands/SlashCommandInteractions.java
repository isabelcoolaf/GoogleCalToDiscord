package edu.wsu.cs320.commands;

import edu.wsu.cs320.RP.DiscordInterface;
import edu.wsu.cs320.RP.DiscordRichPresence;
import edu.wsu.cs320.googleapi.GoogleCalendarServiceHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class SlashCommandInteractions extends ListenerAdapter {
    private final DiscordRichPresence discordRichPresence;
    private final DiscordInterface discordInterface;
    private GoogleCalendarServiceHandler calHandler;

    // Presence required so that commands can alter the data of the activity
    public SlashCommandInteractions(DiscordRichPresence RP, DiscordInterface discInterface) {
        discordRichPresence = RP;
        discordInterface = discInterface;
    }
    public void setGoogleCalendarHandler(GoogleCalendarServiceHandler handler){
        calHandler = handler;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        CommandList cmdList = new CommandList(calHandler);
        System.out.println("Command used \"" + event.getName() + "\"");
        switch (event.getName()) {
            case "event-info":
               cmdList.eventInfoCommand(event);
                break;
            case "presence-type":
                cmdList.presenceTypeCommand(discordRichPresence, event);
                break;
            case "next-event":
                cmdList.nextEventCommand(event);
                break;
            case "start-next-event":
                cmdList.startNextEventCommand(discordRichPresence, event);
                break;
            case "select-calendar":
                cmdList.selectCalendarCommand(event);
                break;
            case "sleep":
                cmdList.sleepCommand(discordRichPresence, event);
                break;
            case "reset":
                cmdList.resetCommand(discordRichPresence, discordInterface, event);
                break;
            case"select-images":
                cmdList.setImageCommand(discordRichPresence, event);
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        CommandList cmdList = new CommandList(calHandler);
        cmdList.calendarPickerStringSelection(discordRichPresence, event);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        List<CommandData> commands = new ArrayList<>();

        // names must be lowercase
        // command names must match command functionality options
        OptionData PresenceType = new OptionData(OptionType.STRING, "presence-type", "Select the presence type", true);
        String[] choices = {"Playing", "Watching", "Listening", "Competing"};
        for (String choice : choices) {
            PresenceType.addChoice(choice, choice);
        }

        OptionData sleepTime = new OptionData(OptionType.INTEGER, "days", "Choose number of days to sleep", true);

        OptionData largeImageSelect = new OptionData(OptionType.STRING, "large-image", "Large Image Key", false);
        OptionData smallImageSelect = new OptionData(OptionType.STRING, "small-image", "Large Image Key", false);


        String[] commandList = {"event-info", "presence-type", "next-event", "start-next-event", "select-calendar", "sleep", "reset", "select-images"};
        String[] commandDescriptions = {
                "Debugging command",
                "Changes Presence Type",
                "Shows next calendar event",
                "Immediately displays the next calendar event",
                "Select a calendar to display",
                "Stops updating events for selected amount of time",
                "Resets calendar status settings",
                "Select images to display on your profile presence"
        };
        OptionData[] options = {null, PresenceType, null, null, null, sleepTime, null, largeImageSelect, smallImageSelect};
        for (int i = 0; i < commandList.length - 1; i++) {
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

        // image select *requires* two options
        commands.add(Commands.slash(commandList[commandList.length - 1], commandDescriptions[commandList.length - 1])
                        .setContexts(InteractionContextType.ALL)
                        .setIntegrationTypes(IntegrationType.USER_INSTALL)
                        .addOptions(options[commandList.length - 1])
                        .addOptions(options[commandList.length]));


        event.getJDA().updateCommands().addCommands(commands).queue();

    }
}
