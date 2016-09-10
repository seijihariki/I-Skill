package com.temporary.i_skill;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private boolean authenticating;

    private EditText user_view;
    private EditText pass_view;
    private TextView create_account_label;
    private Button   sign_in_button;

    private AlertDialog network_error_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        authenticating = false;

        // Get views from layout
        user_view = (EditText) findViewById(R.id.user_text);
        pass_view = (EditText) findViewById(R.id.pass_text);

        sign_in_button = (Button) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean can_authenticate = true;
                String username = user_view.getText().toString();
                String password = pass_view.getText().toString();

                // Verify if authentication is doable

                if(username.isEmpty()) {
                    user_view.setError(getString(R.string.empty_user_err));
                    can_authenticate = false;
                } else {
                    user_view.setError(null);
                }

                if(password.isEmpty()) {
                    pass_view.setError(getString(R.string.empty_pass_err));
                    can_authenticate = false;
                } else {
                    pass_view.setError(null);
                }

                NetworkInfo net_info = NetworkSingleton.getInstance(getApplicationContext()).getNetworkInfo();

                // Show error if not connected
                if (net_info == null || !net_info.isConnected()) {
                    network_error_dialog.setMessage(getString(R.string.no_network_diag));
                    network_error_dialog.show();
                    can_authenticate = false;
                }

                if(can_authenticate) {
                    authenticating = true;

                    // Send data to server for authentication
                    String url = getString(R.string.server_domain) + "/app/login.php";

                    try {
                        JSONObject auth_data = new JSONObject();
                        auth_data.put("username", username);
                        auth_data.put("password", password);
                    } catch (JSONException e) {
                        network_error_dialog.setMessage(getString(R.string.req_error_diag));
                        network_error_dialog.show();
                        Log.e(TAG, "Couldn't send data to server. JSON error.");
                    }

                    JsonObjectRequest request = new JsonObjectRequest
                            (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                                private static final String TAG = "Json Request Listener";
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String status = response.getString("status");

                                        switch(status) {
                                            case "OK":
                                                break;
                                            case "WRONG":
                                                user_view.setError(getString(R.string.wrong_creds_err));
                                                pass_view.setError(getString(R.string.wrong_creds_err));
                                                break;
                                            default:
                                                network_error_dialog.setMessage(getString(R.string.req_error_diag));
                                                network_error_dialog.show();
                                                Log.e(TAG, "Received status error.");
                                                break;
                                        }
                                    } catch (JSONException e) {
                                        network_error_dialog.setMessage(getString(R.string.req_error_diag));
                                        network_error_dialog.show();
                                        Log.e(TAG, "Received malformed JSON.");
                                    }
                                    authenticating = false;
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    network_error_dialog.setMessage(getString(R.string.req_error_diag));
                                    network_error_dialog.show();
                                    Log.e(TAG, "Volley request failed. Stack trace: ");
                                    error.printStackTrace();
                                    authenticating = false;
                                }
                            });
                    NetworkSingleton.getInstance(getApplicationContext()).addRequest(request);
                } else {
                    authenticating = false;
                }
            }
        });

        create_account_label = (TextView) findViewById(R.id.create_account_label);
        create_account_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        network_error_dialog = new AlertDialog.Builder(this).create();
        network_error_dialog.setIcon(android.R.drawable.ic_dialog_alert);
        network_error_dialog.setTitle(getString(R.string.error));
        network_error_dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
    }
}
