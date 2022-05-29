package com.tibelian.gangaphone;

import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.user.LoginFragment;

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