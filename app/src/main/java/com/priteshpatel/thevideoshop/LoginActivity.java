/**
 * https://sourcey.com/beautiful-android-login-and-signup-screens-with-material-design/
 */

package com.priteshpatel.thevideoshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String LOGIN_URL = "https://thevideoshop.herokuapp.com/api/users/login?";
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private Handler mHandler;

    private EditText mUsernameText;
    private EditText mPasswordText;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mHandler = new Handler(Looper.getMainLooper());

        //Setup views
        mUsernameText = findViewById(R.id.input_username);
        mPasswordText = findViewById(R.id.input_password);
        mLoginButton = findViewById(R.id.btn_login);

        //Set up login button listener
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    /**
     * Attempt to login with input provided by user.
     */
    public void login() {
        //Validate user input.
        if (!validate()) {
            onLoginFailed();
            return;
        }
        mLoginButton.setEnabled(false);
        authenticate(); //Authenticate user.
    }


    /**
     * Authenticates user - passes user input to server and verifies user.
     * @return true if user successfully logs in, otherwise return false.
     */
    public void authenticate() {

        //Display authenticating message
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String username = mUsernameText.getText().toString();
        String password = mPasswordText.getText().toString();

        final HttpClient httpClient = (HttpClient)getApplication();
        OkHttpClient client = httpClient.getClient();

        RequestBody body = new FormBody.Builder().build(); //Create empty body - need to make a POST request with parameters.

        //Build POST request with username and password as headers
        Request request = new Request.Builder()
                .url(LOGIN_URL + "username=" + username +"&password=" + password)
                .post(body)
                .build();

        //Make ASYNC POST Request
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                //Parse JSON and Update UI
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (response.code() == 200) {
                            httpClient.setLoggedIn();
                            progressDialog.dismiss();
                            onLoginSuccess();
                        } else {
                            httpClient.logOut();
                            progressDialog.dismiss();
                            onLoginFailed();//Failed login. call appropriate method.
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    /**
     * Display Toast message on login success and finish activity.
     */
    public void onLoginSuccess() {
        Toast.makeText(getBaseContext(), "Successfully Logged In", Toast.LENGTH_LONG).show();
        mLoginButton.setEnabled(true);
        setResult(999);
        finish();
    }

    /**
     * Display Toast message on login failure.
     */
    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login Failed - Please check credentials", Toast.LENGTH_LONG).show();
        mLoginButton.setEnabled(true);
    }

    /**
     * Sanity check inputs. Ensure username and password fields are not empty.
     * @return true if form is correctly filled, else false.
     */
    public boolean validate() {
        boolean valid = true;

        String username = mUsernameText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (username.isEmpty()) {
            mUsernameText.setError("enter your username");
            valid = false;
        } else {
            mUsernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 1 || password.length() > 10) {
            mPasswordText.setError("between 1 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }

    /**
     * Close activity when back button is pressed on the action bar
     * @return true when activity is closed.
     */
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}



