package com.tibelian.gangaphone.user.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.database.model.ProductPicture;
import com.tibelian.gangaphone.product.ProductDetailsFragment;
import com.tibelian.gangaphone.product.ProductPagerActivity;
import com.tibelian.gangaphone.product.ProductsFragment;

import java.io.IOException;
import java.util.ArrayList;

public class ProductEditFragment extends Fragment {

    private static final int REQUEST_PHOTO = 1;
    private static final String ARG_PRODUCT_ID = "product_id";
    private boolean isNew = true;
    private Product mProduct;

    private EditText mNameInput;
    private EditText mPriceInput;
    private EditText mDescInput;
    private RadioButton mStatusNewCheck;
    private RadioButton mStatusScratchedCheck;
    private RadioButton mStatusBrokenCheck;
    private ImagesFragment mImagesFragment;
    private Button mUploadImgBtn;
    private Button mRemoveBtn;
    private Button mSaveBtn;
    private LinearLayout mTotalViews;
    private CheckBox mSold;

    private ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                mSaveBtn.performClick();
            } else {
                Toast.makeText(getActivity(),
                        "Can't save the product because we don't have permission", Toast.LENGTH_LONG).show();
            }
    });



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
        if (isNew) {
            mProduct = new Product();
            mProduct.setOwner(Session.get().getUser());
        }
        else {
            ArrayList<Product> sesProd = Session.get().getUser().getProducts();
            for(Product p:sesProd) {
                if (p.getId() == productId) {
                    mProduct = p;
                    break;
                }
            }
            if (mProduct == null) {
                Toast.makeText(getActivity(), "PRODUCT NOT FOUND ON USER'S SESSION", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
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
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PHOTO);
            }
        });

        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNew) new DatabaseManager().deleteProduct(mProduct);
                getActivity().finish();
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {

                    // ALL GOOD - we have permission
                    int pId = new DatabaseManager().saveProduct(mProduct, isNew);
                    if (pId != -1)
                        startActivity(ProductPagerActivity.newIntent(getActivity(), pId));
                    else
                        Toast.makeText(getActivity(), R.string.error_newProduct, Toast.LENGTH_LONG).show();

                } else {
                    requestPermissionLauncher.launch(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
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
        mTotalViews = v.findViewById(R.id.edit_pViewsContainer);
        mSold = v.findViewById(R.id.edit_pSold);

        // events on change values
        mNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mProduct.setName(charSequence.toString());
            }
        });
        mDescInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mProduct.setDescription(charSequence.toString());
            }
        });
        mPriceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    mProduct.setPrice(Float.parseFloat(charSequence.toString()));
                } catch (NumberFormatException ne) {}
            }
        });
        mStatusScratchedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mProduct.setStatus("scratched");
            }
        });
        mStatusNewCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mProduct.setStatus("new");
            }
        });
        mStatusBrokenCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mProduct.setStatus("broken");
            }
        });

        // load fragment
        FragmentManager fragmentManager = getParentFragmentManager();
        mImagesFragment = (ImagesFragment) fragmentManager.findFragmentById(R.id.edit_pImagesList);
        if(mImagesFragment == null) {
            mImagesFragment = new ImagesFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.edit_pImagesList, mImagesFragment)
                    .runOnCommit(new Runnable() {
                        @Override
                        public void run() {
                            mImagesFragment.loadImages(mProduct.getPictures());
                        }
                    })
                    .commit();
        }

        // append data if user's editing a product
        // product pictures load on the fragmentManager.runOnCommit()
        if (isNew == false) {
            mNameInput.setText(mProduct.getName());
            mPriceInput.setText(""+mProduct.getPrice());
            mDescInput.setText(mProduct.getDescription());
            mStatusNewCheck.setChecked(mProduct.getStatus().equals("new"));
            mStatusScratchedCheck.setChecked(mProduct.getStatus().equals("scratched"));
            mStatusBrokenCheck.setChecked(mProduct.getStatus().equals("broken"));
            // show info
            mTotalViews.setVisibility(View.VISIBLE);
            mSold.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PHOTO) {

            Uri imageUri = data.getData();

            ProductPicture pp = new ProductPicture();
            pp.setUri(imageUri);
            pp.setRealpath(getRealPathFromUri(imageUri));

            mProduct.getPictures().add(pp);
            mImagesFragment.loadImages(mProduct.getPictures());
        }
    }



    private String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null)  cursor.close();
        }
    }

}
