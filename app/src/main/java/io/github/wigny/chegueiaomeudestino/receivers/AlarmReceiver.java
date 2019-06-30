package io.github.wigny.chegueiaomeudestino.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import io.github.wigny.chegueiaomeudestino.services.LocationService;

import static io.github.wigny.chegueiaomeudestino.classes.Utils.getRepeatDays;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getRepeatDaysIsTrue;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getTimeHour;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getTimeMinutes;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setRequestingLocationUpdates;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startForegroundService(new Intent(context, LocationService.class));
    }

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    public void setUpAlarms(Context context, boolean start) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(getRepeatDaysIsTrue(context)) {
            for(int i = 0; i <= 6 ; i++) {
                if(getRepeatDays(context, i)) scheduleAlarm(context,i+1, start);
            }
        } else {
            scheduleAlarm(context,0, start);
        }
    }

    private void scheduleAlarm(Context context, int dayOfWeek, boolean start) {
        pendingIntent = PendingIntent.getBroadcast(context, dayOfWeek, new Intent(context, AlarmReceiver.class), 0);

        final Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, getTimeHour(context));
        calendar.set(Calendar.MINUTE, getTimeMinutes(context));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if(start) {
            if(dayOfWeek == 0) startSimpleAlarm(calendar);
            else startRepetitiveAlarm(calendar, dayOfWeek);
            setRequestingLocationUpdates(context, true);
        } else stopAlarm(context);
    }

    private void stopAlarm(Context context) {
        alarmManager.cancel(pendingIntent);
        setRequestingLocationUpdates(context, false);
        Log.d(TAG, "stopAlarm");
    }

    private void startSimpleAlarm(Calendar calendar) {
        if(calendar.before(Calendar.getInstance())){
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d(TAG, "startSimpleAlarm: " + calendar.getTime());
    }

    private void startRepetitiveAlarm(Calendar calendar, int dayOfWeek) {
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

        if(calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DAY_OF_WEEK, 7);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
        Log.d(TAG, "startRepetitiveAlarm: " + calendar.getTime());
    }
}

