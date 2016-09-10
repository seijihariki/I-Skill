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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText user_view;
    private EditText pass_view;
    private TextView create_account_label;
    private Button   sign_in_button;
    private AuthenticationTask authenticate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authenticate = new AuthenticationTask(this);

        // Get views from layout
        user_view = (EditText) findViewById(R.id.user_text);
        pass_view = (EditText) findViewById(R.id.pass_text);

        sign_in_button = (Button) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticate.execute();
            }
        });

        create_account_label = (TextView) findViewById(R.id.create_account_label);
        create_account_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        setContentView(R.layout.activity_login_activity);
    }

    // AsyncTask handling authentication
    private class AuthenticationTask extends AsyncTask<Void, Void, Integer> {

        private Context context;

        private String user;
        private String pass;

        ConnectivityManager conn_manager;
        NetworkInfo         net_info;

        public AuthenticationTask (Context context) {
            this.context = context;
            conn_manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        protected void onPreExecute() {
            boolean can_authenticate = true;
            user = user_view.getText().toString();
            pass = pass_view.getText().toString();

            if(user.isEmpty()) {
                user_view.setError(getString(R.string.empty_user_err));
                can_authenticate = false;
            }

            if(pass.isEmpty()) {
                pass_view.setError(getString(R.string.empty_pass_err));
                can_authenticate = false;
            }

            // Check for internet connection
            net_info = conn_manager.getActiveNetworkInfo();

            // Show error if not connected
            if(net_info == null || !net_info.isConnected()) {
                AlertDialog network_error_dialog = new AlertDialog.Builder(context).create();
                network_error_dialog.setIcon(android.R.drawable.ic_dialog_alert);
                network_error_dialog.setTitle(getString(R.string.error));
                network_error_dialog.setMessage(getString(R.string.no_network_diag));
                network_error_dialog.setButton(0, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                network_error_dialog.show();
                can_authenticate = false;
            }

            // Stop task if can't authenticate
            if(!can_authenticate) {
                this.cancel(true);
            }
        }

        protected Integer doInBackground(Void... args) {
            return 0;
        }

        protected void onPostExecute(Integer result) {

        }
    }
}
