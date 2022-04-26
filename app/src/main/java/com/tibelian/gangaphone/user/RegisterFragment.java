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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tibelian.gangaphone.MainActivity;
import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.product.ListProductActivity;

public class RegisterFragment extends Fragment {

    private EditText mUsernameInput;
    private EditText mPasswordInput;
    private EditText mPassword2Input;
    private EditText mEmailInput;
    private EditText mPhoneInput;
    private Button mRegisterBtn;
    private TextView mLoginBtn;
    private TextView mGuestBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form_register, container,false);

        //
        initMemberVariables(view);

        //
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Login", "create account");
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Login", "Check credentials");
                ((MainActivity) getActivity()).replaceFragment(new LoginFragment());
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
        mRegisterBtn = (Button) view.findViewById(R.id.user_register_confirm);
        mLoginBtn = (TextView) view.findViewById(R.id.user_register_go_login);
        mGuestBtn = (TextView) view.findViewById(R.id.user_register_go_guest);
        mUsernameInput = (EditText) view.findViewById(R.id.user_register_username);
        mPasswordInput = (EditText) view.findViewById(R.id.user_register_password);
        mPassword2Input = (EditText) view.findViewById(R.id.user_register_password2);
        mEmailInput = (EditText) view.findViewById(R.id.user_register_email);
        mPhoneInput = (EditText) view.findViewById(R.id.user_register_phone);
    }

}
