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

public class ProductPagerActivity extends AppCompatActivity {

    private static final String EXTRA_PRODUCT_ID = "current_product_id";
    private static final String EXTRA_SESSION = "load_from_session";

    private ViewPager2 mViewPager;
    private ArrayList<Product> mProducts;

    private ArrayList<Integer> visitedProducts = new ArrayList<>();

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_pager);

        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, 0);
        boolean fromSession = getIntent().getBooleanExtra(EXTRA_SESSION, false);

        //
        if (fromSession)
            mProducts = Session.get().getUser().getProducts();
        else {
            try {
                mProducts = new RestApi().searchProducts(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //
        initViewPager();

        // show the selected product
        for(int i = 0; i < mProducts.size(); i++){
            if (mProducts.get(i).getId() == productId) {
                mViewPager.setCurrentItem(i); break;
            }
        }

    }

    private void initViewPager() {
        mViewPager = findViewById(R.id.activityProductViewPager);
        mViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Product currentProduct = mProducts.get(position);
                updateVisits(currentProduct);
                return ProductDetailsFragment.newInstance(currentProduct.getId());
            }
            @Override
            public int getItemCount() {
                return mProducts.size();
            }
        });
    }

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
