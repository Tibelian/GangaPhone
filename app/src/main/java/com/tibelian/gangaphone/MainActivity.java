package com.tibelian.gangaphone;

import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.user.LoginFragment;

/**
 * The first activity to show
 * it is a SingleFragmentActivity
 */
public class MainActivity extends SingleFragmentActivity {

    /**
     * we have to use declare this method
     * to return the first visual content
     * @return Fragment
     */
    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }

    /**
     * override default activity
     * back function to prevent
     * unexpected exit or duplicated session
     */
    @Override
    public void onBackPressed() {
        // lock back button
        return;
    }

}