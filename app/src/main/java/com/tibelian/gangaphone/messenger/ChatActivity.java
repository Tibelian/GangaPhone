package com.tibelian.gangaphone.messenger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.tibelian.gangaphone.R;

public class ChatActivity extends AppCompatActivity {

    private static final String EXTRA_USERNAME = "current_username";
    private String username;

    public static Intent newIntent(Context packageContext, String username){
        Intent intent = new Intent(packageContext, ChatActivity.class);
        intent.putExtra(EXTRA_USERNAME, username);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // obtain username from intent
        username = getIntent().getStringExtra(EXTRA_USERNAME);

        // load messages
        initMessagesFragment();

    }

    private void initMessagesFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if(fragment == null){
            fragment = MessageFragment.newInstance(username);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

}
