package com.temporary.i_skill;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkSingleton {
    private static NetworkSingleton instance;
    private static Context context;

    private ConnectivityManager conn_manager;

    private RequestQueue request_queue;

    private NetworkSingleton(Context context) {
        NetworkSingleton.context = context;
        request_queue = getRequestQueue();
    }

    public static synchronized NetworkSingleton getInstance(Context context) {
        if(instance == null) {
            instance = new NetworkSingleton(context);
        }
        return instance;
    }

    public ConnectivityManager getConnectivityManager() {
        if(conn_manager == null) {
            conn_manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return conn_manager;
    }

    public NetworkInfo getNetworkInfo() {
        return getConnectivityManager().getActiveNetworkInfo();
    }

    public RequestQueue getRequestQueue() {
        if(request_queue == null) {
            request_queue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return request_queue;
    }

    public <T> void addRequest(Request<T> req) {
        getRequestQueue().add(req);
    }
}
