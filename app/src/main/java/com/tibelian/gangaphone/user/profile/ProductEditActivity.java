package com.tibelian.gangaphone.user.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.database.model.Product;

/**
 * Edit or Create product
 */
public class ProductEditActivity extends AppCompatActivity {

    // constant variable to control intent args
    private static final String EXTRA_PRODUCT_ID = "product_id";

    /**
     * Obtain the ProductEditActivity Intent object
     * @param packageContext
     * @param productId
     * @return
     */
    public static Intent newIntent(Context packageContext, int productId){
        Intent intent = new Intent(packageContext, ProductEditActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        return intent;
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // obtain current product's identifier
        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, 0);

        // check if it's a new product
        if (productId == 0)
            getSupportActionBar().setSubtitle(R.string.edit_p_new);
        else
            getSupportActionBar().setSubtitle(R.string.edit_pTitle);

        // prepare the form
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if(fragment == null){
            fragment = ProductEditFragment.newInstance(productId);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

    }
}
