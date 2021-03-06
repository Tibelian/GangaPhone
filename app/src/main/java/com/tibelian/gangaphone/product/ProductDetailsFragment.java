package com.tibelian.gangaphone.product;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.api.ImageLoadTask;
import com.tibelian.gangaphone.database.model.Product;
import com.tibelian.gangaphone.messenger.ChatActivity;
import com.tibelian.gangaphone.messenger.ChatListActivity;

import java.io.IOException;

/**
 * Generate the details fragment
 */
public class ProductDetailsFragment extends Fragment {

    // const to manage intents data
    private static final String ARG_PRODUCT_ID = "product_id";

    // current product
    private Product mProduct;

    // member variables - xml elements
    private ImageView mPicture;
    private TextView mTitle;
    private TextView mDate;
    private TextView mLocation;
    private TextView mPrice;
    private TextView mDescription;
    private TextView mPhone;
    private TextView mEmail;
    private TextView mStatus;
    private Button mContactSeller;
    private Button mShare;

    // manage images slide
    private int currentImg = 0;
    private TextView mPrevImg;
    private TextView mNextImg;

    // instantiate the details page
    public static ProductDetailsFragment newInstance(int productId) {
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * On init
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check id to search on database
        int productId = getArguments().getInt(ARG_PRODUCT_ID, 0);

        // new thread to obtain product from database
        new Thread(new Runnable() {
            @Override
            public void run() {

                try { mProduct = new RestApi().findProduct(productId); }
                catch (IOException e) {}

                // set data after rest api finishes
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bindData();
                    }
                });

            }
        }).start();

    }

    /**
     * create main view from layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // main view
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);

        // init xml elements
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
        mEmail = view.findViewById(R.id.productDetails_Email);
        mShare = view.findViewById(R.id.productDetails_Share);

        return view;
    }

    /**
     * now we will attach data to the visual elements
     * each time we call this method
     */
    private void bindData()
    {
        // load pictures
        if (mProduct.getPictures().size() > 0)
            updateCurrentPicture();

        // bind data
        mTitle.setText(mProduct.getName());
        mDate.setText(mProduct.getDate().toString());
        mLocation.setText(mProduct.getOwner().getLocation());
        mPrice.setText(mProduct.getPrice() + " ???");
        mDescription.setText(mProduct.getDescription());
        mPhone.setText(mProduct.getOwner().getPhone());
        mEmail.setText(mProduct.getOwner().getEmail());

        // translate status
        String status = "unknown";
        switch(mProduct.getStatus()) {
            case "broken":     status = getString(R.string.status_broken);     break;
            case "new":        status = getString(R.string.status_new);        break;
            case "scratched":  status = getString(R.string.status_scratched);  break;
        }
        mStatus.setText(status);

        // load previous image if exits
        mPrevImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentImg == 0) return;
                currentImg--;
                updateCurrentPicture();
            }
        });

        // load next image if exits
        mNextImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentImg == mProduct.getPictures().size() -1) return;
                currentImg++;
                updateCurrentPicture();
            }
        });

        // open messenger on click the button
        mContactSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Session.get().isLoggedIn() == false)
                    Toast.makeText(getActivity(),
                            "MUST LOGIN TO USE THIS OPTION", Toast.LENGTH_LONG).show();
                else
                    startActivity(ChatActivity.newIntent(
                            getActivity(), mProduct.getOwner().getId()));
            }
        });

        // call phone
        mPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mProduct.getOwner().getPhone()));
                startActivity(intent);
            }
        });

        // send email
        mEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + mProduct.getOwner().getEmail()));
                intent.putExtra(Intent.EXTRA_SUBJECT, mProduct.getName());
                startActivity(intent);
            }
        });

        // send text
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getReport());
                intent = Intent.createChooser(intent, getString(R.string.send_report));
                startActivity(intent);
            }
        });

        // the logged in user cant contact himself
        if (mProduct.getOwner().getId() == Session.get().getUser().getId())
            mContactSeller.setVisibility(View.INVISIBLE);
    }


    /**
     * Create text from product's data
     * @return String
     */
    private String getReport() {

        // translate status
        String status = "unknown";
        switch(mProduct.getStatus()) {
            case "broken":     status = getString(R.string.status_broken);     break;
            case "new":        status = getString(R.string.status_new);        break;
            case "scratched":  status = getString(R.string.status_scratched);  break;
        }
        // generate report
        String content = "";
        content += getString(R.string.report_intro) + "\n";
        content += getString(R.string.productName) + ": " + mProduct.getName() + "\n";
        content += getString(R.string.price) + ": " + mProduct.getPrice() + "\n";
        content += getString(R.string.location) + ": " + mProduct.getOwner().getLocation() + "\n";
        content += getString(R.string.status) + ": " + status + "\n";
        content += getString(R.string.phone) + ": " + mProduct.getOwner().getPhone() + "\n";
        content += getString(R.string.email) + ": " + mProduct.getOwner().getEmail() + "\n";
        content += getString(R.string.publishedDate) + ": " + mProduct.getDate() + "\n";
        content += getString(R.string.description) + ": " + mProduct.getDescription() + "\n";
        content += getString(R.string.report_end);
        return content;
    }

    /**
     * show the selected picture
     */
    private void updateCurrentPicture() {
        mPicture.setImageResource(R.drawable.loading_image);
        if (mProduct.getPictures().get(currentImg) != null)
            new ImageLoadTask(mProduct.getPictures().get(currentImg).getUrl(), mPicture).execute();
        checkNavPictures();
    }

    /**
     * visual effects to the next and prev buttons
     */
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
