package com.tibelian.gangaphone.product;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;

/**
 * The products list
 * First activity to show after login
 */
public class ListProductActivity extends AppCompatActivity {

    // constants to control input and output intent data
    public static final String DIALOG_FILTER = "dialog_filter";
    public static final String DIALOG_FILTER_APPLIED = "dialog_filter_applied";

    // member variables, visual elements
    private Button mShowFilterDialogBtn;
    private Fragment mProductsFragment;
    //private TextView mCurrentSearchText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // load layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        // bind xml elements
        mShowFilterDialogBtn = findViewById(R.id.btnShowFilterDialog);
        mShowFilterDialogBtn.setOnClickListener(v -> {
            FilterFragment filterFragment = new FilterFragment();
            filterFragment.show(getSupportFragmentManager(), DIALOG_FILTER);
        });

        // create a listener, when filter's fragment finish
        getSupportFragmentManager().setFragmentResultListener(DIALOG_FILTER, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // update the adapter with the new products
                if (result.getBoolean(DIALOG_FILTER_APPLIED))
                    ((ProductsFragment) mProductsFragment).reloadProducts();
            }
        });

        // initialize the product's fragment
        initProductsFragment();

        // set slogan on the menu
        getSupportActionBar().setSubtitle(R.string.app_slogan);

    }

    /**
     * initialize the products fragment
     */
    private void initProductsFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mProductsFragment = fragmentManager.findFragmentById(R.id.fragment_list);
        if(mProductsFragment == null){
            mProductsFragment = new ProductsFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_list, mProductsFragment)
                    .commit();
        }
    }

    /**
     * disable back button if
     * user has logged in
     */
    public void onBackPressed() {
        if (Session.get().isLoggedIn())
            return;
        super.onBackPressed();
    }

}
