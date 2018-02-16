package com.priteshpatel.thevideoshop;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private HttpClient mHttpClient;
    private NavigationView mNavigationView;
    private View mNavigationHeader;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private Button mLoginButton;
    private Button mRegisterButton;
    private Button mLogoutButton;

    private ImageView mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHttpClient = (HttpClient)getApplication();

        //Initial setup
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open_drawer, R.string.close_drawer);
        mDrawerLayout.addDrawerListener(mToggle);
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nv);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(nvDrawer);

        mNavigationView = (NavigationView) findViewById(R.id.nv);
        mNavigationHeader = mNavigationView.getHeaderView(0);

        //Setup Onclick listener for the login button
        mLoginButton = mNavigationHeader.findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, 1001);
            }
        });

        //Setup Onclick listener for the register button
        mRegisterButton = mNavigationHeader.findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, 1002);
            }
        });

        //Setup Onclick listener for the log out button
        mLogoutButton = mNavigationHeader.findViewById(R.id.logout_button);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpClient httpClient = (HttpClient)getApplication();
                httpClient.logOut();
                updateNavigationDrawerButtons();
                Toast.makeText(getBaseContext(), "Logged Out Successfully", Toast.LENGTH_LONG).show();

                //Refresh the my account fragment
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("myAccountFragment");
                if (fragment != null ){
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.detach(fragment).attach(fragment).commit();
                }
            }
        });

        updateNavigationDrawerButtons(); //Display the correct buttons (sign in, sign out, sign up) in the navigation drawer.

        //Display catalogue fragment by default.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.flcontent, new CatalogueFragment())
                    .commit();
        }
    }

    /**
     * Displays login, logout and register buttons in the drawer menu based on if user is logged in or not.
     */
    public void updateNavigationDrawerButtons() {
        //Display buttons based on if user is logged in or not.
        if (mHttpClient.isLoggedIn()) {
            mLoginButton.setVisibility(View.GONE);
            mRegisterButton.setVisibility(View.GONE);
            mLogoutButton.setVisibility(View.VISIBLE);
        } else {
            mLoginButton.setVisibility(View.VISIBLE);
            mRegisterButton.setVisibility(View.VISIBLE);
            mLogoutButton.setVisibility(View.GONE);
        }
    }

    /**
     * Open drawer when hamburger icon is selected.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectItemDrawer(item);
                return true;
            }
        });
    }

    /**
     * Handles navigation view clicks
     */
    public void selectItemDrawer(MenuItem menuItem) {
        Fragment myFragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.catalogue:
                fragmentClass = CatalogueFragment.class;
                break;
            case R.id.search:
                fragmentClass = SearchFragment.class;
                break;
            case R.id.account:
                fragmentClass = MyAccountFragment.class;
                break;
            case R.id.weather:
                fragmentClass = WeatherFragment.class;
                break;
            case R.id.about:
                fragmentClass = AboutFragment.class;
                break;
            case R.id.privacy:
                fragmentClass = PrivacyFragment.class;
                break;
            case R.id.contact:
                fragmentClass = ContactFragment.class;
                break;
            default:
                fragmentClass = CatalogueFragment.class;
                break;
        }

        try {
            myFragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        //Add tag if opening my account fragment. Used for refreshing on login and logout
        if (fragmentClass == MyAccountFragment.class) {
            fragmentManager.beginTransaction().replace(R.id.flcontent, myFragment, "myAccountFragment").commit();
        } else {
            fragmentManager.beginTransaction().replace(R.id.flcontent, myFragment).commit();
        }

        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationDrawerButtons();  //Display the correct buttons (sign in, sign out, sign up) in the navigation drawer.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 999) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("myAccountFragment");
            if (fragment != null && fragment.isVisible()){
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.detach(fragment).attach(fragment).commit();
            }
        }
    }

}
