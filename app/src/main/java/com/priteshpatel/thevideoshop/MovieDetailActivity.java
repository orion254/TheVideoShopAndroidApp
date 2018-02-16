package com.priteshpatel.thevideoshop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MovieDetailActivity extends AppCompatActivity   {

    private static final String BASE_URL = "https://thevideoshop.herokuapp.com/api/movies/";
    private static final String REVIEW_URL = "https://thevideoshop.herokuapp.com/api/reviews/";

    private ListView mReviewListView;
    private ReviewListAdapter mReviewListAdapter;
    private ArrayList<Review> mReviewsList;
    private Handler mHandler;

    private TextView mMovieTitle;
    private ImageView mMoviePoster;
    private TextView mOverviewText;
    private Button mTrailerButton;
    private Button mPurchaseButton;
    private TextView mOverviewTitle;
    private TextView mReviewTitle;
    private View mSeparator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        //Setup all views
        mMovieTitle = findViewById(R.id.tv_movieName);
        mMoviePoster = findViewById(R.id.poster_imageView);
        mOverviewText = findViewById(R.id.tv_overview_text);
        mTrailerButton = findViewById(R.id.trailer_button);
        mPurchaseButton = findViewById(R.id.buy_button);
        mOverviewTitle = findViewById(R.id.tv_overview_title);
        mReviewTitle = findViewById(R.id.tv_reviews_title);
        mSeparator = findViewById(R.id.separator);

        //Hide all views
        mMovieTitle.setVisibility(View.INVISIBLE);
        mMoviePoster.setVisibility(View.INVISIBLE);
        mOverviewText.setVisibility(View.INVISIBLE);
        mTrailerButton.setVisibility(View.INVISIBLE);
        mPurchaseButton.setVisibility(View.INVISIBLE);
        mOverviewTitle.setVisibility(View.INVISIBLE);
        mReviewTitle.setVisibility(View.INVISIBLE);
        mSeparator.setVisibility(View.INVISIBLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final String movieId = intent.getStringExtra("movieId");

        mHandler = new Handler(Looper.getMainLooper());
        mReviewsList= new ArrayList<Review>();

        mPurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPurchase(movieId);
            }
        });

        downloadAndUpdateView(movieId);
    }

    /**
     * Downloads movie information and reviews, and updates the UI with movie information.
     * @param movieId id of the movie that is being displayed
     */
    public void downloadAndUpdateView(String movieId) {

        try {
            //Get OkHTTP client instance from Application class
            HttpClient httpClient = (HttpClient)getApplication();
            OkHttpClient client = httpClient.getClient();

            //Build GET request for retrieving movie information
            Request request = new Request.Builder()
                    .url(this.BASE_URL + movieId)
                    .build();


            //Display loading message
            final ProgressDialog progressDialog = new ProgressDialog(MovieDetailActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading Movie...");
            progressDialog.show();

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

                                try {
                                    //Read Movie data from JSON Array
                                    JSONObject movie = jArray.getJSONObject(0);
                                    String title = movie.getString("title");
                                    String poster = movie.getString("poster");
                                    final String trailer = movie.getString("trailer");
                                    String price = movie.getString("price");
                                    String overview = movie.getString("description");

                                    //Display movie data in UI
                                    mMovieTitle.setText(title);
                                    Picasso.with(MovieDetailActivity.this).load(poster).into(mMoviePoster);
                                    mOverviewText.setText(overview);
                                    mPurchaseButton.setText("Purchase ($" + price + ")");

                                    //Setup trailer button.
                                    mTrailerButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
                                            Pattern compiledPattern = Pattern.compile(pattern);
                                            Matcher matcher = compiledPattern.matcher(trailer);
                                            matcher.find(); //Find the id from the trailer link
                                            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + matcher.group()));
                                            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse(trailer));
                                            try {
                                                startActivity(appIntent);
                                            } catch (ActivityNotFoundException ex) {
                                                startActivity(webIntent);
                                            }
                                        }
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            //Build GET request for retrieving reviews
            Request reviewRequest = new Request.Builder()
                    .url(this.REVIEW_URL + movieId)
                    .build();

            //Async GET request call
            Call reviewCall = client.newCall(reviewRequest);
            reviewCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String jsonData = response.body().string();
                    try {
                        final JSONArray jArray = new JSONArray(jsonData);

                        //Parse JSON and Update Reviews UI
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //Parse all reviews from JSON array into the mReviewList field
                                    for (int i = 0; i < jArray.length(); i++) {
                                        JSONObject review = jArray.getJSONObject(i);
                                        JSONObject authorObject = review.getJSONObject("author");
                                        String authorName = authorObject.getString("username");
                                        String reviewText = review.getString("text");
                                        mReviewsList.add(new Review(authorName,reviewText));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                progressDialog.dismiss();

                                //Movie information and Reviews loaded successfully, show all views
                                mMovieTitle.setVisibility(View.VISIBLE);
                                mMoviePoster.setVisibility(View.VISIBLE);
                                mOverviewText.setVisibility(View.VISIBLE);
                                mTrailerButton.setVisibility(View.VISIBLE);
                                mPurchaseButton.setVisibility(View.VISIBLE);
                                mOverviewTitle.setVisibility(View.VISIBLE);
                                mReviewTitle.setVisibility(View.VISIBLE);
                                mSeparator.setVisibility(View.VISIBLE);

                                //Review list related
                                mReviewListAdapter = new ReviewListAdapter(getApplicationContext(), mReviewsList);
                                mReviewListView = (ListView)findViewById(R.id.listview_review);
                                mReviewListView.setAdapter(mReviewListAdapter);
                                setListViewHeightBasedOnChildren(mReviewListView);
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

    /**
     * Process the purchase. Send API call to server and display message according to response.
     * @param movieId of movie
     */
    public void processPurchase(String movieId) {

        //Display authenticating message
        final ProgressDialog progressDialog = new ProgressDialog(MovieDetailActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Purchasing Movie");
        progressDialog.show();

        //Get OkHTTP client instance from Application class
        HttpClient httpClient = (HttpClient)getApplication();
        OkHttpClient client = httpClient.getClient();

        RequestBody body = new FormBody.Builder().build(); //Empty body - Need to make a post request

        //Build POST request for retrieving movie information
        Request request = new Request.Builder()
                .url(this.BASE_URL + movieId + "/buy")
                .post(body)
                .build();

        //ASYNC POST request
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if (response.code() == 200) {
                            Toast.makeText(getBaseContext(), "Purchased Movie Successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getBaseContext(), "Please Login to Purchase a movie", Toast.LENGTH_LONG).show();
                        }

                    }
                }, 2000);//2000 milliseconds delay.
            }
        });
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

    /**
     * Represents a movie review.
     */
    public class Review {
        private String reviewerName;
        private String reviewText ;
        private int icon = 0;

        public Review(String reviewerName, String reviewText ){
            this.reviewerName = reviewerName;
            this.reviewText = reviewText;
        }
        public String getReview(){
            return reviewText;
        }

        public String getAuthor(){
            return reviewerName;
        }

        public int getIcon(){
            return this.icon;
        }
        public void setIcon(int icon){
            this.icon = icon;
        }
    }

    public class ReviewListAdapter extends BaseAdapter {
        private Context context;
        private ArrayList< Review> reviews;

        public ReviewListAdapter(Context context, ArrayList< Review> reviews){
            this.context = context;
            this.reviews = reviews;
        }

        @Override
        public int getCount() {
            return reviews.size();
        }

        @Override
        public Object getItem(int position) {
            return reviews.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater)
                        context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.list_item_review, null);
            }

            TextView aurthor = (TextView) convertView.findViewById(R.id.review_author);
            TextView review = (TextView) convertView.findViewById(R.id.review_detail);

            aurthor.setText(reviews.get(position).getAuthor());
            review.setText(reviews.get(position).getReview());

            return convertView;
        }
    }

    /**
     * For Reviews List
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        ListAdapter adapter = listView.getAdapter();

        int grossElementHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View childView = adapter.getView(i, null, listView);
            childView.measure(UNBOUNDED, UNBOUNDED);
            grossElementHeight += childView.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = grossElementHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
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

