/**
 * https://www.sitepoint.com/consuming-web-apis-in-android-with-okhttp/
 */

package com.priteshpatel.thevideoshop;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by Pritesh Patel on 15/02/2018.
 */

public class CookieStore implements CookieJar {
    private final Set<Cookie> cookieStore = new HashSet<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.addAll(cookies); //Saves cookies from HTTP response
    }

    /**
     * Load cookies from the jar for an HTTP request. This method returns cookies that have not yet expired
     */
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {

        List<Cookie> validCookies = new ArrayList<>();
        for (Cookie cookie : cookieStore) {
            LogCookie(cookie);
            if (cookie.expiresAt() < System.currentTimeMillis()) {
                // invalid cookies
            } else {
                validCookies.add(cookie);
            }
        }
        return validCookies;
    }

    /**
     * Clears the cookie jar. Used for signing out of account.
     */
    public void emptyStore() {
        cookieStore.clear();
    }

    //Print the values of cookies - Useful for testing
    private void LogCookie(Cookie cookie) {
        System.out.println("String: " + cookie.toString());
        System.out.println("Expires: " + cookie.expiresAt());
        System.out.println("Hash: " + cookie.hashCode());
        System.out.println("Path: " + cookie.path());
        System.out.println("Domain: " + cookie.domain());
        System.out.println("Name: " + cookie.name());
        System.out.println("Value: " + cookie.value());
    }
}
