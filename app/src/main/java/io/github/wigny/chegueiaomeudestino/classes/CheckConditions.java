package io.github.wigny.chegueiaomeudestino.classes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMarkerPosition;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getTimeHour;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getTimeMinutes;

public class CheckConditions {
    private static boolean checkPermissions(Context context) {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private static boolean getTimeIsSeted(Context context) {
        return (getTimeHour(context) != 25 || getTimeMinutes(context) != 61);
    }

    private static boolean getMarkerIsSeted(Context context) {
        return (getMarkerPosition(context) != null);
    }

    public static String CHECK_PERMISSIONS = "checkPermissions";
    public static String GET_TIME = "getTime";
    public static String GET_MARKER = "getMarker";

    public static String startConditions(Context context) {
        String result = null;

        if(!checkPermissions(context)) result = CHECK_PERMISSIONS;
        if(!getTimeIsSeted(context)) result = GET_TIME;
        if(!getMarkerIsSeted(context)) result = GET_MARKER;

        return result;
    }
}
