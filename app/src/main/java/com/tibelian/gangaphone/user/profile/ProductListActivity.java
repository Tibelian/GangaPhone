package com.tibelian.gangaphone.user.profile;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tibelian.gangaphone.MainActivity;
import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.database.model.User;
import com.tibelian.gangaphone.messenger.ChatListActivity;

import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView mPostsRecyclerView;
    private PostListAdapter mPostAdapter;
    private Button mAddProductBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_products);

        // init recycler
        mPostsRecyclerView = findViewById(R.id.posts_profile_recycler);
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // add items to the recycler
        mPostAdapter = new PostListAdapter();
        mPostsRecyclerView.setAdapter(mPostAdapter);

        // load the products from the current session
        List<Product> posts = Session.get().getUser().getProducts();
        mPostAdapter.setPosts(posts);
        mPostAdapter.setContext(this);
        mPostAdapter.notifyDataSetChanged();

        //
        mAddProductBtn = findViewById(R.id.profile_addProduct);
        mAddProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(
                        ProductEditActivity.newIntent(ProductListActivity.this, 0));
            }
        });

    }


    private class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

        private List<Product> mPosts;
        private Context context;
        public void setPosts(List<Product> posts) {
            mPosts = posts;
        }
        public void setContext(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public PostListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostListAdapter.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_profile_product, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(PostListAdapter.ViewHolder viewHolder, final int position) {
            viewHolder.product = ( (Product) (mPosts.get(position)) );
            viewHolder.bind();
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public Product product;
            private TextView mProductTitle;
            private TextView mProductPrice;
            private TextView mProductViews;

            public ViewHolder(View v) {
                super(v);

                // on click item show product details
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(
                                ProductEditActivity.newIntent(ProductListActivity.this, product.getId()));
                    }
                });

                // bind xml textview
                mProductTitle = v.findViewById(R.id.productTitle);
                mProductPrice = v.findViewById(R.id.productPrice);
                mProductViews = v.findViewById(R.id.productViews);

            }

            public void bind()
            {
                mProductTitle.setText(product.getName());
                mProductPrice.setText(product.getPrice() + " €");

                String numViews = context.getResources().getString(R.string.num_views);
                mProductViews.setText(numViews + ": " + product.getVisits());
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main, menu);
        menu.findItem(R.id.menu_user).setTitle(R.string.menu_logout);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_msg:
                startActivity(new Intent(ProductListActivity.this, ChatListActivity.class));
                return true;
            case R.id.menu_user:
                Session.get().setUser(null);
                Session.get().setLoggedIn(false);
                startActivity(new Intent(ProductListActivity.this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
