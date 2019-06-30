package io.github.wigny.chegueiaomeudestino.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import io.github.wigny.chegueiaomeudestino.MainActivity;
import io.github.wigny.chegueiaomeudestino.R;
import io.github.wigny.chegueiaomeudestino.classes.Utils;

import static io.github.wigny.chegueiaomeudestino.classes.Utils.GPS_DISABLED;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.PACKAGE_NAME;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMarkerPosition;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getNotificationSoundIsEnabled;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getNotificationVibrationIsEnabled;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getRepeatDaysIsTrue;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getRingtoneUri;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setRequestingLocationUpdates;

public class LocationService extends Service {

    private NotificationCompat.Builder builder;
    private static final String CHANNEL_ID = "channel_01";
    private static final int NOTIFICATION_ID = 12345678;

    private static final String TAG = LocationService.class.getSimpleName();
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private Vibrator vibrator;
//    private Ringtone ringtone;
    private MediaPlayer player;

    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();

        getLastLocation();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            Objects.requireNonNull(mNotificationManager).createNotificationChannel(mChannel);
        }

        builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        startForeground(NOTIFICATION_ID, getNotification(builder));

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        player = MediaPlayer.create(getApplicationContext(), getRingtoneUri(getApplicationContext()));
        player.setLooping(true);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,false);

        if (startedFromNotification) {
            stopSelf();
        }

        startLocationUpdates();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        if(player.isPlaying()) player.stop();
        if(vibrator != null) vibrator.cancel();

        if (player != null) player.release();
        player = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");

                                LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                                try {
                                    boolean gps_enabled = Objects.requireNonNull(lm).isProviderEnabled(LocationManager.GPS_PROVIDER);
                                    if(!gps_enabled) openMainActivity();
                                } catch(Exception ignored) {}
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);

        mLocation = location;

        LatLng myLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLng destination =  Objects.requireNonNull(getMarkerPosition(this));

        float distance = distanceBetween(myLocation, destination);

        if(distance <= getMinimumDistance()) {
            mNotificationManager.notify(NOTIFICATION_ID, newNotification(builder));
            stopLocationUpdates();
            if(!getRepeatDaysIsTrue(this)) setRequestingLocationUpdates(this, false);
        }
//        setDistanceLocation(getApplicationContext(), distance);
    }

    private Notification getNotification(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, LocationService.class);

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        builder.addAction(R.drawable.ic_action_launch, getString(R.string.notification_launch),
                activityPendingIntent)
                .addAction(R.drawable.ic_action_cancel, getString(R.string.notification_stop),
                        servicePendingIntent)
                .setContentText(getString(R.string.notification_text))
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setSmallIcon(R.mipmap.ic_notification)
//                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                        R.mipmap.ic_launcher))
                .setWhen(System.currentTimeMillis())
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private Notification newNotification(NotificationCompat.Builder builder) {
        long[] pattern = {0, 1000, 100, 1000, 100, 1000, 100, 1000, 100};

        builder.setContentText(getString(R.string.notification_wakeup_content))
                .setContentTitle(getString(R.string.notification_wakeup_title));

        if (getNotificationVibrationIsEnabled(getApplicationContext()))
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));

        if(getNotificationSoundIsEnabled(getApplicationContext()))
            player.start();

        return builder.build();
    }

    private float distanceBetween(LatLng latLng1, LatLng latLng2) {
        Location loc1 = new Location(LocationManager.GPS_PROVIDER);
        Location loc2 = new Location(LocationManager.GPS_PROVIDER);

        loc1.setLatitude(latLng1.latitude);
        loc1.setLongitude(latLng1.longitude);

        loc2.setLatitude(latLng2.latitude);
        loc2.setLongitude(latLng2.longitude);

        return loc1.distanceTo(loc2);
    }

    private int getMinimumDistance() {
        int minimumDistance = 0;
        switch (Utils.getMinimumDistance(this)) {
            case 0:
                minimumDistance = 300;
                break;
            case 1:
                minimumDistance = 500;
                break;
            case 2:
                minimumDistance = 700;
                break;
            case 3:
                minimumDistance = 900;
                break;
            case 4:
                minimumDistance = 1000;
                break;
            case 5:
                minimumDistance = 1500;
                break;
        }
        return minimumDistance;
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(GPS_DISABLED, true);
        startActivity(intent);
    }
}
