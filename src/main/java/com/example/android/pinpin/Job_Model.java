package com.example.android.pinpin;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

public class Job_Model implements Serializable {
    String uuid;
    String title;
    String type;
    LatLng location;
    Date date;
    String pay;
    String description;
    String requester;
    String acceptor;
    Optional<Date> completedDate;

    public Job_Model(String uuid,
                     String requesterID,
                     String title,
                     String type,
                     double lat,
                     double lon,
                     Date date,
                     String pay,
                     String description) {
        this.uuid = uuid;
        this.requester = requesterID;
        this.acceptor = "";
        this.title = title;
        this.type = type;
        this.location = new LatLng(lat, lon);
        this.date = date;
        this.pay = pay;
        this.description = description;
        completedDate = Optional.empty();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
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

    public void setCompletedDate(Date date) {
        completedDate = Optional.of(date);
    }

    public JSONObject getAsJSONObj() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("uuid", uuid);
            obj.put("title", title);
            obj.put("type", type);
            obj.put("pay", pay);
            obj.put("requester", requester);
            obj.put("description", description);
            obj.put("date", date);
            obj.put("lat", location.latitude);
            obj.put("long", location.longitude);
            obj.put("acceptor", acceptor);
            if (completedDate.isPresent()) obj.put("completed", completedDate.get());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return obj;
    }

    @Override
    public String toString() {
        return "Job_Model(uuid: " + uuid + ", title: " + title + ", requester: " + requester + ", acceptor: "
                + acceptor + ", type: " + type + ", pay: " + pay + ", date: " + date + ", location: " + location
                + ", description: " + description + ")";
    }

}
