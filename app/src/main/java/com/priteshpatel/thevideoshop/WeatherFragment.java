package com.priteshpatel.thevideoshop;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

public class WeatherFragment extends Fragment {

    private static final String OWM_API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric"; //URL for fetching weather data from
    Typeface weatherFont;

    TextView mCityField;
    TextView mLastUpdated;
    TextView mDetailsField;
    TextView mTemperatureField;
    TextView mWeatherIcon;

    Handler handler; //For updating UI thread.

    public WeatherFragment() {
        this.handler = new Handler();
    }

    //Method returns view for this fragment.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);

        //Initialise views
        mCityField = (TextView)rootView.findViewById(R.id.city_field);
        mLastUpdated = (TextView)rootView.findViewById(R.id.updated_field);
        mDetailsField = (TextView)rootView.findViewById(R.id.details_field);
        mTemperatureField = (TextView)rootView.findViewById(R.id.current_temperature_field);
        mWeatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);

        mWeatherIcon.setTypeface(weatherFont);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather_icons_font.ttf");
        updateWeather("Wellington, NZ");
    }

    /**
     * Fetch the weather data and update the UI (calls a different method for updating ui)
     * @param city
     */
    private void updateWeather(final String city) {

        //Display loading message
        final ProgressDialog progressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false); //Disable dismiss by tapping outside of the dialog.
        progressDialog.setMessage("Loading Weather Information...");
        progressDialog.show();


        //Start a new that that calls getJSON on the FetchWeather class. If the value returned by getJSON is null, then display error message. If it isn't, then display the weather.
        new Thread() {
            public void run() {
                final JSONObject json = fetchWeather(getActivity(), city);
                if (json == null) {
                    handler.post(new Runnable() {
                       public void run() {
                           Toast.makeText(getActivity(), getActivity().getString(R.string.weather_not_found), Toast.LENGTH_LONG).show();
                       }
                    });
                } else {
                    //WeatherFragment found. Use handler to show weather - cant call methods on main thread directly from a background thread.
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            displayWeather(json);
                        }
                    });
                }

                progressDialog.dismiss();
            }
        }.start();
    }


    /**
     * Fetches the weather data from the API and returns it as a JSON object.
     * @param context
     * @param city
     * @return JSONObject with weather data. null if there is an error.
     */
    public JSONObject fetchWeather (Context context, String city) {
        try {
            URL url = new URL(String.format(OWM_API_URL, city)); //Setup url
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.addRequestProperty("x-api-key", context.getString(R.string.weather_api_key));

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String temp = ""; //For reading into
            while((temp = reader.readLine()) != null) {
                json.append(temp).append("\n");
            }
            reader.close();

            JSONObject weatherData = new JSONObject(json.toString());

            //If fetching not successful - i.e., 404 error
            if(weatherData.getInt("cod") != 200){
                return null;
            }

            return weatherData;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses weather JSON data and updates the text view objects.
     * @param json
     */
    private void displayWeather(JSONObject json) {

        //Error, display appropriate messag
        if (json == null) {
            mCityField.setText("Weather not avaiable. Try again later.");
            return;
        }

        try {
            mCityField.setText(json.getString("name").toUpperCase() + ", " + json.getJSONObject("sys").getString("country"));
            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            mDetailsField.setText(details.getString("description").toUpperCase() +
            "\n" + "Humidity: " + main.getString("humidity") + "%");

            mTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + "℃");
            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            mLastUpdated.setText("Last update: " + updatedOn);

            //Display correct weather icon.
            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the correct WeatherFragment icon in the UI.
     * @param actualId
     * @param sunrise
     * @param sunset
     */
    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";

        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        mWeatherIcon.setText(icon);
    }
}
