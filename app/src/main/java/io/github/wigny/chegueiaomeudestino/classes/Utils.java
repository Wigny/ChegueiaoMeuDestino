package io.github.wigny.chegueiaomeudestino.classes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Objects;

import io.github.wigny.chegueiaomeudestino.MainActivity;
import io.github.wigny.chegueiaomeudestino.R;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class Utils {

    public static final String PACKAGE_NAME = Objects.requireNonNull(MainActivity.class.getPackage()).getName();

    public static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    public static String LATITUDE = "latitude";
    public static String LONGITUDE = "longitude";
    public static String GPS_DISABLED = "gps_disabled";
    private static String THEME = "theme";
    private static String THEME_MODE = "theme_mode";
    private static String HOUR = "hour";
    private static String MINUTES = "minutes";
    private static String POSITION = "position";
    private static String MAP_TYPE = "map_type";
    private static String MAP_TRAFFIC = "map_traffic";
    private static String MAP_CONTROLS = "map_controls";
    private static String NOTIFICATION_SOUND_ENABLED = "notification_sound_enabled";
    private static String NOTIFICATION_VIBRATION_ENABLED = "notification_vibration_enabled";
    static String DISTANCE_LOCATION = "distance_location";
    private static String THEME_P = "theme_position";

    public static void clearPreferences(Context context) {
        getDefaultSharedPreferences(context)
                .edit()
                .clear()
                .apply();
    }

    private static void setBooleanPreferences(Context context, String key, boolean b) {
        getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(key, b)
                .apply();
    }

    private static boolean getBooleanPreferences(Context context, String key, boolean defValue) {
        return getDefaultSharedPreferences(context)
                .getBoolean(key, defValue);
    }

    private static void setIntPreferences(Context context, String key, int i) {
        getDefaultSharedPreferences(context)
                .edit()
                .putInt(key, i)
                .apply();
    }

    private static int getIntPreferences(Context context, String key, int defValue) {
        return getDefaultSharedPreferences(context)
                .getInt(key, defValue);
    }

    private static void setFloatPreferences(Context context, String key, float f) {
        getDefaultSharedPreferences(context)
                .edit()
                .putFloat(key, f)
                .apply();
    }

    private static float getFloatPreferences(Context context, String key, float defValue) {
        return getDefaultSharedPreferences(context)
                .getFloat(key, defValue);
    }

    private static void setStringPreferences(Context context, String key, String s) {
        getDefaultSharedPreferences(context)
                .edit()
                .putString(key, s)
                .apply();
    }

    private static String getStringPreferences(Context context, String key, String defValue) {
        return getDefaultSharedPreferences(context)
                .getString(key, defValue);
    }

    public static void toast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

//    <code>

    public static boolean requestingLocationUpdates(Context context) {
        return getBooleanPreferences(context, KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        setBooleanPreferences(context, KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates);
    }

    public static LatLng getMarkerPosition(Context context) {
        double latitude = Double.parseDouble(getStringPreferences(context, LATITUDE, "0"));
        double longitude = Double.parseDouble(getStringPreferences(context, LONGITUDE, "0"));

        if(latitude!=0 && longitude!=0) return new LatLng(latitude, longitude);
        else return null;
    }

    public static void setMarkerPosition(Context context, LatLng latLng) {
        setStringPreferences(context, LATITUDE, String.valueOf(latLng.latitude));
        setStringPreferences(context, LONGITUDE, String.valueOf(latLng.longitude));
    }

    public static void setTime(Context context, int hourOfDay, int minutes) {
        setIntPreferences(context, HOUR, hourOfDay);
        setIntPreferences(context, MINUTES, minutes);
    }

    public static int getTimeHour(Context context) {
        return getIntPreferences(context, HOUR, 25);
    }

    public static int getTimeMinutes(Context context) {
        return getIntPreferences(context, MINUTES, 61);
    }

    public static void setMinimumDistance(Context context, int position) {
        setIntPreferences(context, POSITION, position);
    }

    public static int getMinimumDistance(Context context) {
        return getIntPreferences(context, POSITION, 1);
    }

    public static void setRepeatDays(Context context, int day, boolean isChecked) {
        setBooleanPreferences(context, getDayName(day), isChecked);
    }

    public static boolean getRepeatDaysIsTrue(Context context) {
        boolean state = false;
        for (int i = 0; i <= 6; i++) {
            if(getRepeatDays(context, i)) state = true;
        }
        return state;
    }

    public static boolean getRepeatDays(Context context, int day) {
        return getBooleanPreferences(context, getDayName(day), false);
    }

    private static String getDayName(int day) {
        String dayName = null;
        switch (day) {
            case 0:
                dayName = "sunday";
                break;
            case 1:
                dayName = "monday";
                break;
            case 2:
                dayName = "tuesday";
                break;
            case 3:
                dayName = "wednesday";
                break;
            case 4:
                dayName = "thursday";
                break;
            case 5:
                dayName = "friday";
                break;
            case 6:
                dayName = "saturday";
                break;
        }
        return dayName;
    }

    public static int getThemeId(Context context) {
        int themeLight = R.style.AppTheme_NoActionBar;
        int themeDark = R.style.AppTheme_Dark_NoActionBar;
        int theme;

        if(getAutoThemeMode(context)) {
            Calendar rightNow = Calendar.getInstance();
            int hour = rightNow.get(Calendar.HOUR_OF_DAY);

            if(hour >= 18 || hour < 6) theme = themeDark;
            else theme = themeLight;
        } else {
            if(getThemePosition(context) == 0) theme = themeLight;
            else theme = themeDark;
        }

        return theme;
    }

    public static void setThemePosition(Context context, int position) {
        setIntPreferences(context, THEME_P, position);
    }

    public static int getThemePosition(Context context) {
        return getIntPreferences(context, THEME_P, 0);
    }

    public static void setAutoThemeMode(Context context, boolean auto) {
        setBooleanPreferences(context, THEME_MODE, auto);
    }

    public static boolean getAutoThemeMode(Context context) {
        return getBooleanPreferences(context, THEME_MODE, false);
    }

    public static void setMapType(Context context, int type) {
        setIntPreferences(context, MAP_TYPE, type);
    }

    public static int getMapType(Context context) {
        return getIntPreferences(context, MAP_TYPE, 4);
    }

    public static void setMapTraffic(Context context, boolean isChecked) {
        setBooleanPreferences(context, MAP_TRAFFIC, isChecked);
    }

    public static boolean getMapTraffic(Context context) {
        return getBooleanPreferences(context, MAP_TRAFFIC, false);
    }

    public static void setMapControls(Context context, boolean isChecked) {
        setBooleanPreferences(context, MAP_CONTROLS, isChecked);
    }

    public static boolean getMapControls(Context context) {
        return getBooleanPreferences(context, MAP_CONTROLS, true);
    }

    public static void setNotificationSoundIsEnabled(Context context, boolean isChecked) {
        setBooleanPreferences(context, NOTIFICATION_SOUND_ENABLED, isChecked);
    }

    public static boolean getNotificationSoundIsEnabled(Context context) {
        return getBooleanPreferences(context, NOTIFICATION_SOUND_ENABLED, false);
    }

    public static Uri getRingtoneUri(Context context) {
        return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
    }

    public static void setNotificationVibrationIsEnabled(Context context, boolean isChecked) {
        setBooleanPreferences(context, NOTIFICATION_VIBRATION_ENABLED, isChecked);
    }

    public static boolean getNotificationVibrationIsEnabled(Context context) {
        return getBooleanPreferences(context, NOTIFICATION_VIBRATION_ENABLED, true);
    }

    static void setDistanceLocation(Context context, float distance) {
        setFloatPreferences(context, DISTANCE_LOCATION, distance);
    }

    public static float getDistanceLocation(Context context) {
        return getFloatPreferences(context, DISTANCE_LOCATION, 0);
    }

    static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text);
        clipboardManager.setPrimaryClip(clipData);
        toast(context, context.getString(R.string.copied_to_clipboard));
    }

//    private int exemple(int a, int b) {
//        int max;
//
//        if (a > b) max = a;
//        else max = b;
//
//        max = (a > b) ? a : b;
//
//        return max;
//    }
}
