package com.tibelian.gangaphone.user.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.product.ProductDetailsFragment;

public class ProductEditFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "product_id";
    private Product mProduct;

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

        View view = inflater.inflate(R.layout.fragment_product_edit, container, false);



        return view;
    }
}
