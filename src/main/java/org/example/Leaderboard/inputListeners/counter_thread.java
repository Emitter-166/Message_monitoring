package org.example.Leaderboard.inputListeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.example.Database;

import java.util.concurrent.CountDownLatch;

public class counter_thread implements Runnable {
    public static CountDownLatch counterRunning = new CountDownLatch(0);
    Database database = new Database();

    public counter_thread(MessageReceivedEvent e) {
        counterRunning = new CountDownLatch(1);
        this.e = e;
    }

    MessageReceivedEvent e;

    @Override
    public void run() {
        try {
            String userId = e.getAuthor().getId();
            String serverId = e.getGuild().getId();
            String channelId = e.getChannel().getId();

            if (!database.get(serverId, "serverId").get("channels").toString().contains(channelId)) {
                database.set(serverId, "serverId", "channels", channelId + " ", true);
            }
            if (!database.get(serverId, "serverId").get("users").toString().contains(userId)) {
                database.set(serverId, "serverId", "users", userId + " ", true);
            }
            try {
                if (!database.get(serverId, "serverId").get(channelId).toString().contains(userId)) {
                    database.set(serverId, "serverId", channelId, userId + " ", true);
                }
            } catch (Exception exception) {
                database.set(serverId, "serverId", channelId, userId + " ", true);
            }
            database.set(userId, "userId", channelId, 1, true);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        counterRunning.countDown();
    }
}
