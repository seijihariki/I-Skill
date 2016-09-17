package com.temporary.i_skill;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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

        // Try using token for login
        if(AuthenticationSingleton.getInstance(getApplicationContext()).hasToken())
        {
            try {
                authenticating = true;
                AuthenticationSingleton.getInstance(getApplicationContext()).checkToken(new StatusCallback() {
                    @Override
                    public void callback(String status) {
                        authenticating = false;
                        if (status.equals("OK")) toHome();
                        else Log.i(TAG, "Invalid token");
                    }
                });
            } catch (JSONException e) {
                Log.e(TAG, "Early auth failed.");
                authenticating = false;
            }
        }

        // Get views from layout
        user_view = (EditText) findViewById(R.id.user_text);
        pass_view = (EditText) findViewById(R.id.pass_text);

        user_view.setText("");
        pass_view.setText("");

        sign_in_button = (Button) findViewById(R.id.sign_in_button);


        final View.OnFocusChangeListener empty_listener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                EditText editView = (EditText) view;
                String text = editView.getText().toString();
                if (text.isEmpty() && hasFocus == false) {
                    editView.setError(getString(R.string.empty_field_err));
                }
            }
        };

        user_view.setOnFocusChangeListener(empty_listener);
        pass_view.setOnFocusChangeListener(empty_listener);

        new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                EditText editView = (EditText) view;
                String text = editView.getText().toString();

                if (!text.isEmpty()) {
                    editView.setError(null);
                }

                return false;
            }
        };


        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean can_authenticate = true;
                String username = user_view.getText().toString();
                String password = pass_view.getText().toString();

                // Verify if authentication is doable

                if(username.isEmpty()) {
                    can_authenticate = false;
                    user_view.setError(getString(R.string.empty_field_err));
                    Log.i(TAG, "Username field is empty.");
                }

                if(password.isEmpty()) {
                    can_authenticate = false;
                    pass_view.setError(getString(R.string.empty_field_err));
                    Log.i(TAG, "Password field is empty.");
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

                    try {
                        AuthenticationSingleton.getInstance(getApplicationContext()).authenticate(username, password, new StatusCallback() {
                            @Override
                            public void callback(String status) {
                                switch(status) {
                                    case "OK":
                                        toHome();
                                        break;
                                    case "WRONG":
                                        user_view.setError(getString(R.string.wrong_creds_err));
                                        pass_view.setError(getString(R.string.wrong_creds_err));
                                        break;
                                    case "MALFORMED":
                                        network_error_dialog.setMessage(getString(R.string.req_error_diag));
                                        network_error_dialog.show();
                                        Log.e(TAG, "Malformed JSON.");
                                        break;
                                    case "NETWORK":
                                        network_error_dialog.setMessage(getString(R.string.network_err_diag));
                                        network_error_dialog.show();
                                        Log.e(TAG, "Malformed JSON or Request error.");
                                        break;
                                    default:
                                        network_error_dialog.setMessage(getString(R.string.req_error_diag));
                                        network_error_dialog.show();
                                        Log.e(TAG, "Unknown status error: " + status);
                                        break;
                                }
                                authenticating = false;
                            }
                        });
                    } catch (JSONException e) {
                        network_error_dialog.setMessage(getString(R.string.req_error_diag));
                        network_error_dialog.show();
                        Log.e(TAG, "Error creating JSON object.");
                    }

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

    private void toHome() {
        Intent intent = new Intent(this, SummaryActivity.class);
        startActivity(intent);
    }
}
