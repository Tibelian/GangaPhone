package com.tibelian.gangaphone.product;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.Product;

import java.util.List;

public class ProductPagerActivity extends AppCompatActivity {

    private static final String EXTRA_PRODUCT_ID = "current_product_id";

    private ViewPager2 mViewPager;
    private List<Product> mProducts;

    public static Intent newIntent(Context packageContext, int productId) {
        Intent intent = new Intent(packageContext, ProductPagerActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_pager);

        //
        mProducts = DatabaseManager.get(this).getProducts(true);

        //
        initViewPager();

        // show the selected product
        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, 0);
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
                Product product = mProducts.get(position);
                return ProductDetailsFragment.newInstance(product.getId());
            }
            @Override
            public int getItemCount() {
                return mProducts.size();
            }
        });
    }

}
