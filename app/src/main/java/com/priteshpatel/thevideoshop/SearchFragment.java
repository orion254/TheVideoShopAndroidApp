package com.priteshpatel.thevideoshop;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SearchFragment extends Fragment {

    private static final String ACCOUNT_URL = "https://thevideoshop.herokuapp.com/api/users/me";

    private EditText mSearchField;
    private Button mSearchButton;

    //Method returns view for this fragment.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        //Initialise Views
        mSearchField =  rootView.findViewById(R.id.input_search);
        mSearchButton = rootView.findViewById(R.id.search_btn);

        //Set up search button click listener.
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If not a valid search query, then do not attempt to process.
                if (!validateSearchField()) {
                    return;
                }

                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra("query", mSearchField.getText().toString());
                startActivity(intent);
            }
        });

        return rootView;
    }

    /**
     * Checks if the search query is valid.
     * @return true if valid search query, else false.
     */
    public boolean validateSearchField() {
        boolean valid = true;
        String query = mSearchField.getText().toString();

        if (query.isEmpty()) {
            mSearchField.setError("enter search query");
            valid = false;
        } else {
            mSearchField.setError(null);
        }

        return valid;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
