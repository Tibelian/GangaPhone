package com.tibelian.gangaphone.product;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.database.model.Product;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Create a product list slider
 */
public class ProductPagerActivity extends AppCompatActivity {

    // constant variables
    private static final String EXTRA_PRODUCT_ID = "current_product_id";
    private static final String EXTRA_SESSION = "load_from_session";

    // member variables
    private ViewPager2 mViewPager;
    private ArrayList<Product> mProducts;

    // the list of products already viewed
    private ArrayList<Integer> visitedProducts = new ArrayList<>();

    /**
     * Create intent of ProductPager to show specific product
     * @param packageContext
     * @param productId
     * @return Intent
     */
    public static Intent newIntent(Context packageContext, int productId) {
        Intent intent = new Intent(packageContext, ProductPagerActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        return intent;
    }
    public static Intent newIntent(Context packageContext, int productId, boolean fromSession) {
        Intent intent = newIntent(packageContext, productId);
        intent.putExtra(EXTRA_SESSION, fromSession);
        return intent;
    }

    /**
     * first method to execute after activity is initialized
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // load main view from layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_pager);

        // check if this activity has received args
        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, 0);
        boolean fromSession = getIntent().getBooleanExtra(EXTRA_SESSION, false);

        // load product from session
        // or obtain it from the database
        if (fromSession)
            mProducts = Session.get().getUser().getProducts();
        else {
            // I should execute this on a new thread
            try {
                mProducts = new RestApi().searchProducts(true);
            } catch (IOException e) {
                Log.e("ProductPagerActivity", "error --> " + e);
            }
        }

        // initialize the slider
        initViewPager();

        // show the selected product
        for(int i = 0; i < mProducts.size(); i++){
            if (mProducts.get(i).getId() == productId) {
                mViewPager.setCurrentItem(i);
                updateSubtitleCounter();
                break;
            }
        }
    }

    /**
     * current product counter
     */
    private void updateSubtitleCounter() {
        try {
            // text
            String subtitle = getString(R.string.product_details);

            // current item
            subtitle += " - " + (mViewPager.getCurrentItem() + 1);

            // total items
            subtitle += "/" + mViewPager.getAdapter().getItemCount();

            // udpate the menu's subtitle
            getSupportActionBar().setSubtitle(subtitle);
        } catch (NullPointerException e) {
            Log.e("ProductPagerActivity", "updateSubtitleCounter error --> " + e);
        }
    }

    /**
     * create slider
     */
    private void initViewPager() {
        // find the view into the layout
        mViewPager = findViewById(R.id.activityProductViewPager);
        // init adapter
        mViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Product currentProduct = mProducts.get(position);
                updateVisits(currentProduct); // increase views
                return ProductDetailsFragment.newInstance(currentProduct.getId());
            }
            @Override
            public int getItemCount() {
                return mProducts.size();
            }
        });
        // update menu's subtitle on change page / on slide
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateSubtitleCounter();
            }
        });
    }

    /**
     *
     * @param current
     */
    private void updateVisits(Product current) {

        // do not update visits if product belongs to the logged in user
        ArrayList<Product> uProducts = Session.get().getUser().getProducts();
        for (Product up:uProducts)
            if (up.getId() == current.getId())
                return;

        // if first time viewing item then increase the product visits
        if (!visitedProducts.contains(current.getId()))
        {
            visitedProducts.add(current.getId());
            try {

                new RestApi().addProductVisit(current.getId());

                // update also the product from the memory
                current.setVisits(current.getVisits() + 1);

            } catch (IOException e) {
                Log.e("ProductPagerActivity", "updateVisits error --> " + e);
            }
        }
    }

}
