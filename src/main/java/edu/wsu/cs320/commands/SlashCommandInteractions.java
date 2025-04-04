package edu.wsu.cs320.commands;

import edu.wsu.cs320.RP.DiscordInterface;
import edu.wsu.cs320.RP.RichPresence;
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
    private final RichPresence richPresence;
    private final DiscordInterface discordInterface;
    private GoogleCalendarServiceHandler calHandler;

    // Presence required so that commands can alter the data of the activity
    public SlashCommandInteractions(RichPresence RP, DiscordInterface discInterface) {
        richPresence = RP;
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
                cmdList.presenceTypeCommand(richPresence, event);
                break;
            case "next-event":
                cmdList.nextEventCommand(event);
                break;
            case "start-next-event":
                cmdList.startNextEventCommand(richPresence, event);
                break;
            case "select-calendar":
                cmdList.selectCalendarCommand(event);
                break;
            case "sleep":
                cmdList.sleepCommand(richPresence, event);
                break;
            case "reset":
                cmdList.resetCommand(richPresence, discordInterface, event);
                break;
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        CommandList cmdList = new CommandList(calHandler);
        cmdList.calendarPickerStringSelection(richPresence, event);
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


        String[] commandList = {"event-info", "presence-type", "next-event", "start-next-event", "select-calendar", "sleep", "reset"};
        String[] commandDescriptions = {
                "Debugging command",
                "Changes Presence Type",
                "Shows next calendar event",
                "Immediately displays the next calendar event",
                "Select a calendar to display",
                "Stops updating events for selected amount of time",
                "Resets calendar status settings"
        };
        OptionData[] options = {null, PresenceType, null, null, null, sleepTime, null};
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
