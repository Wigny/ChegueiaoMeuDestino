package io.github.wigny.chegueiaomeudestino.fragments;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import io.github.wigny.chegueiaomeudestino.R;

import static android.app.Activity.RESULT_OK;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.PACKAGE_NAME;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.clearPreferences;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getAutoThemeMode;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMapControls;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMapTraffic;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMapType;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getNotificationSoundIsEnabled;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getNotificationVibrationIsEnabled;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getRingtoneUri;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getThemeId;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getThemePosition;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.requestingLocationUpdates;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setAutoThemeMode;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setMapControls;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setMapTraffic;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setMapType;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setNotificationSoundIsEnabled;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setNotificationVibrationIsEnabled;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setThemePosition;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.toast;

public class SettingsFragment extends Fragment {

    OnHeadlineSelectedListener mCallback;

    public void setOnHeadlineSelectedListener(Activity activity) {
        mCallback = (OnHeadlineSelectedListener) activity;
    }

    public interface OnHeadlineSelectedListener {
        void onRestartActivity();
    }

    private TextView setNotificationSoundContent;
    private Switch notificationVibration;
    private Switch notificationSound;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch themeAuto = view.findViewById(R.id.themeAutomatic);
        final Spinner themeManual = view.findViewById(R.id.themeManual);
        Spinner mapType = view.findViewById(R.id.mapType);
        Switch mapTraffic = view.findViewById(R.id.mapTraffic);
        Switch mapControls = view.findViewById(R.id.mapControls);
        notificationSound = view.findViewById(R.id.notificationSound);
        ConstraintLayout layoutSetSound = view.findViewById(R.id.layoutSetSound);
        final TextView setNotificationSoundTitle = view.findViewById(R.id.setting_notification2);
        setNotificationSoundContent = view.findViewById(R.id.setting_notification2_description);
        notificationVibration = view.findViewById(R.id.notificationVibration);
        ConstraintLayout layoutReset = view.findViewById(R.id.layoutReset);

        themeAuto.setChecked(getAutoThemeMode(getContext()));
        themeManual.setEnabled(!getAutoThemeMode(getContext()));
        themeManual.setSelection(getThemePosition(getContext()));
        mapType.setSelection(getMapType(getContext()));
        mapTraffic.setChecked(getMapTraffic(getContext()));
        mapControls.setChecked(getMapControls(getContext()));
        notificationSound.setChecked(getNotificationSoundIsEnabled(getContext()));
        setNotificationSoundTitle.setEnabled(getNotificationSoundIsEnabled(getContext()));
        setNotificationSoundContent.setEnabled(getNotificationSoundIsEnabled(getContext()));
        setNotificationSoundContent.setText(getNotificationSoundContent());
        notificationVibration.setChecked(getNotificationVibrationIsEnabled(getContext()));

        themeAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(getAutoThemeMode(getContext()) != isChecked) {
                    themeManual.setEnabled(!isChecked);
                    setAutoThemeMode(getContext(), isChecked);
                    if(requestingLocationUpdates(getContext())) toast(getContext(), getString(R.string.toast_theme_reboot));
                    else if(getThemePositionById() != themeManual.getSelectedItemPosition()) {
                        mCallback.onRestartActivity();
                    }
                }
            }
        });

        themeManual.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(getThemePosition(getContext()) != position) {
                    setThemePosition(getContext(), position);
                    if(requestingLocationUpdates(getContext())) toast(getContext(), getString(R.string.toast_theme_reboot));
                    mCallback.onRestartActivity();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(getMapType(getContext()) != position) {
                    setMapType(getContext(), position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mapTraffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMapTraffic(getContext(), isChecked);
            }
        });

        mapControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMapControls(getContext(), isChecked);
            }
        });

        notificationSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(setNotificationSoundState()) {
                    if(getNotificationSoundIsEnabled(getContext()) != isChecked) {
                        setNotificationSoundTitle.setEnabled(isChecked);
                        setNotificationSoundContent.setEnabled(isChecked);
                        setNotificationSoundIsEnabled(getContext(), isChecked);
                    }
                }
            }
        });

        layoutSetSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getNotificationSoundIsEnabled(getContext())) {
                    if(Settings.System.canWrite(getContext())) selectRingtone();
                    else requestPermissions();
                }
            }
        });

        notificationVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(setNotificationVibrationState())
                    setNotificationVibrationIsEnabled(getContext(), isChecked);
            }
        });

        layoutReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearPreferences(getContext());
                mCallback.onRestartActivity();
            }
        });

        return view;
    }

    private boolean setNotificationVibrationState() {
        if(!getNotificationSoundIsEnabled(getContext())) {
            notificationVibration.setChecked(true);
            return false;
        } else return true;
    }

    private boolean setNotificationSoundState() {
        if(!getNotificationVibrationIsEnabled(getContext())) {
            notificationSound.setChecked(true);
            return false;
        } else return true;
    }

    private void selectRingtone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.dialog_choice_ringtone));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getRingtoneUri(getContext()));
        startActivityForResult( intent, 999);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_ALARM, uri);
                setNotificationSoundContent.setText(getNotificationSoundContent());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getNotificationSoundContent() {
        Ringtone ringtone = RingtoneManager.getRingtone(getContext(), getRingtoneUri(getContext()));
        return ringtone.getTitle(getContext());
    }

    private void requestPermissions() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + PACKAGE_NAME));
        startActivity(intent);
    }

    private int getThemePositionById() {
        int theme = getThemeId(getContext());
        int position = -1;

        switch (theme) {
            case R.style.AppTheme_NoActionBar:
                position = 0;
                break;
            case R.style.AppTheme_Dark_NoActionBar:
                position = 1;
                break;
        }

        return position;
    }
}
