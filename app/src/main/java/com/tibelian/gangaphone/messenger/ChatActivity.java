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

    private static final String EXTRA_USER_ID = "current_user_id";
    private int uid;

    public static Intent newIntent(Context packageContext, int uid){
        Intent intent = new Intent(packageContext, ChatActivity.class);
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

    }

    private void initMessagesFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if(fragment == null){
            fragment = MessageFragment.newInstance(uid);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

}
