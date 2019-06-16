package com.example.android.pinpin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import java.util.List;

public class YourNecsFragment extends Fragment {
    private final static String TAG = "YourNecListFragment";
    private final static String BACK_STACK_ROOT_TAG = "Your_Necs_Fragment_Root";
    private ImageButton exitBtn;
    private ListView necList;
    private List<Necessity_Model> accepted_necs;
    private List<Necessity_Model> requested_necs;
    private MyApplication app;
    private User user;
    private MapsActivity activity;
    private YourNecsAdapter adapter;
    private RelativeLayout requestedNecs;
    private View requestedLine;
    private RelativeLayout acceptedNecs;
    private View acceptedLine;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        app = MyApplication.getMyApp();
        user = app.getUser();
        activity = (MapsActivity) getActivity();
        return inflater.inflate(
                R.layout.your_necs_page,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exitBtn = view.findViewById(R.id.exit_yourNecs_button);
        necList = view.findViewById(R.id.nec_list);
        requestedNecs = view.findViewById(R.id.requested_necs);
        requestedLine = view.findViewById(R.id.requested_line);
        acceptedNecs = view.findViewById(R.id.accepted_necs);
        acceptedLine = view.findViewById(R.id.accepted_line);
        requested_necs = app.getUserNecs();
        accepted_necs = app.getAcceptedNecs();
        adapter = new YourNecsAdapter(requested_necs, activity.getApplicationContext(), activity);
        adapter.setActivity(activity);
        necList.setAdapter(adapter);

        requestedNecs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestedLine.setVisibility(View.VISIBLE);
                acceptedLine.setVisibility(View.INVISIBLE);
                adapter = new YourNecsAdapter(requested_necs, activity.getApplicationContext(), activity);
                necList.setAdapter(adapter);
            }
        });

        acceptedNecs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestedLine.setVisibility(View.INVISIBLE);
                acceptedLine.setVisibility(View.VISIBLE);
                adapter = new YourNecsAdapter(accepted_necs, activity.getApplicationContext(), activity);
                necList.setAdapter(adapter);
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
