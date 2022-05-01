package com.tibelian.gangaphone.product;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.database.CurrentFilter;

public class FilterFragment extends DialogFragment {

    private CheckBox
            mStatusNewCheck,mStatusScratchedCheck, mStatusBrokenCheck;

    private EditText
            mKeywordInput, mLocationInput, mPriceMinInput, mPriceMaxInput;

    private RadioButton
            mOrderPriceAsc, mOrderPriceDesc, mOrderDateDesc, mOrderDateAsc, mOrderFeatured;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // load layout
        View view = LayoutInflater.from(getActivity()
        ).inflate(R.layout.fragment_filter,null);

        // bind xml elements
        initMemberVariables(view);

        // load filter data
        loadCurrentFilter();

        // load dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        // apply and cancel buttons
        initButtonEvents(builder);

        // show modal
        AlertDialog dialog = builder.create();
        dialog.setTitle("Custom search");
        dialog.show();

        return dialog;
    }

    private void initMemberVariables(View view) {
        mStatusNewCheck = view.findViewById(R.id.filterStatusNew);
        mStatusScratchedCheck = view.findViewById(R.id.filterStatusScratched);
        mStatusBrokenCheck = view.findViewById(R.id.filterStatusBroken);
        mKeywordInput = view.findViewById(R.id.filterKeyword);
        mLocationInput = view.findViewById(R.id.filterLocation);
        mPriceMinInput = view.findViewById(R.id.filterPriceMin);
        mPriceMaxInput = view.findViewById(R.id.filterPriceMax);
        mOrderPriceAsc = view.findViewById(R.id.filterOrderPriceAsc);
        mOrderPriceDesc = view.findViewById(R.id.filterOrderPriceDesc);
        mOrderDateDesc = view.findViewById(R.id.filterOrderDateDesc);
        mOrderDateAsc = view.findViewById(R.id.filterOrderDateAsc);
        mOrderFeatured = view.findViewById(R.id.filterOrderFeatured);
    }

    private void initButtonEvents(AlertDialog.Builder builder) {

        builder.setPositiveButton("APPLY FILTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                applyFilter();
                Bundle result = new Bundle();
                result.putBoolean(ListProductActivity.DIALOG_FILTER_APPLIED, true);
                getParentFragmentManager().setFragmentResult(ListProductActivity.DIALOG_FILTER, result);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
            }
        });

    }

    private void loadCurrentFilter() {
        mKeywordInput.setText(CurrentFilter.keyword);
        mStatusNewCheck.setChecked(CurrentFilter.status.contains("new"));
        mStatusBrokenCheck.setChecked(CurrentFilter.status.contains("broken"));
        mStatusScratchedCheck.setChecked(CurrentFilter.status.contains("scratched"));
        mLocationInput.setText(CurrentFilter.location);
        mPriceMinInput.setText(CurrentFilter.minPrice != -1 ? CurrentFilter.minPrice + "" : "");
        mPriceMaxInput.setText(CurrentFilter.maxPrice != -1 ? CurrentFilter.maxPrice + "" : "");
        mOrderPriceAsc.setChecked(CurrentFilter.orderBy.equals("price.asc"));
        mOrderPriceDesc.setChecked(CurrentFilter.orderBy.equals("price.desc"));
        mOrderDateAsc.setChecked(CurrentFilter.orderBy.equals("date.asc"));
        mOrderDateDesc.setChecked(CurrentFilter.orderBy.equals("date.desc"));
        mOrderFeatured.setChecked(CurrentFilter.orderBy.equals("featured"));
    }

    private void applyFilter() {

        // apply no filter
        CurrentFilter.clear();

        // load status
        if (mStatusNewCheck.isChecked()) CurrentFilter.status.add("new");
        if (mStatusBrokenCheck.isChecked()) CurrentFilter.status.add("broken");
        if (mStatusScratchedCheck.isChecked()) CurrentFilter.status.add("scratched");

        // load keyword, location and prices
        CurrentFilter.keyword = mKeywordInput.getText().toString();
        CurrentFilter.location = mLocationInput.getText().toString();

        // load prices
        if (!mPriceMaxInput.getText().toString().equals(""))
            CurrentFilter.maxPrice = Double.parseDouble(mPriceMaxInput.getText().toString());
        if (!mPriceMinInput.getText().toString().equals(""))
            CurrentFilter.minPrice = Double.parseDouble(mPriceMinInput.getText().toString());

        Log.e("maxPrice", mPriceMaxInput.getText().toString());
        Log.e("minPrice", mPriceMinInput.getText().toString());

        // load sorting
        if (mOrderDateAsc.isChecked()) CurrentFilter.orderBy = "date.asc";
        if (mOrderDateDesc.isChecked()) CurrentFilter.orderBy = "date.desc";
        if (mOrderPriceAsc.isChecked()) CurrentFilter.orderBy = "price.asc";
        if (mOrderPriceDesc.isChecked()) CurrentFilter.orderBy = "price.desc";
        if (mOrderFeatured.isChecked()) CurrentFilter.orderBy = "featured";

    }

}
