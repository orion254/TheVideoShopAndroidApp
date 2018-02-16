package com.priteshpatel.thevideoshop;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private static final String REGISTER_URL = "https://thevideoshop.herokuapp.com/api/users/register";
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("application/x-www-form-urlencoded");

    private Handler mHandler;

    private EditText mUsernameText;
    private EditText mEmailText;
    private EditText mFirstNameText;
    private EditText mLastNameText;
    private EditText mPasswordText;
    private EditText mConfirmPasswordText;
    private Button mSignupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mHandler = new Handler(Looper.getMainLooper());

        //Initialise View fields
        mUsernameText = findViewById(R.id.input_username);
        mEmailText= findViewById(R.id.input_email);
        mFirstNameText = findViewById(R.id.input_fName);
        mLastNameText = findViewById(R.id.input_lName);
        mPasswordText= findViewById(R.id.input_password);
        mConfirmPasswordText = findViewById(R.id.input_password_confirm);
        mSignupButton= findViewById(R.id.btn_signup);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Display back button in action bar

        //Set up click listener for sign up button
        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });
    }

    /**
     * Process user sign up.
     */
    public void signup() {

        //Validate user input. Return if not valid.
        if (!validate()) {
            onSignupFailed();
            return;
        }

        mSignupButton.setEnabled(false);

        //Display appropriate loading message.
        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //Read user input
        String username = mUsernameText.getText().toString();
        String email = mEmailText.getText().toString();
        String firstName = mFirstNameText.getText().toString();
        String lastName = mLastNameText.getText().toString();
        String password = mPasswordText.getText().toString();

        //Get OkHTTP client instance from Application class
        final HttpClient httpClient = (HttpClient)getApplication();
        OkHttpClient client = httpClient.getClient();

        //Build body to be sent in request.
        RequestBody body = new FormBody.Builder()
                .add("username", username) //try addEncoded
                .add("email", email)
                .add("fName", firstName)
                .add("lName", lastName)
                .add("password", password)
                .build();

        //Build POST request
        Request request = new Request.Builder()
                .url(REGISTER_URL)
                .post(body)
                .build();

        //Make async POST request.
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
                            onSignupSuccess();
                        } else {
                            //Sign up unsuccessful. Display error message.
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                String error = jsonObject.getString("error");
                                Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getBaseContext(), "Failed to sign up. Try again.", Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getBaseContext(), "Failed to sign up. Try again.", Toast.LENGTH_LONG).show();
                            }
                            httpClient.logOut();
                            progressDialog.dismiss();
                            onSignupFailed();//Failed login. call appropriate method.
                        }
                    }
                });
            }
        });
    }

    /**
     *
     */
    public void onSignupSuccess() {
        Toast.makeText(getBaseContext(), "Signed up successfully and logged in", Toast.LENGTH_LONG).show();
        mSignupButton.setEnabled(true);
        setResult(999);
        finish();
    }

    public void onSignupFailed() {
        mSignupButton.setEnabled(true);
    }

    /**
     * Validates user input (registration form)
     * @return true if form is valid
     */
    public boolean validate() {
        boolean valid = true;

        String username = mUsernameText.getText().toString();
        String email = mEmailText.getText().toString();
        String fName = mFirstNameText.getText().toString();
        String password = mPasswordText.getText().toString();
        String confirmPassword = mConfirmPasswordText.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            mUsernameText.setError("at least 3 characters");
            valid = false;
        } else {
            mUsernameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.setError("enter a valid email address");
            valid = false;
        } else {
            mEmailText.setError(null);
        }

        if (fName.isEmpty()) {
            mFirstNameText.setError("first name required");
        } else {
            mFirstNameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        if (confirmPassword.isEmpty() || !confirmPassword.equals(password)) {
            mConfirmPasswordText.setError("passwords must match");
            valid = false;
        } else {
            mConfirmPasswordText.setError(null);
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
