package org.example.Leaderboard.operations;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bson.Document;
import org.example.Database;
import org.example.Main;
import org.example.tokens;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class resetting implements Runnable {
    Database database = new Database();

    @Override
    public void run() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("reset checking....");
                boolean hasSent = true;
                String serverId = tokens.server_id;
                String reset_time = null;

                try {
                    reset_time = database.get(serverId, "serverId").get("reset-on").toString();
                    hasSent = (boolean) database.get(serverId, "serverId").get("summarySent");
                } catch (InterruptedException ignored) {
                }

                String time = ZonedDateTime.now(ZoneId.of("America/New_York"))
                        .format(DateTimeFormatter.ISO_LOCAL_TIME).split(":")[0];


                if (!time.equalsIgnoreCase(reset_time)) {
                    //resetting back summary send variable
                    try {
                        database.set(serverId, "serverId", "summarySent", false, false);
                    } catch (InterruptedException ignored) {
                    }
                }

                try {
                    if ((boolean) database.get(serverId, "serverId").get("reset")) {
                        if (!hasSent) {
                            if (time.equalsIgnoreCase(reset_time)) {
                                Document serverDoc = null;

                                try {
                                    serverDoc = database.get(serverId, "serverId");
                                    database.set(serverId, "serverId", "summarySent", true, false);
                                } catch (InterruptedException ignored) {
                                }

                                //getting necessary variables from database
                                TextChannel summaryChannel;
                                if (serverDoc.get("actionChannel") != null) {
                                    summaryChannel = Main.jda.getTextChannelById(serverDoc.get("actionChannel").toString());
                                } else {
                                    System.out.println("No Summary channel set!");
                                    return;
                                }

                                String pingRoleID = null;
                                if (serverDoc.get("roleToMention") != null) {
                                    pingRoleID = serverDoc.get("roleToMention").toString();
                                }

                                String channelId;
                                if (serverDoc.get("mainChat") != null) {
                                    channelId = serverDoc.get("mainChat").toString();
                                } else {
                                    System.out.println("main chat has not been set!");
                                    return;
                                }
                                sendLeaderboard sendLeaderboard = new sendLeaderboard(channelId, serverId);
                                Thread thread = new Thread(sendLeaderboard);
                                thread.start();

                                EmbedBuilder summary = new EmbedBuilder()
                                        .setTitle("Summary of the day")
                                        .setDescription(String.format("**Leaderboard for** <#%s> \n", channelId))
                                        .appendDescription(sendLeaderboard.getLeaderboard());

                                if (pingRoleID != null) {
                                    summaryChannel.sendMessageEmbeds(summary.build())
                                            .content(String.format("<@&%s>", pingRoleID)).queue();
                                }else{
                                        System.out.println("No ping roles!");
                                        summaryChannel.sendMessageEmbeds(summary.build()).queue();
                                }

                                Thread clearThread = new Thread(new clearLeaderboard(serverId));
                                clearThread.start();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 60_000);

    }
}
