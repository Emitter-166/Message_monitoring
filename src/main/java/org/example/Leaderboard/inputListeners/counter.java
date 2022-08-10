package org.example.Leaderboard.inputListeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.Leaderboard.operations.clearLeaderboard;
import org.jetbrains.annotations.NotNull;

public class counter extends ListenerAdapter {

    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if(clearLeaderboard.cleanerRunning) return;
        Thread counter = new Thread(new counter_thread(e));
        counter.start();
    }
}

