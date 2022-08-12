package org.example.Leaderboard.inputListeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.Database;
import org.example.Leaderboard.operations.clearLeaderboard;
import org.jetbrains.annotations.NotNull;

public class counter extends ListenerAdapter {
    Database database = new Database();
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if(clearLeaderboard.cleanerRunning) return;
        Thread counter = new Thread(new counter_thread(e, database));
        counter.start();
    }
}

