package com.example.android.pinpin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class UpdateEmail extends ProfileFragment {

    private static final String TAG = "UpdateNameFragment";
    private static final String BACK_STACK_TAG = "Profile_Fragment_Root";
    private MyApplication application;
    private MapsActivity activity;
    private String email;
    private EditText email_edit_text;
    private ImageView exit_button;
    private ImageButton clear_text;
    private Button update_button;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "Update Fragment created");
        application = MyApplication.getMyApp();
        user = application.getUser();
        activity = (MapsActivity) getActivity();
        return inflater.inflate(
                R.layout.update_email,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      //  super.onViewCreated(view, savedInstanceState);
        email = user.getEmail();
        email_edit_text = view.findViewById(R.id.email_edit_text);
        exit_button = view.findViewById(R.id.exit_edit_email);
        clear_text = view.findViewById(R.id.clear_text);
        update_button = view.findViewById(R.id.update_email_button);
        clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email_edit_text.setText("");
            }
        });
        email_edit_text.setText(email);
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });


        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = email_edit_text.getText().toString();
                if (email.contains("@") && email.contains(".")) {
                    // TODO: make sure no other user using this email before changing it
                    user.setEmail(email);
                    application.updateUser(user);
                    activity.getFragManager().popBackStack(BACK_STACK_TAG,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    Toast.makeText(getActivity(),
                            "Email not valid",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
