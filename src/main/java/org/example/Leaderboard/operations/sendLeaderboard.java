package org.example.Leaderboard.operations;
import org.example.Database;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class sendLeaderboard implements Runnable{
    Database database;
    StringBuilder leaderboard;
    String channelId;
    String serverId;
    public sendLeaderboard(String channelId, String serverId, Database database) {
        this.database = database;
        this.channelId = channelId;
        this.serverId = serverId;
        latch = new CountDownLatch(1);
        leaderboard = new StringBuilder();
    }

    CountDownLatch latch;

    public StringBuilder getLeaderboard() throws InterruptedException {
        latch.await();
        return leaderboard;
    }

    @Override
    public void run() {
        Map<String, Integer> result = new HashMap<>();
        String[] users;

        try {
            if(database.get(serverId, "serverId").get(channelId) == null){
                leaderboard.append("`No messages on that channel!`");
                latch.countDown();
                return;
            }
            users = database.get(serverId, "serverId").get(channelId).toString().split(" ");
            for(String user : users){
                try{
                    result.put(user, (int) database.get(user, "userId").get(channelId));
                }catch (Exception ignored){}
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<Map.Entry<String, Integer>> sorted = result.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toList());
        Map.Entry<String, Integer> entry;

        for(int i = 0; i < sorted.size(); i++){
            if(i > 19){
                break;
            }
            entry =  sorted.get(i);
            leaderboard.append(String.format("`%s.` <@%s> - `%s` messages \n", i+1, entry.getKey(), entry.getValue()));
        }
        latch.countDown();
    }

    public static void main(String[] args) {
       String s = "`%s.` <@%s> - `%s` messages \n" + "`%s.` <@%s> - `%s` messages \n" ;
       String id = s.split("\n")[0].split("-")[0].split("`%s.`")[1].replace("<@", "").replace("> ", "");
        System.out.println(id);
    }

}
