package io.github.wigny.chegueiaomeudestino.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static io.github.wigny.chegueiaomeudestino.classes.Utils.requestingLocationUpdates;

public class StartUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
            new AlarmReceiver().setUpAlarms(context, requestingLocationUpdates(context));
    }
}
