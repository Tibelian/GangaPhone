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
import com.tibelian.gangaphone.database.model.User;
import com.tibelian.gangaphone.product.ListProductActivity;

import java.io.IOException;

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

                if (!mPasswordInput.getText().toString().equals(mPassword2Input.getText().toString())) {
                    Toast.makeText(getActivity(),
                            "The passwords don't match", Toast.LENGTH_SHORT).show();
                    return;
                }
                User registered = new User();
                registered.setUsername(mUsernameInput.getText().toString());
                registered.setPassword(mPasswordInput.getText().toString());
                registered.setEmail(mEmailInput.getText().toString());
                registered.setPhone(mPhoneInput.getText().toString());

                // @todo obtain lcoation using gps
                // if no permission the use ip location
                registered.setLocation("unknown");

                int id = -1;
                try { id = new RestApi().createUser(registered); }
                catch (IOException e) {
                    Log.e("RegisterFragment", "createUser --> " + e);
                }
                if (id != -1) {
                    registered.setId(id);
                    Session.get().setUser(registered);
                    Session.get().setLoggedIn(true);
                    startActivity(new Intent(getContext(), ListProductActivity.class));
                    Log.d("Register", "user created successfully");
                } else {
                    Toast.makeText(getActivity(),
                            "Couldn't create the account", Toast.LENGTH_SHORT).show();
                }
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
