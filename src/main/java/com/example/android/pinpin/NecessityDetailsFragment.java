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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class NecessityDetailsFragment extends RequestFragment {

    private final static String TAG = "NecDetailsFragment";
    // private final static String BACK_STACK_TAG = "Base_Map_Activity_Root";
    private static String BACK_STACK_TAG;
    private MapsActivity activity;
    private MyApplication app = MyApplication.getMyApp();
    private User user;
    private Necessity_Model nec;
    private ImageButton backBtn;
    private AppCompatTextView typeTV;
    private AppCompatTextView dateTV;
    private AppCompatTextView descTV;
    private AppCompatTextView partnerTV;
    private AppCompatButton acceptBtn;
    private AppCompatButton editBtn;
    private AppCompatButton completeBtn;
    private AppCompatButton deleteBtn;

    public static NecessityDetailsFragment newInstance(Necessity_Model nec, String BACK_STACK_TAG) {
        NecessityDetailsFragment fragment = new NecessityDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("NEC", nec);
        bundle.putSerializable("BACKTAG", BACK_STACK_TAG);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        nec = (Necessity_Model) getArguments().getSerializable("NEC");
        BACK_STACK_TAG = (String) getArguments().getSerializable("BACKTAG");
        activity = (MapsActivity) getActivity();

        return inflater.inflate(
                R.layout.nec_details_page,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        user = app.getUser();
        typeTV = view.findViewById(R.id.nec_type_details);
        dateTV = view.findViewById(R.id.nec_date);
        descTV = view.findViewById(R.id.nec_desc);
        backBtn = view.findViewById(R.id.necDetails_back_button);
        acceptBtn = view.findViewById(R.id.accept_nec);
        editBtn = view.findViewById(R.id.edit_nec);
        completeBtn = view.findViewById(R.id.complete_nec);
        deleteBtn = view.findViewById(R.id.delete_nec);
        partnerTV = view.findViewById(R.id.nec_partner_desc);

        typeTV.setText(nec.type);
        DateFormat dateFormat = new SimpleDateFormat("M/dd/yyyy, h:mm a");
        String dateStr = dateFormat.format(nec.date);
        dateTV.setText(dateStr);
        descTV.setText(nec.description);

        if (nec.requester.equals(user.getUUID())) {
            acceptBtn.setVisibility(View.GONE);
            if (!nec.acceptor.equals("")) {
                Optional<User> accUser = app.getAllUsers().stream()
                        .filter(u -> (u.getUUID().equals(nec.acceptor))).findFirst();
                String partnerText = accUser.isPresent() ?
                        accUser.get().getName() + " accepted this necessity." :
                        nec.acceptor;
                partnerTV.setText(partnerText);
            } else {
                completeBtn.setVisibility(View.GONE);
                partnerTV.setText(R.string.no_nec_acceptor);
            }
        } else {
            if (!nec.acceptor.equals("")) {
                acceptBtn.setVisibility(View.GONE);
                if (!nec.acceptor.equals(user.getUUID())) {
                    completeBtn.setVisibility(View.GONE);
                }
            } else {
                completeBtn.setVisibility(View.GONE);
            }
            editBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
            Optional<User> reqUser = app.getAllUsers().stream()
                    .filter(u -> (u.getUUID().equals(nec.requester))).findFirst();
            String partnerText = reqUser.isPresent() ?
                    reqUser.get().getName() + " requested this necessity." :
                    nec.requester;
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
                Log.i(TAG, "Accept Nec button pressed");
                // TODO: Incorporate database stuff, and check to make sure user didn't accept own nec
                nec.setAcceptor(user.getUUID());
                app.addAcceptedNec(nec);
                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Edit Nec button pressed");
                RequestNecessityFragment fragment = RequestNecessityFragment.newInstance(nec, BACK_STACK_TAG);
                activity.getFragManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, "requestFrag")
                        .addToBackStack(BACK_STACK_TAG)
                        .commit();
            }
        });

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Complete nec button presssed");
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                String options[] = {"Complete Necessity", "Cancel"};

                builder.setTitle("Do you want to mark this necessity as completed?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,  int which) {
                        switch(which) {
                            case 0:
                                Log.i(TAG, "COMPLETE NEC CLICKED");

                                //TODO: mark nec as complete;
                                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                break;
                            case 1:
                                Log.i(TAG, "CANCEL COMPLETE NEC CLICKED");
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
                Log.i(TAG, "Delete Nec button pressed");
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                String options[] = {"Delete Necessity", "Cancel"};

                builder.setTitle("Do you really want to delete this necessity?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,  int which) {
                        switch(which) {
                            case 0:
                                Log.i(TAG, "DELETE NEC CLICKED");
                                app.removeNec(nec);
                               // activity.setPins();
                                activity.getFragManager().popBackStack(BACK_STACK_TAG,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                break;
                            case 1:
                                Log.i(TAG, "CANCEL DELETE NEC CLICKED");
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
        return nec.type;
    }

    @Override
    public void setLocation(LatLng loc) {
        nec.setLocation(loc);
    }
}
