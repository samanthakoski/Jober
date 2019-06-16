package com.example.android.pinpin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.UUID;

public class SignupFragment extends Fragment {

    private static final String TAG = "SignupFragment";
    private static final String BACK_STACK_TAG = "Login_Activity_Root";
    private MyApplication app;
    private LoginActivity activity;
    private AppCompatEditText nameET;
    private AppCompatEditText emailET;
    private AppCompatEditText passET;
    private AppCompatEditText confirmPassET;
    private AppCompatButton signupBtn;
    private ImageButton backBtn;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (LoginActivity) getActivity();
        app = MyApplication.getMyApp();

        return inflater.inflate(
                R.layout.signup_page,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //   super.onViewCreated(view, savedInstanceState);

        nameET = view.findViewById(R.id.signup_name_et);
        emailET = view.findViewById(R.id.signup_email_et);
        passET = view.findViewById(R.id.signup_password_et);
        confirmPassET = view.findViewById(R.id.signup_confirm_password_et);
        signupBtn = view.findViewById(R.id.signup_button);
        backBtn = view.findViewById(R.id.signup_back_button);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeNewUser();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Back button pressed");
                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private void makeNewUser() {
        if (emailET.getText().toString().isEmpty() || passET.getText().toString().isEmpty()) return;
        if (!passET.getText().toString().equals(confirmPassET.getText().toString())) {
            Toast.makeText(activity, "Passwords do not match.", Toast.LENGTH_LONG).show();
            return;
        }
        String name = nameET.getText().toString();
        String email = emailET.getText().toString();
        String pass = passET.getText().toString();
        if (app.getAllUsers().stream()
                .anyMatch(u -> u.getEmail().equals(email))) {
            Log.i(TAG, "User already exists with that email.");
            Toast.makeText(activity,
                    "A user already exists with this email.",
                    Toast.LENGTH_SHORT).show();
        } else {
            User user = new User(UUID.randomUUID().toString(), name, email, pass);
            Thread addUser = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                    JSONObject juser = user.getAsJSONObj();
                    String entry = "http://129.65.221.101/php/sendJoberUserData.php?user=" + juser.toString();
                    URL url = new URL(entry);
                    URLConnection conn = url.openConnection();
                    InputStream in = conn.getInputStream();
                    in.close();
                    } catch (Exception e) {
                        Log.i(TAG, "Something failed: " + e);

                    }
                }
            });
            addUser.start();
            Log.i(TAG, "User successfully created");
            activity.updateUI(user);
        }
    }

}

