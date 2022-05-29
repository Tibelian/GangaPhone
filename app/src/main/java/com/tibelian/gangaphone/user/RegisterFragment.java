package com.tibelian.gangaphone.user;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.tibelian.gangaphone.MainActivity;
import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.database.model.User;
import com.tibelian.gangaphone.product.ListProductActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RegisterFragment extends Fragment {

    private EditText mUsernameInput;
    private EditText mPasswordInput;
    private EditText mPassword2Input;
    private EditText mEmailInput;
    private EditText mPhoneInput;
    private Button mRegisterBtn;
    private TextView mLoginBtn;
    private TextView mGuestBtn;

    private final int REQ_LOCATION = 2;
    private User registered = new User();

    private LocationRequest locationRequest;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        getCurrentLocation();

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
                registered.setUsername(mUsernameInput.getText().toString());
                registered.setPassword(mPasswordInput.getText().toString());
                registered.setEmail(mEmailInput.getText().toString());
                registered.setPhone(mPhoneInput.getText().toString());

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
                ((MainActivity) getActivity()).replaceFragment(new LoginFragment());
            }
        });
        mGuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (isGPSEnabled()) {
                LocationServices.getFusedLocationProviderClient(getActivity())
                        .requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                LocationServices.getFusedLocationProviderClient(getActivity())
                                        .removeLocationUpdates(this);

                                if (locationResult != null && locationResult.getLocations().size() > 0) {
                                    int index = locationResult.getLocations().size() - 1;
                                    double latitude = locationResult.getLocations().get(index).getLatitude();
                                    double longitude = locationResult.getLocations().get(index).getLongitude();
                                    registered.setLocation(
                                            RegisterFragment.this.obtainCityName(latitude, longitude));

                                    Log.d("RegisterFragment", "current location: " + registered.getLocation());
                                }
                            }
                        }, Looper.getMainLooper());
            } else turnOnGPS();
        } else {
            requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        }

    }


    private void turnOnGPS() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(getContext(), "GPS is already turned on", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(getActivity(), REQ_LOCATION);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        if (locationManager == null)
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private String obtainCityName(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            // another option --> getAddressLine(0|1|2)
            // cityName == pos --> 0
            // stateName == pos --> 1
            // countryName == pos --> 2
            return addressList.get(0).getLocality();
        } catch (IOException e) {
            Log.e("RegisterFragment", "obtainCityName() error --> " + e);
        }
        return "unknown";
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_LOCATION && resultCode == Activity.RESULT_OK)
            getCurrentLocation();
    }

}
