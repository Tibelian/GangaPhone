package com.tibelian.gangaphone.user.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.messenger.ChatListActivity;
import com.tibelian.gangaphone.messenger.socket.SocketClient;

import java.util.List;

/**
 * The user's products list
 */
public class ProductListActivity extends AppCompatActivity {

    // member variables elements from the layout
    private RecyclerView mPostsRecyclerView;
    private PostListAdapter mPostAdapter;
    private Button mAddProductBtn;

    /**
     * This method is the first to run
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load layout
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

        getSupportActionBar().setSubtitle(R.string.app_slogan);

    }

    /**
     * The custom adapter
     */
    private class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

        // list of products
        private List<Product> mPosts;

        // the activity
        private Context context;

        // setters
        public void setPosts(List<Product> posts) {
            mPosts = posts;
        }
        public void setContext(Context context) {
            this.context = context;
        }

        /**
         * each product's generated layout
         * @param parent
         * @param viewType
         * @return
         */
        @NonNull
        @Override
        public PostListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostListAdapter.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_profile_product, parent, false)
            );
        }

        /**
         * insert data to the view
         * @param viewHolder
         * @param position
         */
        @Override
        public void onBindViewHolder(PostListAdapter.ViewHolder viewHolder, final int position) {
            viewHolder.product = ( (Product) (mPosts.get(position)) );
            viewHolder.bind();
        }

        /**
         * count the products
         * @return int
         */
        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        /**
         * The products view
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            // current product
            public Product product;

            // member variables, xml elements
            private TextView mProductTitle;
            private TextView mProductPrice;
            private TextView mProductViews;

            /**
             * constructor
             * @param v
             */
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

            /**
             * set visual data
             */
            public void bind()
            {
                mProductTitle.setText(product.getName());
                mProductPrice.setText(product.getPrice() + " €");
                String numViews = context.getResources().getString(R.string.num_views);
                mProductViews.setText(numViews + ": " + product.getVisits());
            }

        }

    }

    /**
     * Inflate the custom menu
     * @param menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main, menu);
        menu.findItem(R.id.menu_user).setTitle(R.string.menu_logout);
        menu.findItem(R.id.menu_user).setIcon(R.drawable.logout);
        return true;
    }

    /**
     * Events on click each menu option
     * @param item
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            // show messenger
            case R.id.menu_msg:
                startActivity(new Intent(ProductListActivity.this, ChatListActivity.class));
                return true;

            // logout button
            case R.id.menu_user:
                Session.get().setUser(null);
                Session.get().setLoggedIn(false);
                // important
                SocketClient.get().close();
                SocketClient.get().setAsNull();
                startActivity(new Intent(ProductListActivity.this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
