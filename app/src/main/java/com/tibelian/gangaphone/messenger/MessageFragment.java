package com.tibelian.gangaphone.messenger;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.User;

public class MessageFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private User mUser;

    public static MessageFragment newInstance(int userId) {
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int userId = getArguments().getInt(ARG_USER_ID, 0);
        mUser = DatabaseManager.get(getActivity()).getUser(userId);
    }

}
