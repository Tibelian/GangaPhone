package com.tibelian.gangaphone.product;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tibelian.gangaphone.MainActivity;
import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.api.ImageLoadTask;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.messenger.ChatListActivity;
import com.tibelian.gangaphone.user.profile.ProductListActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment with products
 */
public class ProductsFragment extends Fragment {

    // constant with the number of products per row
    private final int PRODUCTS_PER_ROW = 2;

    // member variables - xml elements
    private TextView mNumPostsFound;
    private RecyclerView mPostsRecyclerView;
    private PostListAdapter mPostAdapter;

    /**
     * Fragment's creation
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable menu on this fragment
        setHasOptionsMenu(true);
    }

    /**
     * Generate view with the inflater using the layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // attach view
        View view = inflater.inflate(R.layout.fragment_list_products, container,false);

        // bind xml elements
        initMemberVariables(view);

        // init recycler
        mPostsRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), PRODUCTS_PER_ROW));
        mPostAdapter = new PostListAdapter();
        mPostsRecyclerView.setAdapter(mPostAdapter);

        // load products form db
        reloadProducts();

        return view;
    }

    /**
     * bind xml elements
     * @param view
     */
    private void initMemberVariables(View view) {
        mNumPostsFound = view.findViewById(R.id.num_posts_found);
        mPostsRecyclerView = view.findViewById(R.id.posts_recycler);
    }

    /**
     * update the product's adapter
     */
    public void reloadProducts() {
        // we need a new thread
        // so the main thread keeps awake
        new Thread(new Runnable() {
            @Override
            public void run() {
                // obtain products
                try {
                    final ArrayList<Product> posts = new RestApi().searchProducts(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPostAdapter.setPosts(posts);
                            mPostAdapter.notifyDataSetChanged();
                            // show results num
                            mNumPostsFound.setText(posts.size() + " " + getString(R.string.results_found));
                        }
                    });
                } catch (IOException e) {
                    Log.e("ProductFragment", "reloadProducts error --> " + e);
                }
            }
        }).start();
    }

    /**
     * Custom adapter to manage products form the recycler view
     */
    private class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

        // list of products to show
        private List<Product> mPosts = new ArrayList<>();

        // setter
        public void setPosts(List<Product> posts) {
            mPosts = posts;
        }

        /**
         * Load layout
         * @param parent
         * @param viewType
         * @return ViewHolder
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_product, parent, false)
            );
        }

        /**
         * attach data
         * @param viewHolder
         * @param position
         */
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            viewHolder.product = ( (Product) (mPosts.get(position)) );
            viewHolder.bind();
        }

        /**
         * number of products
         * @return int
         */
        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        /**
         * Custom View Layout for each product
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            // current product
            public Product product;

            // member variables - xml elements
            private TextView mProductTitle;
            private TextView mProductDate;
            private TextView mProductLocation;
            private TextView mProductPrice;
            private ImageView mProductThumbnail;

            // constructor
            public ViewHolder(View v) {
                super(v);

                // on click item show product details
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(
                            ProductPagerActivity.newIntent(getActivity(), product.getId()));
                    }
                });

                // bind xml textview
                mProductTitle = v.findViewById(R.id.productTitle);
                mProductDate = v.findViewById(R.id.productDate);
                mProductLocation = v.findViewById(R.id.productLocation);
                mProductPrice = v.findViewById(R.id.productPrice);
                mProductThumbnail = v.findViewById(R.id.productThumbnail);

            }

            /**
             * load data
             */
            public void bind()
            {
                // parse time to string
                String timeAgo = DateUtils.getRelativeTimeSpanString(product.getDate().getTime()).toString();

                // bind data
                mProductDate.setText(timeAgo);
                mProductTitle.setText(product.getName());
                mProductLocation.setText(product.getOwner().getLocation());
                mProductPrice.setText(product.getPrice() + " €");

                // load images
                if (product.getPictures().size() > 0)
                    new ImageLoadTask(product.getPictures().get(0).getUrl(), mProductThumbnail)
                            .execute();
            }

        }

    }

    /**
     * load layout of options to show on the menu
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_main, menu);

        // disable this buttons for guest users
        if (Session.get().isLoggedIn() == false) {
            menu.findItem(R.id.menu_msg).setVisible(false);
            menu.findItem(R.id.menu_user).setTitle(R.string.menu_login);
        }

    }

    /**
     * on click menu's buttons events
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            // open messenger
            case R.id.menu_msg:
                startActivity(new Intent(getContext(), ChatListActivity.class));
                return true;

            // show profile or login form if guest user
            case R.id.menu_user:
                if (Session.get().isLoggedIn())
                    startActivity(new Intent(getContext(), ProductListActivity.class));
                else
                    startActivity(new Intent(getContext(), MainActivity.class)); // login
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
