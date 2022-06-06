package com.tibelian.gangaphone.messenger.socket;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.tibelian.gangaphone.App;
import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.utils.JsonMapper;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.database.model.Chat;
import com.tibelian.gangaphone.messenger.ChatActivity;
import com.tibelian.gangaphone.messenger.ChatListActivity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Manage the connection with the messenger server
 */
public class MessengerManager extends Thread {

    // the connection
    private SocketClient client;

    // notification channel id
    private static final String CHANNEL_ID = "MESSENGER";

    /**
     * run the thread
     */
    @Override
    public void run() {
        init();
    }

    /**
     *
     */
    public void init() {

        // access the singleton
        client = SocketClient.get();
        // start connection
        client.open();

        // while client is connected correctly
        while (client.isConnected())
        {
            // wait for input data
            String msg = client.receive();
            try {
                // now interpret received message
                // all input data must have the next pattern:
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

    /**
     * method to set which users are connected
     * @param users
     */
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
        // update the view
        notifyActivities(false);
    }

    /**
     * update messages
     */
    private void refreshMessages()
    {
        try {
            // load messages from database
            Session.get().getUser().getChats().clear();
            Session.get().getUser().setChats(
                    JsonMapper.mapChats(
                            new RestApi().findMessages(
                                    Session.get().getUser().getId()
                            ), Session.get().getUser()
                    )
            );
            // update the view
            notifyActivities(true);
        }
        catch (IOException e) {
            Log.e("MessengerManager", "refreshMessages() error --> " + e);
        }
    }

    /**
     * detect if current view has to be rendered again
     * else then send a notification if sdk version is compatible
     * @param alsoUser
     */
    public static void notifyActivities(boolean alsoUser)
    {
        // check current activity
        if (ChatActivity.isActive()) {
            // if ChatFragment is active then refresh the messages
            ChatActivity.getContext().update();
        }
        else if (ChatListActivity.isActive()) {
            // if on ChatList is active then refresh the adapter list of users
            ChatListActivity.getContext().loadChats(false);
        }
        else if (alsoUser) {
            Log.i("MessengerManager", "notifyActivities alsoUser");
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
                    NotificationManager nm = (NotificationManager) App.getContext()
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.createNotificationChannel(channel);

                    // create notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getContext(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.logo_icon)
                            .setContentTitle(App.getContext().getString(R.string.newMessageTitle))
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    // send notif
                    nm.notify(1, builder.build());
                }
            }
            catch (Exception e) {
                Log.e("MessengerManager", "notifyActivities error --> " + e);
            }
        }
    }

}
