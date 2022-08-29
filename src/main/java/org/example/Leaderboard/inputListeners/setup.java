package org.example.Leaderboard.inputListeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.Database;
import org.example.Leaderboard.operations.clearLeaderboard;
import org.example.Leaderboard.operations.clearOneLeaderboard;
import org.example.Leaderboard.operations.sendLeaderboard;

import java.awt.*;

public class setup extends ListenerAdapter {
    Database database = new Database();
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e){
        Permission permission = Permission.MODERATE_MEMBERS;
        String name = e.getName();
        String serverId = e.getGuild().getId();
        switch (name){
            case "help":
                e.deferReply().queue();
                e.getHook().sendMessageEmbeds(new EmbedBuilder()
                                .setTitle("Commands for Message monitor")
                                .setColor(Color.WHITE)
                                .addField("Normal commands",
                                "`/leaderboard #channel` **See leaderboard for that channel** \n" +
                                        "`/messages #channel @user (optional)` **See how many messages you/someone have on that channel** \n",false)
                                .addField("Admin commands",
                                        "`/summary-channel #channel` **Set channel to send summary to at the end of each day on 12 am EST by default, you can change it by** `/reset-on` \n" +
                                                "`/role-to-mention @role` **Set which role to mention with summary** \n" +
                                                "`/mainchat #channel` **Set mainchat, mainchats result will be sent with summary** \n" +
                                                "`/reset-every-day true or false` **Set if the bot should reset leaderboard everyday** \n" +
                                                "`/reset #channel (optional)` **it will reset data for that specific channel or the whole database if no channel is provided** \n" +
                                                "`/reset-on` **set when to reset. this will take first two digits(hour) of 24 hour formatted est time**", false)
                        .build()).queue();
                break;

            case "summary-channel":
                e.deferReply().queue();
                if(!e.getMember().hasPermission(permission)){
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                                    .setTitle("You can't do that!!")
                                    .setDescription("**You must have** `" + permission.getName() + "` **permission to do that!**")
                            .build()).queue();
                    return;
                }

                String summary_channel;
                if(e.getOption("channel") != null){
                    summary_channel = e.getOption("channel").getAsMessageChannel().getId();
                }else{
                    summary_channel = e.getChannel().getId();
                }
                e.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Summary channel set!")
                        .setDescription("`summary channel set:` " + "<#" + summary_channel + ">")
                        .build()).queue();

                try {
                    database.set(serverId, "serverId", "actionChannel", summary_channel, false);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                break;

            case "role-to-mention":
                e.deferReply().queue();
                if(!e.getMember().hasPermission(permission)){
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("You can't do that!!")
                            .setDescription("**You must have** `" + permission.getName() + "` **permission to do that!**")
                            .build()).queue();
                    return;
                }
                String role_to_mention = e.getOption("role").getAsRole().getId();
                e.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Summary ping role set!")
                        .setDescription("`Summary ping role:` " + "<@&" + role_to_mention + ">" )
                        .build()).queue();
                try {
                    database.set(serverId, "serverId", "roleToMention", role_to_mention, false);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                break;

            case "mainchat":
                e.deferReply().queue();
                if(!e.getMember().hasPermission(permission)){
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("You can't do that!!")
                            .setDescription("**You must have** `" + permission.getName() + "` **permission to do that!**")
                            .build()).queue();
                    return;
                }


                String mainchat;
                if(e.getOption("channel") != null){
                    mainchat = e.getOption("channel").getAsMessageChannel().getId();
                }else{
                    mainchat = e.getChannel().getId();
                }
                e.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Main chat set!")
                        .setDescription(String.format("`Main chat:` <#%s>", mainchat))
                        .build()).queue();
                try {
                    database.set(serverId, "serverId", "mainChat", mainchat, false);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                break;

            case "reset-every-day":
                e.deferReply().queue();
                if(!e.getMember().hasPermission(permission)){
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("You can't do that!!")
                            .setDescription("**You must have** `" + permission.getName() + "` **permission to do that!**")
                            .build()).queue();
                    return;
                }
                boolean reset = e.getOption("reset").getAsBoolean();
                 e.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Reset everyday set!")
                        .setDescription("**reset everyday:** `" + reset + "`")
                        .build()).queue();
                try {
                    database.set(serverId, "serverId", "reset", reset, false);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                break;

            case "reset":
                e.deferReply().queue();
                if(!e.getMember().hasPermission(permission)){
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("You can't do that!!")
                            .setDescription("**You must have** `" + permission.getName() + "` **permission to do that!**")
                            .build()).queue();
                    return;
                }

                if(e.getOption("channel") == null){
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Resetting leaderboard..")
                            .build()).queue();
                    Thread thread = new Thread(new clearLeaderboard(serverId, database));
                    thread.start();
                }else{
                    String channelId = e.getOption("channel").getAsGuildChannel().getId();
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Resetting leaderboard for " +  e.getOption("channel").getAsGuildChannel().getName() )
                            .build()).queue();
                    Thread thread = new Thread(new clearOneLeaderboard(channelId, serverId, database));
                    thread.start();
                }
                break;

            case "leaderboard":
                e.deferReply().queue();
                String channelId = e.getOption("channel").getAsGuildChannel().getId();
                sendLeaderboard sendLeaderboard = new sendLeaderboard(channelId, serverId, database);
                Thread thread = new Thread(sendLeaderboard);
                thread.start();

                try {
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                                    .setTitle("Message Leaderboard")
                                    .setDescription(String.format("**Leaderboard for** <#%s> \n", channelId))
                                    .appendDescription(sendLeaderboard.getLeaderboard())
                            .build()).queue();

                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                break;

            case "messages":
                e.deferReply().queue();
                String message_channel = e.getOption("channel").getAsGuildChannel().getId();
                if(e.getOption("user") == null){
                    String userId = e.getMember().getId();
                    int messages;
                    try {
                        messages = (int) database.get(userId, "userId").get(message_channel);
                    } catch (Exception ex) {
                        messages = 0;
                    }
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                                    .setTitle("total messages")
                                    .setDescription(String.format("**You have a total of** `%s` **messages on** <#%s>", messages, message_channel))
                                    .build())
                            .queue();
                }else{
                    String userId = e.getOption("user").getAsUser().getId();
                    int messages = 0;
                    try {
                        messages = (int) database.get(userId, "userId").get(message_channel);
                    } catch (Exception exception){}
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                                    .setTitle("total messages")
                                    .setDescription(String.format("<@%s> **have a total of** `%s` **messages on** <#%s>", userId, messages, message_channel))
                                    .build())
                            .queue();
                }
                break;

            case "reset-on":
                e.deferReply().queue();
                if(!e.getMember().hasPermission(permission)){
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("You can't do that!!")
                            .setDescription("**You must have** `" + permission.getName() + "` **permission to do that!**")
                            .build()).queue();
                    return;
                }
                String time = e.getOption("reset").getAsString();
                if(time.length() != 2){
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Wrong usage!!")
                            .setDescription("**Must be 2 digits (hour), 24 hour formatted EST time.**")
                            .build()).queue();
                    break;
                }
                e.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Reset time set!")
                        .setDescription("**reset time:** `" + time + "` est")
                        .build()).queue();
                try {
                    database.set(serverId, "serverId", "reset-on", time, false);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case "role-to-give":
                e.deferReply().queue();
                if(!e.getMember().hasPermission(permission)){
                    e.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("You can't do that!!")
                            .setDescription("**You must have** `" + permission.getName() + "` **permission to do that!**")
                            .build()).queue();
                    return;
                }
                String role_to_add = e.getOption("role-to-add").getAsRole().getId();
                e.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("To add role set!")
                        .setDescription("`To add role:` " + "<@&" + role_to_add + ">" )
                        .build()).queue();
                try {
                    database.set(serverId, "serverId", "roleToAdd", role_to_add , false);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                break;
        }
    }
}
