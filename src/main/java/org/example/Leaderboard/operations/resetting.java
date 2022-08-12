package org.example.Leaderboard.operations;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
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

                                String pingRoleID;
                                if (serverDoc.get("roleToMention") != null) {
                                    pingRoleID = serverDoc.get("roleToMention").toString();
                                }else{
                                    pingRoleID = "";
                                }

                                String channelId;
                                if (serverDoc.get("mainChat") != null) {
                                    channelId = serverDoc.get("mainChat").toString();
                                } else {
                                    System.out.println("main chat has not been set!");
                                    return;
                                }
                                sendLeaderboard sendLeaderboard = new sendLeaderboard(channelId, serverId, database);
                                Thread thread = new Thread(sendLeaderboard);
                                thread.start();


                                Guild guild =  Main.jda.getGuildById(serverId);
                                Document db = database.get(serverId, "serverId");
                                Role role = guild.getRoleById(db.get("roleToAdd").toString());

                                try{
                                    String previous_winner = db.getString("previousWinner");
                                    guild.removeRoleFromMember(guild.retrieveMemberById(previous_winner).complete(), role).queue();
                                }catch (Exception ignored){}


                                StringBuilder leaderboard = sendLeaderboard.getLeaderboard();
                                String id = leaderboard.toString().split("\n")[0].split("-")[0].split("`1.`")[1].replace("<@", "").replace("> ", "").
                                        replace(" ", "");
                                guild.addRoleToMember(guild.retrieveMemberById(id).complete(), role).queue();

                                database.set(serverId, "serverId", "previousWinner", id, false);
                                System.out.println("Winner of the day: " + id);

                                EmbedBuilder summary = new EmbedBuilder()
                                        .setTitle("Summary of the day")
                                        .setDescription(String.format("**Leaderboard for** <#%s> \n", channelId))
                                        .appendDescription(leaderboard);

                                if (pingRoleID.length() != 0) {
                                    summaryChannel.sendMessageEmbeds(summary.build())
                                            .content(String.format("<@&%s>", pingRoleID)).queue();
                                }else{
                                        System.out.println("No ping roles!");
                                        summaryChannel.sendMessageEmbeds(summary.build()).queue();
                                }

                                Thread clearThread = new Thread(new clearLeaderboard(serverId, database));
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
