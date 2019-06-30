package io.github.wigny.chegueiaomeudestino;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.model.LatLng;

import io.github.wigny.chegueiaomeudestino.fragments.AboutFragment;
import io.github.wigny.chegueiaomeudestino.fragments.MainFragment;
import io.github.wigny.chegueiaomeudestino.fragments.MapsFragment;
import io.github.wigny.chegueiaomeudestino.fragments.SettingsFragment;
import io.github.wigny.chegueiaomeudestino.receivers.AlarmReceiver;
import io.github.wigny.chegueiaomeudestino.services.LocationService;

import static io.github.wigny.chegueiaomeudestino.classes.CheckConditions.CHECK_PERMISSIONS;
import static io.github.wigny.chegueiaomeudestino.classes.CheckConditions.GET_MARKER;
import static io.github.wigny.chegueiaomeudestino.classes.CheckConditions.GET_TIME;
import static io.github.wigny.chegueiaomeudestino.classes.CheckConditions.startConditions;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.GPS_DISABLED;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.LATITUDE;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.LONGITUDE;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getThemeId;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.requestingLocationUpdates;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CompoundButton.OnCheckedChangeListener,
        MainFragment.OnHeadlineSelectedListener, MapsFragment.OnHeadlineSelectedListener,
        AboutFragment.OnHeadlineSelectedListener, SettingsFragment.OnHeadlineSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Switch switchOnOff;
    private NavigationView navigationView;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private boolean firstOpenMap = true;
    private boolean firstSetTime = true;

    private int fragmentOpened = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getThemeId(this));

        super.onCreate(savedInstanceState);

        verifyGPSEnabled();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setMenuChecked(R.id.nav_main);
    }

    private void verifyGPSEnabled() {
        boolean gpsDisabled = getIntent().getBooleanExtra(GPS_DISABLED, false);
        if(gpsDisabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_location_disabled_title));
            builder.setMessage(getString(R.string.dialog_location_disabled_content));
            builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
            });
            builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switchOnOff.setChecked(false);
                }
            });
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        switchOnOff = menu.findItem(R.id.app_bar_on_off).getActionView().findViewById(R.id.switch_item);
        switchOnOff.setChecked(requestingLocationUpdates(this));
        switchOnOff.setOnCheckedChangeListener(this);

        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        if (isChecked) {
            if(checkConditions()) {
                alarmReceiver.setUpAlarms(this, true);
                toast(this, getString(R.string.toast_on));
            }
        } else {
            alarmReceiver.setUpAlarms(this,false);
            stopService(new Intent(this, LocationService.class));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        fragmentOpened = id;

        switch (id) {
            case R.id.nav_main:
                showFragment(new MainFragment());
                break;
            case R.id.nav_map:
                showFragment(new MapsFragment());
                break;
            case R.id.nav_settings:
                showFragment(new SettingsFragment());
                break;
            case R.id.nav_about:
                showFragment(new AboutFragment());
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (!(permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openLocationRequestDialog();
            }
        }
    }

    public void setMenuChecked(int id) {
        if (fragmentOpened != id) {
            navigationView.setCheckedItem(id);
            navigationView.getMenu().performIdentifierAction(id, 0);
        }
    }

    private boolean checkConditions() {
        String result = startConditions(this);
        if (result != null) {
            if (result.equals(CHECK_PERMISSIONS))
                openLocationRequestDialog();

            else if (result.equals(GET_TIME)) {
                if (firstSetTime) {
                    openTimeDialog();
                    firstSetTime = true;
                }
                else toast(this, getString(R.string.dialog_time_ok));
            }

            else if (result.equals(GET_MARKER)) {
                if (firstOpenMap) {
                    openMapDialog();
                    firstOpenMap = false;
                }
                else toast(this, getString(R.string.dialog_map_ok));
            }

            switchOnOff.setChecked(false);
            return false;
        } else return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void openMapDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_map_title));
        builder.setMessage(getString(R.string.dialog_map_content));
        builder.setPositiveButton(getString(R.string.dialog_map_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                setMenuChecked(R.id.nav_map);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void openTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_time_title));
        builder.setMessage(getString(R.string.dialog_time_content));
        builder.setPositiveButton(getString(R.string.dialog_time_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                setMenuChecked(R.id.nav_main);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void openLocationRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.location_permission_request_title));
        builder.setMessage(getString(R.string.location_permission_request_content));
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                requestPermissions();
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showFragment(Fragment fragment) {
        if (findViewById(R.id.fragment_container) != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onOpenFragment(int id) {
        setMenuChecked(id);
    }

    @Override
    public void onShowLocation(LatLng latLng) {
        MapsFragment mapsFragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putString(LATITUDE, String.valueOf(latLng.latitude));
        args.putString(LONGITUDE, String.valueOf(latLng.longitude));
        mapsFragment.setArguments(args);

        showFragment(mapsFragment);
        navigationView.setCheckedItem(R.id.nav_map);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof MainFragment) {
            MainFragment headlinesFragment = (MainFragment) fragment;
            headlinesFragment.setOnHeadlineSelectedListener(this);
        }

        if (fragment instanceof MapsFragment) {
            MapsFragment headlinesFragment = (MapsFragment) fragment;
            headlinesFragment.setOnHeadlineSelectedListener(this);
        }

        if (fragment instanceof AboutFragment) {
            AboutFragment headlinesFragment = (AboutFragment) fragment;
            headlinesFragment.setOnHeadlineSelectedListener(this);
        }

        if (fragment instanceof SettingsFragment) {
            SettingsFragment headlinesFragment = (SettingsFragment) fragment;
            headlinesFragment.setOnHeadlineSelectedListener(this);
        }
    }

    @Override
    public void onRestartActivity() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }
}