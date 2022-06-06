package com.tibelian.gangaphone;

import com.tibelian.gangaphone.database.model.User;
import com.tibelian.gangaphone.messenger.socket.MessengerManager;

/**
 * With this singleton class
 * we can manage the user's session
 */
public class Session {

    // only one Session object can exists on memory
    private static Session session;

    // the logged in User object
    private User user;

    // control variable
    private boolean isLoggedIn = false;

    // each Session has his own Messenger
    private MessengerManager messenger;


    /**
     * On this private constructor we
     * create the user object
     * and the messenger thread is started
     */
    private Session() {
        user = new User();
        // run thread
        messenger = new MessengerManager();
        messenger.start();
    }

    /**
     * This is the onliest way
     * to access the Session Object
     * @return Session
     */
    public static Session get() {
        if (session == null)
            session = new Session();
        return session;
    }

    /**
     * obtain the logged in user
     * @return User
     */
    public User getUser() {
        return user;
    }

    /**
     * sets the logged in user
     * @param user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * detect if user has logged in
     * @return boolean
     */
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    /**
     * sets the logged in control variable
     * @param loggedIn
     */
    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
