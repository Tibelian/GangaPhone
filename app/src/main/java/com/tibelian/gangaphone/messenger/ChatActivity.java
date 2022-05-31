package com.tibelian.gangaphone.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.messenger.socket.MessengerManager;

public class ChatActivity extends AppCompatActivity {

    private static ChatActivity context = null;
    private static boolean active = false;
    private static MessageFragment fragment = null;

    private static final String EXTRA_USER_ID = "current_user_id";
    private int uid;

    public static Intent newIntent(Context packageContext, int uid) {
        Intent intent;

        // detect screen size
        int screenSize = packageContext.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE)
            intent = new Intent(packageContext, ChatListActivity.class);
        else
            intent = new Intent(packageContext, ChatActivity.class);

        intent.putExtra(EXTRA_USER_ID, uid);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // obtain user from intent
        uid = getIntent().getIntExtra(EXTRA_USER_ID, 0);

        // load messages
        initMessagesFragment();

        getSupportActionBar().setSubtitle(R.string.chatting);

    }

    private void initMessagesFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = (MessageFragment) fragmentManager.findFragmentById(R.id.fragment_container);
        if(fragment == null){
            fragment = MessageFragment.newInstance(uid);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }








    @Override
    public void onStart() {
        super.onStart();
        active = true;
        context = this;
    }
    @Override
    public void onStop() {
        super.onStop();
        active = false;
        context = null;
    }
    public static boolean isActive() {
        return active;
    }
    public static ChatActivity getContext() {
        return context;
    }
    public void update() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragment.loadMessages(false);
            }
        });
    }

}
