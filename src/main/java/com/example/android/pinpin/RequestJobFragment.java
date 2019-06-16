package com.example.android.pinpin;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class RequestJobFragment extends RequestFragment {

    private static final String TAG = "RequestJobFragment";
    private static final String BACK_STACK_TAG = "Request_Job_Fragment";
    private static String BACK_STACK_ROOT_TAG;
    private static boolean editJob;
    private Job_Model theJob;
    private MyApplication app;
    private MapsActivity activity;
    private ImageButton exitBtn;
    private AppCompatEditText titleET;
    private AppCompatEditText payET;
    private AppCompatEditText descET;
    private AppCompatButton setLocBtn;
    private AppCompatButton postJobBtn;
    private LatLng location;
    private Spinner jobTypeSpinner;
    private String jobType = "";

    public static RequestJobFragment newInstance(Job_Model job, String BACK_STACK_ROOT_TAG) {
        RequestJobFragment fragment = new RequestJobFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("JOB", job);
        bundle.putSerializable("BACKTAGROOT", BACK_STACK_ROOT_TAG);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        theJob = (Job_Model) getArguments().getSerializable("JOB");
        BACK_STACK_ROOT_TAG = (String) getArguments().getSerializable("BACKTAGROOT");
        editJob = (theJob != null);
        activity = (MapsActivity) getActivity();
        app = MyApplication.getMyApp();
        return inflater.inflate(
                R.layout.request_job,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
      //  user = app.getUser();
        exitBtn = view.findViewById(R.id.exit_button);
        titleET = view.findViewById(R.id.title_et);
        payET = view.findViewById(R.id.pay_et);
        descET = view.findViewById(R.id.desc_et);
        setLocBtn = view.findViewById(R.id.set_loc_button);
        postJobBtn = view.findViewById(R.id.post_job);
        // TODO: Disable post job button till location set

        if (location == null) {
            Log.i(TAG, "loc is null");
            postJobBtn.setEnabled(false);
        }

        jobTypeSpinner = view.findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.job_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobTypeSpinner.setAdapter(adapter);

        jobTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int pos, long id) {
                Log.i(TAG, "Job type selected");
                jobType = adapterView.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "No job type selected.");
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getFragManager().popBackStack();
            }
        });

        if (editJob) {
            setUpEditJob();
        }

        setLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "POST JOB IS ENABLED: " + postJobBtn.isEnabled());
                if (editJob) {
                    EditLocationFragment editFrag = EditLocationFragment
                            .newInstance(theJob, BACK_STACK_TAG);
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
        postJobBtn.setEnabled(true);
        postJobBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (titleET.getText().toString().isEmpty()) {
                    Toast.makeText(activity,
                            "Must enter a job title.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (payET.getText().toString().isEmpty()) {
                    Toast.makeText(activity,
                            "Must enter job pay.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (descET.getText().toString().isEmpty()) {
                    Toast.makeText(activity,
                            "Must enter a job description.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (jobType.isEmpty()) {
                    Toast.makeText(activity,
                            "Must choose a job type.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (location == null) {
                    Toast.makeText(activity,
                            "Must set a location.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // TODO: write job to firebase
                if (editJob) {
                    Log.i(TAG, "sending app this location to update: " + location);
                    app.updateJob(theJob,
                            titleET.getText().toString(),
                            jobType,
                            location,
                            payET.getText().toString(),
                            descET.getText().toString());
                } else {
                    app.addJob(new Job_Model(UUID.randomUUID().toString(),
                            app.getUser().getUUID(),
                            titleET.getText().toString(),
                            jobType,
                            location.latitude,
                            location.longitude,
                            new Date(),
                            payET.getText().toString(),
                            descET.getText().toString()));
                }
                activity.setPins();
                activity.getFragManager().popBackStack(BACK_STACK_ROOT_TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

    }

    private void setUpEditJob() {
        titleET.setText(theJob.title);
        payET.setText(theJob.pay);
        int typePos = 0;
        switch(theJob.type) {
            case "Manual Labor":
                typePos = 0;
                break;
            case "Virtual":
                typePos = 1;
                break;
            case "Task":
                typePos = 2;
                break;
            case "Errand":
                typePos = 3;
                break;
        }
        jobTypeSpinner.setSelection(typePos);
        descET.setText(theJob.description);
        setLocBtn.setText(R.string.edit_loc);
        postJobBtn.setText(R.string.update_job);
        //location = theJob.location;
    }

    public void setLocation(LatLng loc) {
        location = loc;
        Log.i(TAG, "location set: " + loc);
        postJobBtn.setEnabled(true);
    }

    public String getType() {
        return jobType;
    }

    public String getBackStackTag() {
        return BACK_STACK_TAG;
    }

}
