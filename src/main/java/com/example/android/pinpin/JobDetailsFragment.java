package com.example.android.pinpin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class JobDetailsFragment extends RequestFragment {

    private final static String TAG = "JobDetailsFragment";
   // private final static String BACK_STACK_TAG = "Base_Map_Activity_Root";
    private static String BACK_STACK_TAG;
    private MapsActivity activity;
    private MyApplication app = MyApplication.getMyApp();
    private User user;
    private Job_Model job;
    private ImageButton backBtn;
    private AppCompatTextView typeTV;
    private AppCompatTextView titleTV;
    private AppCompatTextView dateTV;
    private AppCompatTextView descTV;
    private AppCompatTextView payTV;
    private AppCompatTextView partnerTV;
    private AppCompatButton acceptBtn;
    private AppCompatButton editBtn;
    private AppCompatButton completeBtn;
    private AppCompatButton deleteBtn;

    public static JobDetailsFragment newInstance(Job_Model job, String BACK_STACK_TAG) {
        JobDetailsFragment fragment = new JobDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("JOB", job);
        bundle.putSerializable("BACKTAG", BACK_STACK_TAG);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        job = (Job_Model) getArguments().getSerializable("JOB");
        BACK_STACK_TAG = (String) getArguments().getSerializable("BACKTAG");
        activity = (MapsActivity) getActivity();

        return inflater.inflate(
                R.layout.job_details_page,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        user = app.getUser();
        typeTV = view.findViewById(R.id.job_type);
        titleTV = view.findViewById(R.id.job_title);
        dateTV = view.findViewById(R.id.job_date);
        descTV = view.findViewById(R.id.job_desc);
        payTV = view.findViewById(R.id.job_pay);
        backBtn = view.findViewById(R.id.jobDetails_back_button);
        acceptBtn = view.findViewById(R.id.accept_job);
        editBtn = view.findViewById(R.id.edit_job);
        completeBtn = view.findViewById(R.id.complete_job);
        deleteBtn = view.findViewById(R.id.delete_job);
        partnerTV = view.findViewById(R.id.job_partner_desc);

        typeTV.setText(job.type);
        titleTV.setText(job.title);
        DateFormat dateFormat = new SimpleDateFormat("M/dd/yyyy, h:mm a");
        String dateStr = dateFormat.format(job.date);
        dateTV.setText(dateStr);
        String payStr = "$" + job.pay;
        if (!payStr.contains(".")) {
            payStr += ".00";
        }
        payTV.setText(payStr);
        descTV.setText(job.description);

        if (job.requester.equals(user.getUUID())) {
            acceptBtn.setVisibility(View.GONE);
            if (!job.acceptor.equals("")) {
                Optional<User> accUser = app.getAllUsers().stream()
                        .filter(u -> (u.getUUID().equals(job.acceptor))).findFirst();
                String partnerText = accUser.isPresent() ?
                        accUser.get().getName() + " accepted this job." :
                        job.acceptor;
                partnerTV.setText(partnerText);
            } else {
                completeBtn.setVisibility(View.GONE);
                partnerTV.setText(R.string.no_job_acceptor);
            }
        } else {
            if (!job.acceptor.equals("")) {
                acceptBtn.setVisibility(View.GONE);
                if (!job.acceptor.equals(user.getUUID())) {
                    completeBtn.setVisibility(View.GONE);
                }
            } else {
                completeBtn.setVisibility(View.GONE);
            }
            editBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
            Optional<User> reqUser = app.getAllUsers().stream()
                    .filter(u -> (u.getUUID().equals(job.requester))).findFirst();
            String partnerText = reqUser.isPresent() ?
                    reqUser.get().getName() + " requested this job." :
                    job.requester;
            partnerTV.setText(partnerText);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Back button pressed");
                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Accept Job button pressed");
                // TODO: Incorporate database stuff
                job.setAcceptor(user.getUUID());
                app.addAcceptedJob(job);
                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,  "Edit job button pressed");
                RequestJobFragment fragment = RequestJobFragment.newInstance(job, BACK_STACK_TAG);
                activity.getFragManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, "requestFrag")
                        .addToBackStack(BACK_STACK_TAG)
                        .commit();
            }
        });

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Complete job button presssed");
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                String options[] = {"Complete Job", "Cancel"};

                builder.setTitle("Do you want to mark this job as completed?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,  int which) {
                        switch(which) {
                            case 0:
                                Log.i(TAG, "COMPLETE JOB CLICKED");

                                //TODO: mark job as complete
                                app.completeJob(job);
                               // app.removeJob(job);
                                // activity.setPins();
                                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                break;
                            case 1:
                                Log.i(TAG, "CANCEL COMPLETE JOB CLICKED");
                                dialog.dismiss();
                                break;
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Delete Job button pressed");
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                String options[] = {"Delete Job", "Cancel"};

                builder.setTitle("Do you really want to delete this job?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,  int which) {
                        switch(which) {
                            case 0:
                                Log.i(TAG, "DELETE JOB CLICKED");
                                app.removeJob(job);
                               // activity.setPins();
                                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                break;
                            case 1:
                                Log.i(TAG, "CANCEL DELETE JOB CLICKED");
                                dialog.dismiss();
                                break;
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    public String getBackStackTag() {
        return BACK_STACK_TAG;
    }

    public String getType() {
        return job.type;
    }

    @Override
    public void setLocation(LatLng loc) {
        job.setLocation(loc);
    }
}
