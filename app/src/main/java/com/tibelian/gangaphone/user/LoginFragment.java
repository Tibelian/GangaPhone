package com.tibelian.gangaphone.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.MainActivity;
import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.User;
import com.tibelian.gangaphone.product.ListProductActivity;

import java.io.IOException;

public class LoginFragment extends Fragment {

    private Button mSignIn;
    private EditText mUsernameInput;
    private EditText mPasswordInput;
    private TextView mRegisterBtn;
    private TextView mGuestBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form_login, container,false);

        //
        initMemberVariables(view);

        //
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("onclick sigin", "STARTED LOGIN");
                User loggedIn = null;
                try {
                    loggedIn = new RestApi().findUserByLogin(
                            mUsernameInput.getText().toString(), mPasswordInput.getText().toString());
                } catch (IOException e) {
                    Log.e("LoginFragment", "error -> " + e);
                }
                Log.e("onclick sigin", "ENDED LOGIN");

                if (loggedIn != null && loggedIn.getId() != 0) {
                    Session.get().setUser(loggedIn);
                    Session.get().setLoggedIn(true);
                    startActivity(new Intent(getContext(), ListProductActivity.class));

                } else {
                    Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Login", "Open register form");
                ((MainActivity) getActivity()).replaceFragment(new RegisterFragment());
            }
        });
        mGuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Login", "Go guest");
                startActivity(new Intent(getContext(), ListProductActivity.class));
            }
        });

        return view;
    }

    private void initMemberVariables(View view) {
        mSignIn = (Button) view.findViewById(R.id.user_login_confirm);
        mUsernameInput = (EditText) view.findViewById(R.id.user_login_username);
        mPasswordInput = (EditText) view.findViewById(R.id.user_login_password);
        mRegisterBtn = (TextView) view.findViewById(R.id.user_login_go_register);
        mGuestBtn = (TextView) view.findViewById(R.id.user_login_go_guest);
    }

}
