package com.tibelian.gangaphone.product;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.async.ImageLoadTask;
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.Product;

public class ProductDetailsFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "product_id";
    private Product mProduct;

    private ImageView mPicture;
    private TextView mTitle;
    private TextView mDate;
    private TextView mLocation;
    private TextView mPrice;
    private TextView mDescription;
    private TextView mStatus;

    public static ProductDetailsFragment newInstance(int productId) {
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int productId = getArguments().getInt(ARG_PRODUCT_ID, 0);
        mProduct = DatabaseManager.get(getActivity()).getProduct(productId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_product_details, container, false);

        mPicture = view.findViewById(R.id.productDetails_Picture);
        mTitle = view.findViewById(R.id.productDetails_Title);
        mDate = view.findViewById(R.id.productDetails_Date);
        mLocation = view.findViewById(R.id.productDetails_Location);
        mPrice = view.findViewById(R.id.productDetails_Price);
        mDescription = view.findViewById(R.id.productDetails_Description);
        mStatus = view.findViewById(R.id.productDetails_Status);

        if (mProduct.getPictures().size() > 0)
            new ImageLoadTask(mProduct.getPictures().get(0).getUrl(), mPicture).execute();
        mTitle.setText(mProduct.getName());
        mDate.setText(mProduct.getDate().toString());
        mLocation.setText(mProduct.getOwner().getLocation());
        mPrice.setText(mProduct.getPrice() + " €");
        mDescription.setText(mProduct.getDescription());
        mStatus.setText(mProduct.getStatus());

        return view;
    }
}
