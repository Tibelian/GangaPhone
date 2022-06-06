package com.tibelian.gangaphone;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * This activity renders only one fragment
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

    /**
     * requires one fragment object
     * @return Fragment
     */
    protected abstract Fragment createFragment();

    /**
     * the layout's id
     * @return
     */
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    /**
     * when activity is created
     * the fragment is loaded
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        // hide menu by default
        getSupportActionBar().hide();

        // creates the fragment using the abstract method
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if(fragment == null)
            replaceFragment(createFragment());
    }

    /**
     * this method replaces the current fragment
     * by another one received as parameter
     * @param newFragment
     */
    public void replaceFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, newFragment)
                .commit();
    }
}
