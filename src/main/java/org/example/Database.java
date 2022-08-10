package org.example;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
 import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class Database extends ListenerAdapter {
    public MongoCollection<Document> collection;
    public MongoDatabase database;
    public String collectionName  = "messages";
    public Database() {
        String uri = tokens.uri; //Mongo DB uri
        MongoClientURI clientURI = new MongoClientURI(uri);
        MongoClient client = new MongoClient(clientURI);
        database = client.getDatabase("messages"); //getting database and collection
        collection = database.getCollection(collectionName);
    }

    public void set(String Id, String field, String Key, Object value, boolean isAdd) throws InterruptedException {
        //for ease of understanding while coding, this method is added, there is no real use of it
        updateDB(Id, field, Key, value, isAdd);
    }

    public Document get(String Id, String field) throws InterruptedException {
        //it will return server settings from database collection
        try {
            return collection.find(Filters.eq(field, Id)).cursor().next();
        } catch (NoSuchElementException exception) {
            if (field.equalsIgnoreCase("serverId")) {
                createDB(Id);
            }else if(field.equalsIgnoreCase("userId")){
                createUserDB(Id);
            }
            return collection.find(Filters.eq(field, Id)).cursor().next();
        }

    }

    public void drop(){
        ListIndexesIterable<Document> Indexes = collection.listIndexes();
        collection.drop();
        database.createCollection(collectionName);
        collection = database.getCollection(collectionName);
//        for (Document index : Indexes) {
//            collection.createIndex(index);
//        }
    }
    private void createDB(String Id) {
        //server config, here is the template used to make new settings document on db collection
        Document document = new Document("serverId", Id)
                .append("actionChannel", "")
                .append("roleToMention", "")
                .append("mainChat", "")
                .append("reset", true)
                .append("channels", "")
                .append("users", "")
                .append("summarySent", false)
                .append("reset-on", "00");
        collection.insertOne(document);

    }

    private void createUserDB(String Id) {
        //server config, here is the template used to make new settings document on db collection
        Document document = new Document("userId", Id); //ad_name
        collection.insertOne(document);

    }


    private void updateDB(String Id, String field, String key, Object value, boolean isAdd) throws InterruptedException {
        //for server
        Document document = null;
        try {
            //it will try to assign value to document, if there is no server settings for role logging, it will create one
            document = collection.find(Filters.eq(field, Id)).cursor().next();
        } catch (NoSuchElementException exception) {
            if (field.equalsIgnoreCase("serverId")) {
                createDB(Id);
            }else if(field.equalsIgnoreCase("userId")){
                createUserDB(Id);
            }
            document = collection.find(Filters.eq(field, Id)).cursor().next();
        }
        if (!isAdd) {
            //it will check if the values should be added with the previous value
            Document Updatedocument = new Document(key, value);
            Bson updateKey = new Document("$set", Updatedocument);
            collection.updateOne(document, updateKey);
        } else {
            Document Updatedocument = null;
            if (value.getClass().getSimpleName().equalsIgnoreCase("Integer")){
                if(document.get(key) == null){
                    Database database = new Database();
                    database.set(Id, "userId", key, value, false);
                    return;
                }
                Updatedocument = new Document(key, ((int) document.get(key) + (int) value));

            } else if(value.getClass().getSimpleName().equalsIgnoreCase("String")){
                if(document.get(key) == null){
                    Database database = new Database();
                    database.set(Id, "serverId", key, value, false);
                    return;
                }
                Updatedocument = new Document(key, (document.get(key) + (String) value));
            }

            Bson updateKey = new Document("$set", Updatedocument);
            collection.updateOne(document, updateKey);

        }
    }

}
