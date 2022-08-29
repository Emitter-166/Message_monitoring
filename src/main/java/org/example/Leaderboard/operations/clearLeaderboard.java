package org.example.Leaderboard.operations;
import org.bson.Document;
import org.example.Database;
import static org.example.Leaderboard.inputListeners.counter_thread.counterRunning;

public class clearLeaderboard implements Runnable{
    String serverId;
    public static boolean cleanerRunning;
    Database database;
    public clearLeaderboard(String serverId, Database database) {
        this.database = database;
        cleanerRunning = true;
        this.serverId = serverId;
    }

    @Override
    public void run() {
        try {
            counterRunning.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Document serverConfig;
        try {
            serverConfig = database.get(serverId, "serverId");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String[] channels = serverConfig.get("channels").toString().split(" ");
        for(String channelId : channels){
            serverConfig.remove(channelId);
        }

        serverConfig.put("channels", "");
        serverConfig.put("users", "");

        try {
            database.drop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Database.collection.insertOne(serverConfig);
        cleanerRunning = false;
    }

}
