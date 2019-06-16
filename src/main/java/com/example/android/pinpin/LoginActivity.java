package com.example.android.pinpin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Optional;

public class LoginActivity extends FragmentActivity {

    private AppCompatEditText email;
    private AppCompatEditText password;
    private AppCompatButton signInBtn;
    private AppCompatTextView signUpBtn;
    private static final String TAG = "LoginActivity";
    private static final String BACK_STACK_ROOT_TAG = "Login_Activity_Root";
    private MyApplication app;
    private FragmentManager fragManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        app = (MyApplication) getApplication();
        fragManager = getSupportFragmentManager();
        email = findViewById(R.id.login_email_et);
        password = findViewById(R.id.login_password_et);
        signInBtn = findViewById(R.id.signin_button);
        signUpBtn = findViewById(R.id.signup_tv);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SignupFragment();
                fragManager.beginTransaction()
                        .replace(R.id.login_content_frame, fragment)
                        .addToBackStack(BACK_STACK_ROOT_TAG)
                        .commit();
            }
        });
        Log.i(TAG, "Login Activity created.");
        User curUser = app.getUser();
        if (curUser != null) {
            Log.i(TAG, "Current user logged in: " + curUser.getEmail());
            updateUI(curUser);
        }
    }


    public void updateUI(User user) {
        if (user == null) return;
        app.setUser(user);
        fragManager.popBackStack();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void validate() {
        if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) return;
        String typedEmail = email.getText().toString();
        String typedPassword = password.getText().toString();
        Optional<User> dbUser = app.getAllUsers().stream()
                .filter(u -> (u.getEmail().equals(typedEmail)
                        && u.getPassword().equals(typedPassword)))
                .findFirst();
        if (dbUser.isPresent()) {
            Log.i(TAG, "signInWithEmail:success");
            updateUI(dbUser.get());
        } else {
            Log.i(TAG, "signInWithEmail:failure");
            Log.i(TAG, "typedEmail: " + typedEmail);
            Log.i(TAG, "typedPassword: " + typedPassword);
            Log.i(TAG, "app.allusers: ");
            for (User u : app.getAllUsers()) {
                Log.i(TAG, u.toString());
            }
            Toast.makeText(LoginActivity.this,
                    "Authentication failed:\n" + "No user found.",
                    Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    public FragmentManager getFragManager() {
        return fragManager;
    }

}

