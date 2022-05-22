package com.tibelian.gangaphone.product;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.async.ImageLoadTask;
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.messenger.ChatActivity;
import com.tibelian.gangaphone.messenger.ChatListActivity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProductDetailsFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "product_id";
    private Product mProduct;

    private ImageView mPicture;
    private TextView mTitle;
    private TextView mDate;
    private TextView mLocation;
    private TextView mPrice;
    private TextView mDescription;
    private TextView mPhone;
    private TextView mStatus;
    private Button mContactSeller;

    private int currentImg = 0;
    private TextView mPrevImg;
    private TextView mNextImg;

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
        mProduct = new DatabaseManager().getProduct(productId);
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
        mPhone = view.findViewById(R.id.productDetails_Phone);
        mPrevImg = view.findViewById(R.id.productDetails_PrevImg);
        mNextImg = view.findViewById(R.id.productDetails_NextImg);
        mContactSeller = view.findViewById(R.id.productDetails_ContactSeller);

        if (mProduct.getPictures().size() > 0)
            updateCurrentPicture();
        mTitle.setText(mProduct.getName());
        mDate.setText(mProduct.getDate().toString());
        mLocation.setText(mProduct.getOwner().getLocation());
        mPrice.setText(mProduct.getPrice() + " €");
        mDescription.setText(mProduct.getDescription());
        mStatus.setText(mProduct.getStatus());
        mPhone.setText(mProduct.getOwner().getPhone());

        mPrevImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentImg == 0) return;
                currentImg--;
                updateCurrentPicture();
            }
        });

        mNextImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentImg == mProduct.getPictures().size() -1) return;
                currentImg++;
                updateCurrentPicture();
            }
        });

        mContactSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Session.get().isLoggedIn() == false) {
                    Toast.makeText(getActivity(),
                            "MUST LOGIN TO USE THIS OPTION", Toast.LENGTH_LONG).show();
                } else {
                    startActivity(ChatActivity.newIntent(
                            getActivity(), mProduct.getOwner().getUsername()));
                }
            }
        });

        return view;
    }


    private void updateCurrentPicture() {
        mPicture.setImageResource(R.drawable.loading_image);
        if (mProduct.getPictures().get(currentImg) != null)
            new ImageLoadTask(mProduct.getPictures().get(currentImg).getUrl(), mPicture).execute();
        checkNavPictures();
    }

    private void checkNavPictures() {
        if (currentImg == 0)
            mPrevImg.setTextColor(Color.GRAY);
        else
            mPrevImg.setTextColor(Color.WHITE);

        if (currentImg == mProduct.getPictures().size() - 1)
            mNextImg.setTextColor(Color.GRAY);
        else
            mNextImg.setTextColor(Color.WHITE);
    }

}
