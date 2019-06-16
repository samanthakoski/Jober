package com.example.android.pinpin;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MyApplication extends Application {

    private static final String TAG = "MY_APP";
    public static final String CHANNEL_1 = "nearbyPin";
    public static final String USER_TAG = "USER";
    static final String KEY_CAMERA_POSITION = "camera_position";
    static final String KEY_LOCATION = "location";
    private static MyApplication myApp;
    private MapsActivity observer;
    private List<User> allUsers;
    private List<Job_Model> allJobs;
    private List<Job_Model> completedJobs;
    private List<Necessity_Model> allNecs;
    private User user;
    private Location lastKnownLoc = null;
    private Boolean locPermGranted;
    private final LatLng defaultLoc = new LatLng(35.2828, -120.6596);
    private final float defaultZoom = 15.0f;

    // user database stuff
    final Handler userHandler = new Handler();
    Runnable userRunnable = new Runnable() {
        @Override
        public void run() {
            System.out.println("NEW USER CYCLE");
            // Read and Send new users in new thread
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URLConnection ccc = new URL("http://129.65.221.101/php/getJoberData.php").openConnection();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(ccc.getInputStream()));
                        StringBuilder lines = new StringBuilder();
                        for (String line; (line = reader.readLine()) != null;) {
                            lines.append(line);
                        }
                        String input = lines.toString();
                        Gson g = new GsonBuilder()
                                .setDateFormat("EEE MMM dd HH:mm:ss z yyyy")
                                .create();
                        Gson gson = new Gson();
                        JSONObject obj = new JSONObject(input);
                        JSONArray users = obj.getJSONArray("users");
                     //   Log.i(TAG, "users length: "  + users.length());
                        JsonParser parser = new JsonParser();
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);
                         //   Log.i(TAG, "json obj user: " + user);
                            JsonObject gUser = (JsonObject) parser.parse(user.toString());
                            User fromJ = gson.fromJson(gUser.toString(), User.class);
                          //  Log.i(TAG, "gson user: " + fromJ.toString());
                            if (allUsers.stream().noneMatch(u -> u.getUUID().equals(fromJ.getUUID()))) {
                             //   Log.i(TAG, "adding user to allUsers");
                                allUsers.add(fromJ);
                            //    Log.i(TAG, "all users length: " + allUsers.size());
                            }
                        }
                        JSONArray jobs = obj.getJSONArray("jobs");
                        //Log.i(TAG, "jobs length: "  + jobs.length());
                        for (int i = 0; i < jobs.length(); i++) {
                            JSONObject job = jobs.getJSONObject(i);
                            JsonObject gJob = (JsonObject) parser.parse(job.toString());
                            Job_Model fromJ = g.fromJson(gJob.toString(), Job_Model.class);
                            fromJ.setLocation(new LatLng(gJob.get("lat").getAsDouble(), gJob.get("long").getAsDouble()));
                            if (job.has("completed")) {
                                Log.i(TAG, "FROM J WITH COMPLETED FIELD: " + job.get("completed"));
                                Object a = job.get("completed");
                                fromJ.completedDate = Optional.of(new Date(a.toString()));
                                Log.i(TAG, "new date from completed string: " + new Date(a.toString()));
                                completedJobs.add(fromJ);
                                continue;
                            } else {
                                fromJ.completedDate = Optional.empty();
                            }
                            if (allJobs.stream().noneMatch(j -> j.getUUID().equals(fromJ.getUUID()))) {
                                allJobs.add(fromJ);
                            } else if (allJobs.stream().anyMatch(j -> j.getUUID().equals(fromJ.getUUID()))) {
                                Optional<Job_Model> outdatedJob = allJobs.stream()
                                        .filter(j -> j.getUUID().equals(fromJ.getUUID()))
                                        .findFirst();
                                if (outdatedJob.isPresent() && !outdatedJob.get().toString().equals(fromJ.toString())) {
                                    allJobs.remove(outdatedJob.get());
                                    allJobs.add(fromJ);
                                }
                            }
                        }
                        JSONArray necs = obj.getJSONArray("necs");
                      //  Log.i(TAG, "necs length: "  + necs.length());
                        for (int i = 0; i < necs.length(); i++) {
                            JSONObject nec = necs.getJSONObject(i);
                            JsonObject gNec = (JsonObject) parser.parse(nec.toString());
                            Necessity_Model fromJ = g.fromJson(gNec.toString(), Necessity_Model.class);
                            fromJ.setLocation(new LatLng(gNec.get("lat").getAsDouble(), gNec.get("long").getAsDouble()));
                            if (allNecs.stream().noneMatch(n -> n.getUUID().equals(fromJ.getUUID()))) {
                                allNecs.add(fromJ);
                            } else if (allNecs.stream().anyMatch(n -> n.getUUID().equals(fromJ.getUUID()))) {
                                Optional<Necessity_Model> outdatedNec = allNecs.stream()
                                        .filter(n -> n.getUUID().equals(fromJ.getUUID()))
                                        .findFirst();
                                if (outdatedNec.isPresent() && !outdatedNec.get().toString().equals(fromJ.toString())) {
                                    allNecs.remove(outdatedNec.get());
                                    allNecs.add(fromJ);
                                }
                            }
                        }
                        notifyObserver();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, e.getMessage());
                    }
                }
            });

            thread.start();
            userHandler.postDelayed(this, 30000); // Update every 30 seconds
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        myApp = this;
        observer = null;
        createNotificationChannels();
        allJobs = new ArrayList<>();
        allUsers = new ArrayList<>();
        allNecs = new ArrayList<>();
        completedJobs = new ArrayList<>();
        userHandler.postDelayed(userRunnable, 0);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1,
                    "Nearby Pin",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("Pins are nearby");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel1);
            }
        }
    }

    public static MyApplication getMyApp() {
        return myApp;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LatLng getDefaultLoc() {
        return defaultLoc;
    }

    public float getDefaultZoom() {
        return defaultZoom;
    }

    public Location getLastKnownLoc() {
        return lastKnownLoc;
    }

    public void setLastKnownLoc(Location lastKnownLoc) {
        this.lastKnownLoc = lastKnownLoc;
    }

    public List<Job_Model> getAllJobs() {
        return allJobs;
    }

    public List<Necessity_Model> getAllNecs() {
        return allNecs;
    }

    public List<Job_Model> getUsersJobs() {
        return allJobs.stream().filter(j -> j.requester.equals(user.getUUID()))
                .collect(Collectors.toList());
    }

    public List<Necessity_Model> getUserNecs() {
        return allNecs.stream().filter(n -> n.requester.equals(user.getUUID()))
                .collect(Collectors.toList());
    }

    public void addJob(Job_Model job) {
        // Add Job to database
        final String entry = "http://129.65.221.101/php/sendJoberJobData.php?job=" + job.getAsJSONObj().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void addNec(Necessity_Model nec) {
        // TODO: Use database for jobs
        final String entry = "http://129.65.221.101/php/sendJoberNecData.php?nec=" + nec.getAsJSONObj().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public List<Job_Model> getAcceptedJobs() {
        return allJobs.stream().filter(j -> j.acceptor.equals(user.getUUID()))
                .collect(Collectors.toList());
    }

    public List<Necessity_Model> getAcceptedNecs() {
        return allNecs.stream().filter(n -> n.acceptor.equals(user.getUUID()))
                .collect(Collectors.toList());
    }

    public List<Job_Model> getCompletedJobs() {
        return completedJobs.stream().filter(j ->
                (j.acceptor.equals(user.getUUID()) || j.requester.equals(user.getUUID())))
                .collect(Collectors.toList());
    }

    public void addAcceptedJob(Job_Model job) {
        final String entry = "http://129.65.221.101/php/updateJoberJobData.php?job=" + job.getAsJSONObj().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void addAcceptedNec(Necessity_Model nec) {
        final String entry = "http://129.65.221.101/php/updateJoberNecData.php?nec=" + nec.getAsJSONObj().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void updateJob(Job_Model job,
                          String title,
                          String type,
                          LatLng loc,
                          String pay,
                          String desc) {
        job.setTitle(title);
        job.setType(type);
        job.setLocation(loc);
        job.setPay(pay);
        job.setDescription(desc);
        final String entry = "http://129.65.221.101/php/updateJoberJobData.php?job=" + job.getAsJSONObj().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void updateNec(Necessity_Model nec,
                          String type,
                          String desc,
                          LatLng loc) {
        nec.setType(type);
        nec.setDescription(desc);
        nec.setLocation(loc);
        final String entry = "http://129.65.221.101/php/updateJoberNecData.php?nec=" + nec.getAsJSONObj().toString();
        Log.i(TAG, "SENDING THIS ENTRY: " + entry);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void removeJob(Job_Model job) {
        final String entry = "http://129.65.221.101/php/removeJoberJobData.php?job=" + job.getAsJSONObj().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void removeNec(Necessity_Model nec) {
        final String entry = "http://129.65.221.101/php/removeJoberNecData.php?nec=" + nec.getAsJSONObj().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void completeJob(Job_Model job) {
        JSONObject jJson = job.getAsJSONObj();
        try {
            jJson.put("completed", new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String entry = "http://129.65.221.101/php/updateJoberJobData.php?job=" + jJson.toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    public void updateUser(User u) {
        final String entry = "http://129.65.221.101/php/updateJoberUserData.php?user=" + u.getAsJSONObj().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL send = new URL(entry);
                    URLConnection connection = send.openConnection();
                    InputStream in = connection.getInputStream();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public List<User> getAllUsers() {
        return allUsers;
    }

    public void setObserver(MapsActivity activity) {
        this.observer = activity;
    }

    private void notifyObserver() {
        if (observer != null) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    // Clear all markers on the map first
                    observer.setPins();
                }
            };
            mainHandler.post(myRunnable);
        }
    }
}

