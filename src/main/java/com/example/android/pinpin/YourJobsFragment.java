package com.example.android.pinpin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import java.util.List;

public class YourJobsFragment extends Fragment {
    private final static String TAG = "YourJobListFragment";
    private final static String BACK_STACK_ROOT_TAG = "Your_Job_Fragment_Root";
    private ImageButton exitBtn;
    private ListView jobList;
    private List<Job_Model> accepted_jobs;
    private List<Job_Model> requested_jobs;
    private List<Job_Model> completed_jobs;
    private MyApplication app;
    private User user;
    private MapsActivity activity;
    private YourJobsAdapter adapter;
    private RelativeLayout requestedJobs;
    private View requestedLine;
    private RelativeLayout acceptedJobs;
    private View acceptedLine;
    private RelativeLayout completedJobs;
    private View completedLine;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        app = MyApplication.getMyApp();
        user = app.getUser();
        activity = (MapsActivity) getActivity();
        return inflater.inflate(
                R.layout.your_jobs_page,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exitBtn = view.findViewById(R.id.exit_yourJobs_button);
        jobList = view.findViewById(R.id.job_list);
        requestedJobs = view.findViewById(R.id.requested_jobs);
        requestedLine = view.findViewById(R.id.requested_line);
        acceptedJobs = view.findViewById(R.id.accepted_jobs);
        acceptedLine = view.findViewById(R.id.accepted_line);
        completedJobs = view.findViewById(R.id.completed_jobs);
        completedLine = view.findViewById(R.id.completed_line);
        requested_jobs = app.getUsersJobs();
        accepted_jobs = app.getAcceptedJobs();
        completed_jobs = app.getCompletedJobs();
        adapter = new YourJobsAdapter(requested_jobs, activity.getApplicationContext(), activity);
        jobList.setAdapter(adapter);
        adapter.setActivity(activity);

        requestedJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestedLine.setVisibility(View.VISIBLE);
                acceptedLine.setVisibility(View.INVISIBLE);
                completedLine.setVisibility(View.INVISIBLE);
                adapter = new YourJobsAdapter(requested_jobs, activity.getApplicationContext(), activity);
                jobList.setAdapter(adapter);
            }
        });

        acceptedJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestedLine.setVisibility(View.INVISIBLE);
                acceptedLine.setVisibility(View.VISIBLE);
                completedLine.setVisibility(View.INVISIBLE);
                adapter = new YourJobsAdapter(accepted_jobs, activity.getApplicationContext(), activity);
                jobList.setAdapter(adapter);
            }
        });

        completedJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestedLine.setVisibility(View.INVISIBLE);
                acceptedLine.setVisibility(View.INVISIBLE);
                completedLine.setVisibility(View.VISIBLE);
                adapter = new YourJobsAdapter(completed_jobs, activity.getApplicationContext(), activity);
                jobList.setAdapter(adapter);
            }
        });


        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getFragManager().popBackStack();
            }
        });


    }
}
