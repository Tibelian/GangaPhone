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

/**
 * Sign in form
 */
public class LoginFragment extends Fragment {

    // the member variables
    // these are the objects
    // bind to the xml elements
    private Button mSignIn;
    private EditText mUsernameInput;
    private EditText mPasswordInput;
    private TextView mRegisterBtn;
    private TextView mGuestBtn;

    /**
     * This overridden method is used to generate
     * all the visual elements from the xml layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // declare the main view object using the layout
        View view = inflater.inflate(R.layout.fragment_form_login, container,false);

        // initialize all the elements
        initMemberVariables(view);

        // add events on the buttons
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // when siginIn button is pressed
                // then search on the user and the password
                // on the database
                User loggedIn = null;
                try {
                    loggedIn = new RestApi().findUserByLogin(
                            mUsernameInput.getText().toString(), mPasswordInput.getText().toString());
                } catch (IOException e) {
                    Log.e("LoginFragment", "error -> " + e);
                }

                // if login didn't failed create session
                // and show the default products list
                // else show error message
                if (loggedIn != null && loggedIn.getId() != 0)
                {
                    Session.get().setUser(loggedIn);
                    Session.get().setLoggedIn(true);
                    startActivity(new Intent(getContext(), ListProductActivity.class));
                }
                else {
                    Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // register button opens the create account fragment form
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).replaceFragment(new RegisterFragment());
            }
        });

        // guest button shows the default products list
        mGuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ListProductActivity.class));
            }
        });

        return view;
    }

    /**
     * assign xml elements to java objects
     * @param view
     */
    private void initMemberVariables(View view) {
        mSignIn = (Button) view.findViewById(R.id.user_login_confirm);
        mUsernameInput = (EditText) view.findViewById(R.id.user_login_username);
        mPasswordInput = (EditText) view.findViewById(R.id.user_login_password);
        mRegisterBtn = (TextView) view.findViewById(R.id.user_login_go_register);
        mGuestBtn = (TextView) view.findViewById(R.id.user_login_go_guest);
    }

}
