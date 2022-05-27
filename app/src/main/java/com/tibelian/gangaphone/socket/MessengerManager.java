package com.tibelian.gangaphone.socket;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.JsonMapper;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.database.model.Chat;
import com.tibelian.gangaphone.messenger.ChatActivity;
import com.tibelian.gangaphone.messenger.ChatListActivity;

import java.io.IOException;
import java.util.ArrayList;

public class MessengerManager extends Thread {

    private SocketClient client;

    @Override
    public void run() {
        init();
    }

    public void init() {
        client = SocketClient.get();
        while (true) {
            String json = client.receive();
            try {
                JsonObject result = new Gson().fromJson(json, JsonObject.class);
                String operation = result.get("operation").getAsString();
                switch (operation) {
                    case "identity":
                        int id = Session.get().getUser().getId();
                        client.send("{\"operation\":\"identity\", \"user_id\":"+id+"}");
                        break;
                    case "new_message":
                        refreshMessages();
                        break;
                    case "is_online":
                        whoIsConnected(result.get("data").getAsJsonArray());
                        break;
                    case "quit":
                        client.close();
                        break;
                }
            }
            catch (Exception e) {
                Log.d("MessengerManager", "error --> " + e);
                Log.d("MessengerManager", "received --> " + json);
            }
        }
    }

    private void whoIsConnected(JsonArray users)
    {
        ArrayList<Chat> chats = Session.get().getUser().getChats();
        for (JsonElement je:users)
        {
            JsonObject jo = je.getAsJsonObject();
            int userId = jo.get("user_id").getAsInt();
            boolean isOnline = jo.get("online").getAsBoolean();
            for(Chat c:chats) {
                if (c.getUser().getId() == userId) {
                    c.getUser().setOnline(isOnline);
                    break;
                }
            }
        }
        notifyActivities();
    }

    private void refreshMessages()
    {
        try {
            // load messages
            Session.get().getUser().getChats().clear();
            Session.get().getUser().setChats(
                    JsonMapper.mapChats(
                            new RestApi().findMessages(
                                    Session.get().getUser().getId()
                            ), Session.get().getUser()
                    )
            );
            notifyActivities();
        }
        catch (IOException e) {
            Log.e("MessengerManager", "refreshMessages() error --> " + e);
        }
    }

    private void notifyActivities()
    {
        // check current activity
        if (ChatActivity.isActive()) {
            // if ChatFragment is active then refresh the messages
            ChatActivity.update();
        }
        else if (ChatListActivity.isActive()) {
            // if on ChatList is active then refresh the adapter list of users
            ChatListActivity.loadChats(false);
        }
    }

}
