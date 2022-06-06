package com.tibelian.gangaphone.user.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.database.model.ProductPicture;
import com.tibelian.gangaphone.product.ProductPagerActivity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The product edition form
 */
public class ProductEditFragment extends Fragment {

    // control variables
    private static final int REQUEST_PHOTO = 1;
    private static final String ARG_PRODUCT_ID = "product_id";
    private boolean isNew = true;

    // the current product
    private Product mProduct;

    // member variables
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
    private TextView mTotalViewsCounter;
    private CheckBox mSold;

    // control variable of pictures to delete on the activity
    public ArrayList<Integer> picturesToDelete = new ArrayList<>();

    // callback on request permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                mSaveBtn.performClick();
            } else {
                Toast.makeText(getActivity(),
                        "Can't save the product because we don't have permission", Toast.LENGTH_LONG).show();
            }
    });

    /**
     * generate a ProductEditFragment
     * @param productId
     * @return
     */
    public static ProductEditFragment newInstance(int productId) {
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        ProductEditFragment fragment = new ProductEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * this loads the current product
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // obtain productId from args
        int productId = getArguments().getInt(ARG_PRODUCT_ID, 0);

        // check if its editing or creating a product
        isNew = (productId == 0);
        if (isNew) {
            // create new product object
            mProduct = new Product();
            mProduct.setOwner(Session.get().getUser());
        }
        else {
            // load product from user's session
            ArrayList<Product> sesProd = Session.get().getUser().getProducts();
            for(Product p:sesProd) {
                if (p.getId() == productId) {
                    mProduct = p;
                    break;
                }
            }
            // if product doesn't exist then show an error message
            // and exist from the current activity
            if (mProduct == null) {
                Toast.makeText(getActivity(), "PRODUCT NOT FOUND ON USER'S SESSION", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }

    /**
     * Generate the main view from the layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // obtain view from layout
        View view = inflater.inflate(R.layout.fragment_product_edit, container, false);

        // load elements
        initComponents(view);

        // set event on click upload button
        mUploadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // select picture from internal media
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PHOTO);
            }
        });

        // on click remove button
        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if is editing picture then delete it from the database
                if (!isNew) {
                    try {
                        boolean deleted = new RestApi().deleteProduct(mProduct.getId());
                        if (deleted)
                            Session.get().getUser().getProducts().remove(mProduct);
                        else
                            Toast.makeText(getActivity(), R.string.error_pRemove, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("ProductEditFragment", "mRemoveBtn.onClick error --> " + e);
                    }
                }
                // close activity
                getActivity().finish();
            }
        });

        // when save button is clicked
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // execute the save method
                saveCurrentProduct();
            }
        });

        return view;
    }

    /**
     * initialize elements from the main view
     * @param v
     */
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
        mTotalViewsCounter = v.findViewById(R.id.edit_pViews);
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
                if (b) mProduct.setStatus("scratched");
            }
        });
        mStatusNewCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) mProduct.setStatus("new");
            }
        });
        mStatusBrokenCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) mProduct.setStatus("broken");
            }
        });

        // load images fragment
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
            mSold.setChecked(mProduct.isSold());
            mTotalViewsCounter.setText(mProduct.getVisits()+"");
        }

    }

    /**
     * this method obtains an intent result
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // check if the pictures has loaded correctly
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PHOTO) {
            // obtain the uri
            Uri imageUri = data.getData();

            // bind the picture to the current product
            ProductPicture pp = new ProductPicture();
            pp.setUri(imageUri);
            pp.setRealpath(getRealPathFromUri(imageUri));
            mProduct.getPictures().add(pp);

            // update the pictures adapter
            mImagesFragment.loadImages(mProduct.getPictures());
        }
    }


    /**
     * Obtain the real path from an Uri object
     * @param contentUri
     * @return String
     */
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


    /**
     * Update the current product's changes
     */
    private void saveCurrentProduct() {
        // first check the permissions
        if (ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            // ALL GOOD - we have permission
            try {

                // validate product's data
                if (!checkValidProduct()) return;

                // if is new product then insert it into the database
                // and update the session
                if (isNew) {
                    mProduct = new RestApi().createProduct(mProduct);
                    Session.get().getUser().getProducts().add(mProduct);
                }

                // if editing product then do this:
                else {
                    // delete pictures from temp object
                    int i = 0;
                    for(ProductPicture pp:mProduct.getPictures()) {
                        if (picturesToDelete.contains(pp.getId()))
                            mProduct.getPictures().remove(i);
                        else i++;
                    }
                    picturesToDelete.clear();
                    // and finally update database
                    mProduct = new RestApi().updateProduct(mProduct);
                }
            } catch (IOException io) {
                Log.e("ProductEditFragment", "mSaveBtn onclick --> " + io);
            }

            // the last step is to check if changes
            // has been made correctly
            if (mProduct.getId() != 0)
                startActivity(ProductPagerActivity.newIntent(getActivity(), mProduct.getId(), true));
            else
                Toast.makeText(getActivity(), R.string.error_newProduct, Toast.LENGTH_LONG).show();

        } else {
            // if not then ask for them
            requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * Validate the current product's data
     * @return boolean
     */
    private boolean checkValidProduct() {
        boolean ok = true;
        int lastError = -1;

        // check name
        if (mProduct.getName() == null || mProduct.getName().length() < 5) {
            ok = false; lastError = R.string.req_pName;
        }

        // check price
        try {
            if (mProduct.getPrice() < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            ok = false; lastError = R.string.req_pPriceFormat;
        }

        // check description
        if (mProduct.getDescription() == null || mProduct.getDescription().length() < 10) {
            ok = false; lastError = R.string.req_pDesc;
        }

        // check status
        if (mProduct.getStatus() == null || mProduct.getStatus().length() < 1) {
            ok = false; lastError = R.string.req_pStatus;
        }

        // check pictures on new product
        if (isNew && mProduct.getPictures().size() == 0) {
            ok = false; lastError = R.string.req_pPictures;
        }

        // check pictures on editing product
        if (!isNew && mProduct.getPictures().size() == picturesToDelete.size()) {
            ok = false; lastError = R.string.req_pPictures;
        }

        // show last error
        if (lastError != -1)
            Toast.makeText(getActivity(), lastError, Toast.LENGTH_SHORT).show();

        return ok;
    }


}
