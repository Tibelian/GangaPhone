package com.tibelian.gangaphone.messenger.socket;

import android.util.Log;

import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.utils.JsonMapper;
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
        client.open();

        while (client.isConnected()) {
            String msg = client.receive();
            try {
                // result[0] == command/operation
                // result[1] == args
                String[] result = msg.split("\n");
                switch (result[0]) {
                    case "identity":
                        int id = Session.get().getUser().getId();
                        client.send("identity\n"+id);
                        break;
                    case "new_message":
                        refreshMessages();
                        break;
                    case "is_online":
                        ArrayList<Integer> isOnlineIds = new ArrayList<>();
                        String[] isOnlineStr = result[1].trim().split(",");
                        for (String isOnlineId:isOnlineStr)
                            isOnlineIds.add(Integer.parseInt(isOnlineId));
                        whoIsConnected(isOnlineIds);
                        break;
                    case "quit":
                        client.close();
                        break;
                }
            }
            catch (Exception e) {
                Log.d("MessengerManager", "error --> " + e);
                Log.d("MessengerManager", "received --> " + msg);
            }
        }
    }

    private void whoIsConnected(ArrayList<Integer> users)
    {
        ArrayList<Chat> chats = Session.get().getUser().getChats();
        for (int id:users)
        {
            for(Chat c:chats) {
                if (c.getUser().getId() == id) {
                    c.getUser().setOnline(true);
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

    public static void notifyActivities()
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
