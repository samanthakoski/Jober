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

public class UpdateName extends ProfileFragment {

    private static final String TAG = "UpdateNameFragment";
    private static final String BACK_STACK_TAG = "Profile_Fragment_Root";
    private MyApplication application;
    private MapsActivity activity;
    private String name;
    private EditText name_edit_text;
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

        if (application != null) {
            user = application.getUser();
            name = user.getName();
            Log.i(TAG, "Application found");
        } else {
            Log.d(TAG, "Application is null");
        }

        activity = (MapsActivity) getActivity();
        return inflater.inflate(
                R.layout.update_name,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
       // super.onViewCreated(view, savedInstanceState);
        user = application.getUser();
        if (user == null) Log.i(TAG, "user is null");
        name_edit_text = view.findViewById(R.id.name_edit_text);
        exit_button = view.findViewById(R.id.exit_edit_name);
        clear_text = view.findViewById(R.id.clear_text);
        update_button = view.findViewById(R.id.update_button);
        clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_edit_text.setText("");
            }
        });
        if (user.getName().isEmpty()) {
            name_edit_text.setText(R.string.user_name);
        } else {
            name_edit_text.setText(user.getName());
        }
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
                name = name_edit_text.getText().toString();
                if (name.length() > 0) {
                    user.setName(name);
                    application.updateUser(user);
                    activity.getFragManager().popBackStack(BACK_STACK_TAG,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    Toast.makeText(getActivity(),
                            "Name must be at least one character",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
