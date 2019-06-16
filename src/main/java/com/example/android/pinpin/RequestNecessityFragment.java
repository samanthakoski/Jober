package com.example.android.pinpin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.UUID;


public class RequestNecessityFragment extends RequestFragment {

    private static final String TAG = "RequestNecessityFrag";
    private static final String BACK_STACK_TAG = "Request_Necessity_Frag";
    private static String BACK_STACK_ROOT_TAG;
    private static boolean editNec;
    private Necessity_Model theNec;
    private MyApplication app;
    private MapsActivity activity;
    private ImageButton exitBtn;
    private AppCompatButton setLocBtn;
    private AppCompatButton postNecBtn;
    private AppCompatEditText description;
    private LatLng location;
    private Spinner necTypeSpinner;
    private String necType = "";

    public static RequestNecessityFragment newInstance(Necessity_Model nec,
                                                       String BACK_STACK_ROOT_TAG) {
        RequestNecessityFragment fragment = new RequestNecessityFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("NEC", nec);
        bundle.putSerializable("BACKTAGROOT", BACK_STACK_ROOT_TAG);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        theNec = (Necessity_Model) getArguments().getSerializable("NEC");
        BACK_STACK_ROOT_TAG = (String) getArguments().getSerializable("BACKTAGROOT");
        editNec = (theNec != null);
        activity = (MapsActivity) getActivity();
        app = MyApplication.getMyApp();
        return inflater.inflate(
                R.layout.request_necessity,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //  user = app.getUser();
        exitBtn = view.findViewById(R.id.exit_button);
        description = view.findViewById(R.id.nec_desc_et);
        setLocBtn = view.findViewById(R.id.set_loc_button_nec);
        postNecBtn = view.findViewById(R.id.post_necessity);
        // TODO: Disable post job button till location set

        if (location == null) {
            Log.i(TAG, "loc is null");
            postNecBtn.setEnabled(false);
        }

        necTypeSpinner = view.findViewById(R.id.nec_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.nec_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        necTypeSpinner.setAdapter(adapter);

        necTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int pos, long id) {
                Log.i(TAG, "Nec type selected");
                necType = adapterView.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "No nec type selected.");
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getFragManager().popBackStack();
            }
        });

        if (editNec) {
            setUpEditNec();
        }

        setLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editNec) {
                    EditLocationFragment editFrag = EditLocationFragment
                            .newInstance(theNec, BACK_STACK_TAG);
                    activity.getFragManager().beginTransaction()
                            .replace(R.id.content_frame, editFrag)
                            .addToBackStack(BACK_STACK_TAG)
                            .commit();
                } else {
                    activity.getFragManager().beginTransaction()
                            .replace(R.id.content_frame, new SetLocationFragment())
                            .addToBackStack(BACK_STACK_TAG)
                            .commit();
                }
            }
        });
        postNecBtn.setEnabled(true);
        postNecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (necType.isEmpty()) {
                    Toast.makeText(activity,
                            "Must choose a necessity type.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (location == null) {
                    Toast.makeText(activity,
                            "Must set a location.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                String desc;
                if (description.getText() == null) {
                    desc = "";
                } else {
                    desc = description.getText().toString();
                }
                if (editNec) {
                    app.updateNec(theNec, necType, desc, location);
                } else {
                   // String lat = String.valueOf(location.latitude);
                    //String lon = String.valueOf(location.longitude);
                    app.addNec(new Necessity_Model(UUID.randomUUID().toString(),
                            app.getUser().getUUID(),
                            "",
                            necType,
                            new Date(),
                            location.latitude,
                            location.longitude,
                            desc));
                }
                activity.setPins();
                activity.getFragManager().popBackStack(BACK_STACK_ROOT_TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    public void setLocation(LatLng loc) {
        location = loc;
        Log.i(TAG, "location set: " + loc);
        postNecBtn.setEnabled(true);
    }

    public void setUpEditNec() {
        int typePos = 0;
        switch(theNec.type) {
            case "Food":
                typePos = 0;
                break;
            case "Money":
                typePos = 1;
                break;
            case "FirstAid":
                typePos = 2;
                break;
            case "Ride":
                typePos = 3;
                break;
        }
        necTypeSpinner.setSelection(typePos);
        description.setText(theNec.description);
        //location = theNec.location;
        setLocBtn.setText(R.string.edit_loc);
        postNecBtn.setText(R.string.update_necessity);
    }

    public String getType() {
        return necType;
    }

    public String getBackStackTag() {
        return BACK_STACK_TAG;
    }

}
