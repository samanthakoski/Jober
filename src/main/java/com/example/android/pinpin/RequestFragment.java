package com.example.android.pinpin;

import android.support.v4.app.Fragment;

import com.google.android.gms.maps.model.LatLng;

abstract public class RequestFragment extends Fragment {
    abstract public String getType();
    abstract public void setLocation(LatLng loc);
    abstract public String getBackStackTag();
}
