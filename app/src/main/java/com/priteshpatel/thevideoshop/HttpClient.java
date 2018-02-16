package com.priteshpatel.thevideoshop;

import android.app.Application;
import okhttp3.OkHttpClient;

/**
 * Created by Pritesh Patel on 15/02/2018.
 */

public class HttpClient extends Application {

    private OkHttpClient client;
    private CookieStore store;

    private boolean loggedIn = false;

    @Override
    public void onCreate() {
        super.onCreate();

        initialiseClient(); //Initialise the HTTP client
    }

    /**
     * Initialises an OKHTTP client (with cookie store) to be used in all parts of the app.
     */
    private void initialiseClient() {
        this.store = new CookieStore();
        this.client = new OkHttpClient.Builder()
                .cookieJar(this.store)
                .build();
    }

    /**
     * Set user as logged in. Used to display correct buttons (sign up, sign in, sign out) in navigation drawer.
     */
    public void setLoggedIn() {
        this.loggedIn = true;
    }

    /**
     * Empties the cookie jar - used for logging out.
     */
    public void logOut() {
        this.loggedIn = false;
        this.store.emptyStore();
    }

    /**
     * Checks if a user is logged in or not.
     * @return true if logged in, else false.
     */
    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    /**
     * Returns an OK HTTP client instance.
     * @return OK HTTP client instance
     */
    public OkHttpClient getClient() {
        return this.client;
    }
}
