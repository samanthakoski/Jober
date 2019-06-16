package com.example.android.pinpin;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class Necessity_Model implements Serializable {
    public static final String TAG = "Necessity_Model";
    String uuid;
    String type;
    LatLng location;
    Date date;
    String description;
    String requester;
    String acceptor;

    public Necessity_Model(String uuid,
                           String requester,
                           String acceptor,
                           String type,
                           Date date,
                           double lat,
                           double lon,
                           String description) {
        this.uuid = uuid;
        this.requester = requester;
        this.acceptor = acceptor;
        this.type = type;
        this.location = new LatLng(lat, lon);
        this.date = date;
        this.description = description;
    }

    public String getUUID() {
        return uuid;
    }

    public String getRequester() {
        return requester;
    }

    public String getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(String acceptor) {
        this.acceptor = acceptor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JSONObject getAsJSONObj() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("uuid", uuid);
            obj.put("requester", requester);
            obj.put("acceptor", acceptor);
            obj.put("type", type);
            obj.put("date", date);
            obj.put("lat", location.latitude);
            obj.put("long", location.longitude);
            obj.put("description", description);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return obj;
    }

    @Override
    public String toString() {
        return "Necessity_Model(uuid: " + uuid + ", requester: " + requester + ", acceptor: "
                + acceptor + ", type: " + type + ", date: " + date + ", location: " + location
                + ", description: " + description + ")";
    }
}

