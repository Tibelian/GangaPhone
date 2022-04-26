package com.tibelian.gangaphone.product;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.R;

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

        // load dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        builder.setPositiveButton("APPLY FILTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // @todo apply filter
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setTitle("Custom search");
        dialog.show();

        // @todo bind xml elements
        initMemberVariables(view);

        return dialog;
    }


    private void initMemberVariables(View view) {


    }


}
