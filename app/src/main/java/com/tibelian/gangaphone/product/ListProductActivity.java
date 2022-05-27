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

public class ListProductActivity extends AppCompatActivity {

    public static final String DIALOG_FILTER = "dialog_filter";
    public static final String DIALOG_FILTER_APPLIED = "dialog_filter_applied";

    private Button mShowFilterDialogBtn;
    private Fragment mProductsFragment;
    //private TextView mCurrentSearchText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        //mCurrentSearchText = findViewById(R.id.currentSearchText);

        mShowFilterDialogBtn = findViewById(R.id.btnShowFilterDialog);
        mShowFilterDialogBtn.setOnClickListener(v -> {
            FilterFragment filterFragment = new FilterFragment();
            filterFragment.show(getSupportFragmentManager(), DIALOG_FILTER);
        });

        getSupportFragmentManager().setFragmentResultListener(DIALOG_FILTER, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (result.getBoolean(DIALOG_FILTER_APPLIED))
                    searchProducts();
            }
        });

        initProductsFragment();
    }

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

    private void searchProducts() {
        //mCurrentSearchText.setText(CurrentFilter.getSearch());
        ((ProductsFragment) mProductsFragment).reloadProducts();
    }


    public void onBackPressed() {
        if (Session.get().isLoggedIn())
            return;
        super.onBackPressed();
    }

}
