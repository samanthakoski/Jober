package com.example.android.pinpin;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class YourJobsAdapter extends ArrayAdapter<Job_Model> implements View.OnClickListener {

    private final static String TAG = "YourJobsAdapter";
    private final static String BACK_STACK_ROOT_TAG = "Your_Job_Fragment_Root";
    private List<Job_Model> jobs;
    private int lastPosition = -1;
    Context context;
    private MapsActivity activity;

    private static class ViewHolder {
        RelativeLayout item;
        TextView title;
        TextView type;
        TextView pay;
        TextView desc;
        TextView date;
    }

    public YourJobsAdapter(List<Job_Model> jobs, Context context, MapsActivity activity) {
        super(context, R.layout.job_list_item, jobs);
        this.jobs = jobs;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Job_Model job = getItem(position);
        // TODO: Send to Job Description page
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Job_Model job = getItem(position);
        ViewHolder viewHolder;

        final View result;

       // if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
           // Log.i(TAG, "activity: " + activity);
            convertView = inflater.inflate(R.layout.job_list_item, parent, false);
            viewHolder.type = convertView.findViewById(R.id.job_type);
            viewHolder.title = convertView.findViewById(R.id.job_title);
            viewHolder.date = convertView.findViewById(R.id.job_date);
            viewHolder.pay = convertView.findViewById(R.id.job_pay);
            viewHolder.desc = convertView.findViewById(R.id.job_desc);
            viewHolder.item = convertView.findViewById(R.id.list_item_job);
          //  viewHolder.mapFrag = convertView.findViewById(R.id.job_map);
           // viewHolder.mapFrag = convertView.findViewById(R.id.job_map);
            result = convertView;
            convertView.setTag(viewHolder);
       // } //else {
           // viewHolder = (ViewHolder) convertView.getTag();
           // result = convertView;
       // }

        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;
        viewHolder.title.setText(job.title);
        viewHolder.type.setText(job.type);
        viewHolder.desc.setText(job.description);
        String payStr = "$" + job.pay;
        if (!payStr.contains(".")) {
            payStr += ".00";
        }
        viewHolder.pay.setText(payStr);
        DateFormat dateFormat = new SimpleDateFormat("M/dd/yyyy, h:mm a");
        String dateStr = dateFormat.format(job.date);
        viewHolder.date.setText(dateStr);
        viewHolder.item.setOnClickListener(this);
        viewHolder.item.setTag(position);

        final String title = job.getTitle();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, title + " selected");
                activity.getFragManager().beginTransaction()
                        .replace(R.id.content_frame,
                                JobDetailsFragment.newInstance(job, BACK_STACK_ROOT_TAG))
                        .addToBackStack(BACK_STACK_ROOT_TAG).commit();
                /*
                fragment.setSelectedJob(job);
                activity.getFragManager().beginTransaction()
                        .replace(R.id.your_jobs_content_frame, new JobDetailsFragment())
                        .addToBackStack(BACK_STACK_ROOT_TAG)
                        .commit();
                        */
            }
        });

        return convertView;
    }

    public void setActivity(MapsActivity activity) {
        Log.i(TAG, "SETTING ACTIVITY FOR ADAPTER");
        this.activity = activity;
    }

}
