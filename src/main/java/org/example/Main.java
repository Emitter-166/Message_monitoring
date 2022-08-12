package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.example.Leaderboard.inputListeners.counter;
import org.example.Leaderboard.inputListeners.setup;
import org.example.Leaderboard.operations.resetting;

import javax.security.auth.login.LoginException;

public class Main {
    public static JDA jda;
    public static void main(String[] args) throws LoginException, InterruptedException {
        Database.connect();
        Thread reset_checker = new Thread(new resetting());
        reset_checker.start();

        jda = JDABuilder.createLight(tokens.token)
                .addEventListeners(new counter())
                .addEventListeners(new setup())
                .build().awaitReady();




        String serverId = tokens.server_id;
        jda.getGuildById(serverId).upsertCommand("help", "commands for message monitoring").queue();

        jda.getGuildById(serverId).upsertCommand("leaderboard", "see leaderboard for that channel")
                .addOption(OptionType.CHANNEL, "channel", "channel you want to see leaderboard for", true, false).queue();

        jda.getGuildById(serverId).upsertCommand("messages", "see how many messages you/someone have on that channel")
                .addOption(OptionType.CHANNEL, "channel", "channel you want to see messages for", true, false)
                .addOption(OptionType.USER, "user", "optional", false).queue();

        jda.getGuildById(serverId).upsertCommand("summary-channel", "channel to send summary")
                .addOption(OptionType.CHANNEL, "channel", "summary channel", false, false).queue();

        jda.getGuildById(serverId).upsertCommand("role-to-mention", "role to mention with summary")
                .addOption(OptionType.ROLE, "role", "role", true).queue();

        jda.getGuildById(serverId).upsertCommand("mainchat", "mainchat, more on /help")
                .addOption(OptionType.CHANNEL, "channel", "mainchat", false, false).queue();

        jda.getGuildById(serverId).upsertCommand("reset", "reset leaderboard for that chat")
                .addOption(OptionType.CHANNEL, "channel", "channel to reset leaderboard. if not entered everything will be reset", false, false).queue();

        jda.getGuildById(serverId).upsertCommand("reset-every-day", "set if the bot should reset leaderboard everyday")
                 .addOption(OptionType.BOOLEAN, "reset", "true/false", true).queue();

        jda.getGuildById(serverId).upsertCommand("reset-on", "set when to reset automatically and send a summary")
                .addOption(OptionType.STRING, "reset", "24 hour format, first 2 digits(hours), est time", true).queue();

        jda.getGuildById(serverId).upsertCommand("role-to-give", "which role to give to the top chatter")
                .addOption(OptionType.ROLE, "role-to-add", "role to add to the top chatter of the day", true).queue();
    }
}