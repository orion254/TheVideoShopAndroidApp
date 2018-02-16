package com.priteshpatel.thevideoshop;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

public class SearchActivity extends AppCompatActivity   {

    private static final String SEARCH_URL = "https://thevideoshop.herokuapp.com/api/search?q=";

    private Handler mHandler;
    private ListView moviesListView;
    private MovieListAdapter mMovieListAdapter;
    private ArrayList<MovieItem> mMoviesList;

    private TextView mSearchTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mHandler = new Handler(Looper.getMainLooper());

        mMoviesList= new ArrayList<>();

        mSearchTitle = findViewById(R.id.search_text);
        mSearchTitle.setVisibility(View.INVISIBLE);

        //GET SEARCH VALUE FROM INTENT.
        Intent intent = getIntent();
        String query = intent.getStringExtra("query");

        mSearchTitle.setText("Searching for '" + query + "'");

        searchAndUpdateUI(query);//SEARCH and Update UI
    }

    /**
     * Searches for the movie and displays results.
     * @param query string to be searched.
     */
    public void searchAndUpdateUI(final String query) {
        try {
            //Get OkHTTP client instance from Application class
            HttpClient httpClient = (HttpClient) getApplication();
            OkHttpClient client = httpClient.getClient();

            //Build GET request for retrieving search data
            Request request = new Request.Builder()
                    .url(this.SEARCH_URL + query)
                    .build();

            //Display appropriate loading message
            final ProgressDialog progressDialog = new ProgressDialog(SearchActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Searching for '" + query + "'");
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
                        final JSONArray jArray = new JSONArray(jsonData);

                        //Parse JSON and Update UI
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                ArrayList<String> movieIds = new ArrayList<String>();

                                try {
                                    for (int i = 0; i < jArray.length(); i++) {
                                        JSONObject movie = jArray.getJSONObject(i);
                                        mMoviesList.add(new MovieItem(movie.getString("title"), movie.getString("description"), movie.getString("poster")));
                                        String movieId = movie.getString("_id");
                                        movieIds.add(movieId);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                progressDialog.dismiss();
                                mSearchTitle.setVisibility(View.VISIBLE);

                                mSearchTitle.setText("Results for '" + query + "'");

                                if (jArray == null || jArray.length() == 0) {
                                    mSearchTitle.setText("No Results Found for '" + query + "'");
                                }

                                //Update activity to show search results.
                                mMovieListAdapter = new MovieListAdapter(getApplicationContext(), mMoviesList, movieIds.toArray(new String[movieIds.size()]));
                                moviesListView = (ListView)findViewById(R.id.lv_list);
                                moviesListView.setAdapter(mMovieListAdapter);
                                moviesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View v,
                                                            int position, long id) {
                                        // Sending image id to FullScreenActivity
                                        Intent i = new Intent(SearchActivity.this, MovieDetailActivity.class);

                                        //Pass the activity with movie ID.
                                        SearchActivity.MovieListAdapter adapter = (SearchActivity.MovieListAdapter)parent.getAdapter();
                                        i.putExtra("movieId", adapter.getMovieId(position));

                                        startActivity(i);
                                    }
                                });
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("NewFragmentTag");
        fragment.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {

            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        }
    }

    public class MovieListAdapter extends BaseAdapter {

        private Context context;
        private String[] movieIDs;
        private ArrayList<MovieItem> movieItems;

        public MovieListAdapter(Context context, ArrayList< MovieItem> movieItems, String[] movieIDs){
            this.context = context;
            this.movieItems = movieItems;
            this.movieIDs = movieIDs;
        }

        @Override
        public int getCount() {
            return movieItems.size();
        }

        @Override
        public Object getItem(int position) {
            return movieItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public String getMovieId(int position) {
            return movieIDs[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.list_item_search, null);
            }

            ImageView poster = (ImageView) convertView.findViewById(R.id.iv_poster);
            TextView movieName = (TextView) convertView.findViewById(R.id.movie_name);
            TextView movieOverview = (TextView) convertView.findViewById(R.id.movie_overview);

            movieName.setText(movieItems.get(position).getMovieName());
            movieOverview.setText(movieItems.get(position).getMovieOverview());
            Context context = parent.getContext();
            Picasso.with(context).load(movieItems.get(position).getMoviePoster()).into(poster);
            return convertView;
        }
    }

    /**
     * Holds movie information to be shown in the search activity.
     */
    public class MovieItem {
        private String  movieName;
        private String moviePoster;
        private String movieOverview ;
        private int icon = 0;

        public MovieItem(String movieName, String overview, String poster){
            this.movieName = movieName;
            this.movieOverview = overview;
            this.moviePoster = poster;
        }

        public String getMovieName(){
            return this.movieName;
        }

        public String getMovieOverview() {
            return movieOverview;
        }

        public String getMoviePoster() {
            return moviePoster;
        }

        public int getIcon(){
            return this.icon;
        }
        public void setIcon(int icon){
            this.icon = icon;
        }
    }
}

