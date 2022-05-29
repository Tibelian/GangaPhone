package com.tibelian.gangaphone;

import com.tibelian.gangaphone.database.model.User;
import com.tibelian.gangaphone.messenger.socket.MessengerManager;

public class Session {

    private static Session session;
    private User user;
    private boolean isLoggedIn = false;

    private MessengerManager messenger;


    private Session() {
        user = new User();
        // run thread
        messenger = new MessengerManager();
        messenger.start();
    }

    public static Session get() {
        if (session == null)
            session = new Session();
        return session;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
