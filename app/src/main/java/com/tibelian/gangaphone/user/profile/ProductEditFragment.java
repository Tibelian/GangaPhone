package com.tibelian.gangaphone.user.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.product.ProductDetailsFragment;
import com.tibelian.gangaphone.product.ProductPagerActivity;
import com.tibelian.gangaphone.product.ProductsFragment;

import java.io.IOException;

public class ProductEditFragment extends Fragment {

    private static final int REQUEST_PHOTO = 1;
    private static final String ARG_PRODUCT_ID = "product_id";
    private boolean isNew = true;
    private Product mProduct;

    private EditText mNameInput;
    private EditText mPriceInput;
    private EditText mDescInput;
    private CheckBox mStatusNewCheck;
    private CheckBox mStatusScratchedCheck;
    private CheckBox mStatusBrokenCheck;
    private Fragment mImagesFragment;
    private Button mUploadImgBtn;
    private Button mRemoveBtn;
    private Button mSaveBtn;

    public static ProductEditFragment newInstance(int productId) {
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        ProductEditFragment fragment = new ProductEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int productId = getArguments().getInt(ARG_PRODUCT_ID, 0);
        isNew = (productId == 0);
        mProduct = DatabaseManager.get(getActivity()).getProduct(productId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_product_edit, container, false);

        // load elements
        initComponents(view);

        mUploadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // @todo add event new intent request image
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PHOTO);
            }
        });

        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNew) deleteProduct();
                getActivity().finish();
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pId = saveProduct();
                if (pId != -1)
                    startActivity(ProductPagerActivity.newIntent(getActivity(), pId));
                else
                    Toast.makeText(getActivity(), R.string.error_newProduct, Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private void initComponents(View v) {

        // bind xml elements
        mNameInput = v.findViewById(R.id.edit_pName);
        mPriceInput = v.findViewById(R.id.edit_pPrice);
        mDescInput = v.findViewById(R.id.edit_pDesc);
        mStatusNewCheck = v.findViewById(R.id.edit_pStatusNew);
        mStatusScratchedCheck = v.findViewById(R.id.edit_pStatusScratched);
        mStatusBrokenCheck = v.findViewById(R.id.edit_pStatusBroken);
        mUploadImgBtn = v.findViewById(R.id.edit_pImageUpload);
        mRemoveBtn = v.findViewById(R.id.edit_pDelete);
        mSaveBtn = v.findViewById(R.id.edit_pSave);

        // load fragment
        FragmentManager fragmentManager = getParentFragmentManager();
        mImagesFragment = fragmentManager.findFragmentById(R.id.edit_pImagesList);
        if(mImagesFragment == null) {
            mImagesFragment = new ImagesFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.edit_pImagesList, mImagesFragment)
                    .commit();
        }

    }

    private void deleteProduct() {
        // @todo delete current product and finish activity
    }

    private int saveProduct() {
        // @todo create product
        return -1;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        try{

            if (requestCode == REQUEST_PHOTO) {

                Uri imageSelected = data.getParcelableExtra(Intent.EXTRA_STREAM);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageSelected);

                Toast.makeText(getActivity(), "IMAGE: " + bitmap.toString(), Toast.LENGTH_SHORT).show();
                //
                //imageView.setImageBitmap(bitmap);

            }

        }
        catch (IOException e) {
            Log.e("onActivityResult", "IOException --> request photo");
        }
        catch (NullPointerException e) {
            Log.e("onActivityResult", "NullPointerException --> request photo");
        }

    }
}
