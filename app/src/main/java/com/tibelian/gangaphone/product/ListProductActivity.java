package com.tibelian.gangaphone.product;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.tibelian.gangaphone.R;

public class ListProductActivity extends AppCompatActivity {

    private static final String DIALOG_FILTER = "dialog_filter";

    private Button mShowFilterDialogBtn;
    private Fragment mProductsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        mShowFilterDialogBtn = findViewById(R.id.btnShowFilterDialog);
        mShowFilterDialogBtn.setOnClickListener(v -> {
            FilterFragment filterFragment = new FilterFragment();
            filterFragment.show(getSupportFragmentManager(), DIALOG_FILTER);
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

}
