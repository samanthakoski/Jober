package com.example.android.pinpin;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class YourNecsAdapter extends ArrayAdapter<Necessity_Model> implements View.OnClickListener {

    private final static String TAG = "YourNecsAdapter";
    private final static String BACK_STACK_ROOT_TAG = "Your_Necs_Fragment_Root";
    private List<Necessity_Model> necs;
    private int lastPosition = -1;
    private MapsActivity activity;
    Context context;

    private static class ViewHolder {
        RelativeLayout item;
        TextView type;
        TextView desc;
        TextView date;
    }

    public YourNecsAdapter(List<Necessity_Model> necs, Context context, MapsActivity activity) {
        super(context, R.layout.nec_list_item, necs);
        this.necs = necs;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Necessity_Model nec = getItem(position);
        // TODO: Send to Job Description page
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Necessity_Model nec = getItem(position);
        ViewHolder viewHolder;

        final View result;

        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.nec_list_item, parent, false);
        viewHolder.type = convertView.findViewById(R.id.nec_type);
        viewHolder.date = convertView.findViewById(R.id.nec_date);
        viewHolder.desc = convertView.findViewById(R.id.nec_desc);
        viewHolder.item = convertView.findViewById(R.id.list_nec_item);
        result = convertView;
        convertView.setTag(viewHolder);

        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;
        viewHolder.type.setText(nec.type);
        viewHolder.desc.setText(nec.description);
        DateFormat dateFormat = new SimpleDateFormat("M/dd/yyyy, h:mm a");
        String dateStr = dateFormat.format(nec.date);
        viewHolder.date.setText(dateStr);
        viewHolder.item.setOnClickListener(this);
        viewHolder.item.setTag(position);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, nec.type + " selected");
                activity.getFragManager().beginTransaction()
                        .replace(R.id.content_frame,
                                NecessityDetailsFragment.newInstance(nec, BACK_STACK_ROOT_TAG))
                        .addToBackStack(BACK_STACK_ROOT_TAG).commit();
            }
        });

        return convertView;
    }

    public void setActivity(MapsActivity activity) {
        this.activity = activity;
    }
}
