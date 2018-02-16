package com.priteshpatel.thevideoshop;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CatalogueFragment extends Fragment {

    private static final String CATALOGUE_URL = "https://thevideoshop.herokuapp.com/api/movies"; //URL for fetching cateloue data

    private GridView mGridView;
    private ImageAdapter mImageAdapter;
    private Handler mHandler;

    private int h = 0;
    private int w = 0;

    public CatalogueFragment() {
        //Empty
    }

    //Method returns view for this fragment.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        int length = Resources.getSystem().getDisplayMetrics().widthPixels;
        w = length/2 - 20;
        h = length/2 + 100;

        View rootView = inflater.inflate(R.layout.fragment_catalogue, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mHandler = new Handler(Looper.getMainLooper());

        updateCatalogue(getActivity());//DOWNLOAD CatalogueFragment JSON and Update the grid view

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * Download catalogue JSON data and display in fragment.
     * @param context
     */
    public void updateCatalogue (Context context) {
        try {

            //Display loading message
            final ProgressDialog progressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false); //Disable dismiss by tapping outside of the dialog.
            progressDialog.setMessage("Loading Catalogue...");
            progressDialog.show();

            //Get HTTP client from Application class.
            HttpClient httpClient = (HttpClient) getActivity().getApplication();
            OkHttpClient client = httpClient.getClient();

            //Build GET request
            Request request = new Request.Builder()
                    .url(CATALOGUE_URL)
                    .build();

            //Async GET request call
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //Print Stack trance on failure
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonData = response.body().string();
                    try {
                        final JSONArray jArray = new JSONArray(jsonData);

                        //Parse JSON and Update UI
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<String> movieIds = new ArrayList<String>();
                                ArrayList<String> posterUrls = new ArrayList<String>();

                                //Create array of posters
                                try {
                                    for (int i = 0; i <  jArray.length(); i++ ) {
                                            JSONObject movie = jArray.getJSONObject(i);
                                            String movieId = movie.getString("_id");
                                            String poster = movie.getString("poster");

                                            movieIds.add(movieId);
                                            posterUrls.add(poster);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                mImageAdapter = new ImageAdapter(getActivity(), movieIds.toArray(new String[movieIds.size()]), posterUrls.toArray(new String[posterUrls.size()]));
                                mGridView.setAdapter(mImageAdapter);
                                mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View v,
                                                            int position, long id) {
                                        // Sending image id to FullScreenActivity
                                        Intent i = new Intent(getActivity(), MovieDetailActivity.class);

                                        //Pass the activity with movie ID.
                                        ImageAdapter adapter = (ImageAdapter)parent.getAdapter();
                                        i.putExtra("movieId", adapter.getMovieId(position));

                                        startActivity(i);
                                    }
                                });
                                progressDialog.dismiss();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
    }

    // =======================================
    // IMAGE ADAPETER
    // =======================================

    /**
    Used To display CatalogueFragment
     **/
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public String[] movieIDs;
        public String[] posterUrls;

        // Constructor - Called every time catelogue is opened/clicked on
        public ImageAdapter(Context c, String[] movieIds, String[] posterUrls){
            mContext = c;
            this.movieIDs = movieIds;
            this.posterUrls = posterUrls;
        }

        @Override
        public int getCount() {
            return movieIDs.length;
        }

        @Override
        public Object getItem(int position) {
            return movieIDs[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public String getMovieId(int position) {
            return movieIDs[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(mContext);

            Picasso.with(mContext).load(posterUrls[position]).into(imageView);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(w, h));

            return imageView;
        }
    }

}
