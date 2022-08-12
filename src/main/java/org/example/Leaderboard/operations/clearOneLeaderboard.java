package org.example.Leaderboard.operations;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.example.Database;
import static org.example.Leaderboard.inputListeners.counter_thread.counterRunning;

public class clearOneLeaderboard implements Runnable{
    public clearOneLeaderboard(String channelId, String serverId, Database database) {
        this.channelId = channelId;
        this.serverId = serverId;
        this.database = database;
    }

    String channelId;
    String serverId;
    Database database;
    @Override
    public void run() {
        try {
            counterRunning.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        clearLeaderboard.cleanerRunning = true;

        String[] users = new String[0];
        try {
            users = database.get(serverId, "serverId").get("users").toString().split(" ");
        }catch(InterruptedException ignored) {}

        for (String user : users) {
            Document document = null;
            Document updateDocument = null;
            try {
                document = database.get(user, "userId");
                updateDocument = new Document(channelId, document.get(channelId));
            } catch (Exception ignored){}

            Bson key = new Document("$unset", updateDocument);
            database.collection.updateOne(document, key);
        }

        try {
            database.set(serverId, "serverId", channelId, "", false);
            database.set(serverId, "serverId", "channels", database.get(serverId, "serverId").get("channels").toString().replace(channelId + " ", ""), false);
        }catch(InterruptedException ignored){}



        clearLeaderboard.cleanerRunning = false;
    }
}
