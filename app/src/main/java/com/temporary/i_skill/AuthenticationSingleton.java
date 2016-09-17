package com.temporary.i_skill;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.StateSet;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticationSingleton {
    private static AuthenticationSingleton instance;
    private static Context context;

    private String jwt_token;

    private AuthenticationSingleton(Context context) {
        AuthenticationSingleton.context = context;
        loadCredentials();
    }

    public static synchronized AuthenticationSingleton getInstance(Context context) {
        if(instance == null) {
            instance = new AuthenticationSingleton(context);
        }
        return instance;
    }

    private void loadCredentials() {
        SharedPreferences  auth_token_prefs = context.getSharedPreferences(
            context.getString(R.string.auth_file_key), Context.MODE_PRIVATE);

        String jwt_token_key = context.getString(R.string.jwt_token_key);

        jwt_token = auth_token_prefs.getString(jwt_token_key, "");
    }

    private void saveCredentials() {
        SharedPreferences  auth_token_prefs = context.getSharedPreferences(
                context.getString(R.string.auth_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = auth_token_prefs.edit();
        String jwt_token_key = context.getString(R.string.jwt_token_key);

        editor.putString(jwt_token_key, jwt_token);
        editor.apply();
    }

    public void authenticate(String username, String password, final StatusCallback callback) throws JSONException {
        String url = context.getString(R.string.server_domain) + context.getString(R.string.server_login);

        JSONObject auth_data = new JSONObject();
        auth_data.put("username", username);
        auth_data.put("password", password);

        JsonObjectRequest request = new JsonObjectRequest
            (Request.Method.POST, url, auth_data, new Response.Listener<JSONObject>() {
                private static final String TAG = "Json Request Listener";
                @Override
                public void onResponse(JSONObject response) {
                    String status;
                    try {
                        status = response.getString("status");
                        if (status.equals("OK")) {
                            Log.i(TAG, "Status: OK");
                            jwt_token = response.getString("jwt_token");
                            saveCredentials();
                        }
                        callback.callback(status);
                    } catch (JSONException e) {
                        callback.callback("MALFORMED");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callback.callback("NETWORK");
                    error.printStackTrace();
                }
            });

        NetworkSingleton.getInstance(context).addRequest(request);
    }

    public boolean hasToken() {
        return !jwt_token.isEmpty();
    }

    public void checkToken(final StatusCallback callback) throws JSONException {
        String url = context.getString(R.string.server_domain) + context.getString(R.string.server_login);

        JSONObject auth_data = new JSONObject();
        auth_data.put("jwt_token", jwt_token);

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, url, auth_data, new Response.Listener<JSONObject>() {
                    private static final String TAG = "Json Request Listener";
                    @Override
                    public void onResponse(JSONObject response) {
                        String status;
                        try {
                            status = response.getString("status");
                            callback.callback(status);
                        } catch (JSONException e) {
                            callback.callback("MALFORMED");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.callback("NETWORK");
                    }
                });

        NetworkSingleton.getInstance(context).addRequest(request);
    }
}
