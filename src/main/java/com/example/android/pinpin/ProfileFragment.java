package com.example.android.pinpin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "PROFILE_FRAGMENT";
    private static final String BACK_STACK_TAG = "Profile_Fragment_Root";
    private MyApplication application;
    private MapsActivity activity;
    private RelativeLayout nameBox;
    private AppCompatTextView nameTV;
    private AppCompatTextView emailTV;
    private RelativeLayout emailBox;
    private ImageButton exitBtn;
    private TextInputLayout passTIL;
    private AppCompatEditText passET;
    private AppCompatButton signOutBtn;
    private User user;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "Profile Fragment created");
        Log.i(TAG, "ON CREATE VIEW");
        application = MyApplication.getMyApp();
        user = application.getUser();
        activity = (MapsActivity) getActivity();
        return inflater.inflate(
                R.layout.profile_page,
                container,
                false);
    }




    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "ON VIEW CREATED");
       // user = application.getUser();
        nameTV = view.findViewById(R.id.name_tv);
        nameBox = view.findViewById(R.id.name_box);
        nameTV.setText(user.getName());
        nameBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpdateName(nameTV.toString());
            }
        });
        emailTV = view.findViewById(R.id.email_tv);
        emailBox = view.findViewById(R.id.email_box);
        emailTV.setText(user.getEmail());
        emailBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpdateEmail(emailTV.toString());
            }
        });
        exitBtn = view.findViewById(R.id.prof_exit_button);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getFragManager().popBackStack();
            }
        });
        signOutBtn = view.findViewById(R.id.signout_button);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }


    private void startUpdateName(String name) {
        Fragment updateName = new UpdateName();
        activity.getFragManager().beginTransaction()
                .replace(R.id.content_frame, updateName)
                .addToBackStack(BACK_STACK_TAG)
                .commit();
    }

    private void startUpdateEmail(String email) {
        Fragment updateEmail = new UpdateEmail();
        activity.getFragManager().beginTransaction()
                .replace(R.id.content_frame, updateEmail)
                .addToBackStack(BACK_STACK_TAG)
                .commit();
    }

    private void signOut() {
        activity.finish();
    }

}