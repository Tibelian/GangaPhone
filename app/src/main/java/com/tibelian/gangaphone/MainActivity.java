package com.tibelian.gangaphone;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.api.HttpRequest;
import com.tibelian.gangaphone.user.LoginFragment;

import java.io.IOException;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }

    public void onBackPressed() {
        // lock back button
        return;
    }

}