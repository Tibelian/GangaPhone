package com.tibelian.gangaphone.product;

import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.async.ImageLoadTask;
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.Product;

import java.util.List;


public class ProductsFragment extends Fragment {

    private final int PRODUCTS_PER_ROW = 2;

    private TextView mNumPostsFound;
    private RecyclerView mPostsRecyclerView;
    private PostListAdapter mPostAdapter;

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


    private void initMemberVariables(View view) {
        mNumPostsFound = view.findViewById(R.id.num_posts_found);
        mPostsRecyclerView = view.findViewById(R.id.posts_recycler);
    }

    public void reloadProducts() {
        // obtain products
        List<Product> posts = DatabaseManager.get(getActivity()).getProducts(true);
        mPostAdapter.setPosts(posts);
        // show results num
        mNumPostsFound.setText(posts.size() + " results found");
    }





    private class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

        private List<Product> mPosts;
        public void setPosts(List<Product> posts) {
            mPosts = posts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_product, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
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
            private TextView mProductDate;
            private TextView mProductLocation;
            private TextView mProductPrice;
            private ImageView mProductThumbnail;

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

            public void bind()
            {
                String timeAgo = DateUtils.getRelativeTimeSpanString(product.getDate().getTime()).toString();

                // bind data
                mProductDate.setText(timeAgo);
                mProductTitle.setText(product.getName());
                mProductLocation.setText(product.getOwner().getLocation());
                mProductPrice.setText(product.getPrice() + " €");

                Log.e("product fragment", product.getPictures().size() + "");
                if (product.getPictures().size() > 0) {
                    Log.e("image", "product is loading image");
                    new ImageLoadTask(product.getPictures().get(0).getUrl(), mProductThumbnail)
                            .execute();
                }
                else {
                    Log.e("image", "product has no images");
                }

            }

        }

    }



}
