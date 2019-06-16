package com.example.android.pinpin;

import org.json.JSONObject;

public class User {

    private String uuid;
    private String name;
    private String email;
    private String password;


    public User(String uuid,
                String name,
                String email,
                String password) {
        this.uuid = uuid;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getUUID() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getAsJSONObj() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("uuid", uuid);
            obj.put("name", name);
            obj.put("email", email);
            obj.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return obj;
    }
}
