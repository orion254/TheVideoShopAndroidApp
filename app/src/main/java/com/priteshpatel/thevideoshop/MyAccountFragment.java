package com.priteshpatel.thevideoshop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MyAccountFragment extends Fragment {

    private final String ACCOUNT_URL = "https://thevideoshop.herokuapp.com/api/users/me";

    private Handler mHandler;
    private RelativeLayout mAccountInformation;
    private TextView mFragmentTitle;
    private TextView mUsername;
    private TextView mEmail;
    private TextView mFirstName;
    private TextView mLastName;
    private TextView mRecentPurchases;


    //Method returns view for this fragment.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mHandler = new Handler(Looper.getMainLooper());
        View rootView = inflater.inflate(R.layout.fragment_my_account, container, false);

        //Prepare views
        mAccountInformation = rootView.findViewById(R.id.relative_data);
        mFragmentTitle = rootView.findViewById(R.id.fragment_title);
        mUsername = rootView.findViewById(R.id.username_value);
        mEmail = rootView.findViewById(R.id.email_value);
        mFirstName = rootView.findViewById(R.id.first_name_value);
        mLastName = rootView.findViewById(R.id.last_name_value);
        mRecentPurchases = rootView.findViewById(R.id.recent_purchases);

        //Hide views - may be used later
        mFragmentTitle.setVisibility(View.GONE);
        mAccountInformation.setVisibility(View.INVISIBLE);

        displayUserInformation();
        return rootView;
    }


    /**
     * Download User information and display it in UI
     */
    public void displayUserInformation() {

        //Get OkHTTP client instance from Application class
        HttpClient httpClient = (HttpClient) getActivity().getApplication();
        OkHttpClient client = httpClient.getClient();

        //Build GET request
        Request request = new Request.Builder()
                .url(ACCOUNT_URL)
                .build();

        //Display loading message
        final ProgressDialog progressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false); //Disable dismiss by tapping outside of the dialog.
        progressDialog.setMessage("Loading User Information...");
        progressDialog.show();

        //Async GET request call
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                try {

                    final JSONObject jObject = new JSONObject(jsonData);

                    //Parse JSON and Update UI
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                String username = jObject.getString("username");
                                String email = jObject.getString("email");
                                String fName = jObject.getString("fName");
                                String lName = jObject.getString("lName");

                                String purchaseText = "";

                                //Read the purchase data and prepare string to be displayed in UI
                                JSONArray purchases = jObject.getJSONArray("purchases");
                                for (int i = 0; i < purchases.length(); i++) {
                                    JSONObject purchase = purchases.getJSONObject(i);
                                    purchaseText += getString(R.string.small_space) + purchase.getString("name")+ " -- " + "$" + purchase.get("price") + "\n";
                                }

                                mAccountInformation.setVisibility(View.VISIBLE); //Successfully loaded user information, make account information views visible.

                                //UPDATE UI IN HERE.
                                progressDialog.dismiss();
                                mUsername.setText(username);
                                mEmail.setText(email);
                                mFirstName.setText(fName);
                                mLastName.setText(lName);

                                //Display recent purchases. Display appropriate message if none.
                                if (purchaseText.isEmpty()) {
                                    mRecentPurchases.setText("No Previous Purchases");
                                } else {
                                    mRecentPurchases.setText(purchaseText);
                                }

                            } catch (JSONException e) {
                                //User is logged out.
                                mFragmentTitle.setText("Please Login to view your account");
                                mFragmentTitle.setVisibility(View.VISIBLE);
                                progressDialog.dismiss();
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
