package io.github.wigny.chegueiaomeudestino.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;

import io.github.wigny.chegueiaomeudestino.R;

import static io.github.wigny.chegueiaomeudestino.classes.Utils.KEY_REQUESTING_LOCATION_UPDATES;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMinimumDistance;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getRepeatDays;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getTimeHour;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getTimeMinutes;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.requestingLocationUpdates;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setMinimumDistance;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setRepeatDays;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setTime;

public class MainFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    OnHeadlineSelectedListener mCallback;

    public void setOnHeadlineSelectedListener(Activity activity) {
        mCallback = (OnHeadlineSelectedListener) activity;
    }

    public interface OnHeadlineSelectedListener {
        void onOpenFragment(int id);
    }

    private Button btnChoiceTime;
    private Button btnRepeat;

    public MainFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        btnChoiceTime = view.findViewById(R.id.choiceTime);
        btnRepeat = view.findViewById(R.id.choiceDaysRepeat);
        Spinner spinner = view.findViewById(R.id.minimumDistance);
        Button btnOpenMap = view.findViewById(R.id.openMap);
//        TextView textView = view.findViewById(R.id.txtViewInfo);

        getButtonText();

        btnChoiceTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE) + 1;

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        saveTime(hourOfDay, minutes);
                        setButtonText(hourOfDay, minutes);
                    }
                }, currentHour, currentMinute, false);

                timePickerDialog.show();
            }
        });

        spinner.setSelection(getMinimumDistance(getContext()));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setMinimumDistance(getContext(), position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                String[] days = getResources().getStringArray(R.array.daysOfWeek);

                final boolean[] booleans = new boolean[7];
                
                for (int i=0; i<=6; i++) {
                    booleans[i] = getRepeatDays(getContext(), i);
                }

                builder.setMultiChoiceItems(days, booleans, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedItemId, boolean isChecked) {
                        setRepeatDays(getContext(), selectedItemId, isChecked);
                    }
                });

                builder.setTitle(getString(R.string.dialog_repeat_days_title));

                builder.setPositiveButton(getString(android.R.string.ok), null);

                builder.setNegativeButton(getString(R.string.dialog_reset), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for( int i = 0; i <= 6; i++) {
                            setRepeatDays(getContext(), i, false);
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onOpenFragment(R.id.nav_map);
            }
        });

//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
//            }
//        });

        return view;
    }

    private void saveTime(int hourOfDay, int minutes) {
        setTime(getContext(), hourOfDay, minutes);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setButtonText(int hourOfDay, int minutes) {
        String amPm;

        if (hourOfDay > 12) {
            hourOfDay -= 12;
            amPm = "PM";
        } else if(hourOfDay == 12) amPm = "PM";
        else amPm = "AM";

        btnChoiceTime.setText(String.format("%02d:%02d", hourOfDay, minutes) + " " +amPm);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void getButtonText() {
        int hourOfDay = getTimeHour(getContext());
        int minutes = getTimeMinutes(getContext());

        if(hourOfDay < 25 && minutes < 61) {
            String amPm;

            if (hourOfDay > 12) {
                hourOfDay -= 12;
                amPm = "PM";
            } else if(hourOfDay == 12) amPm = "PM";
            else amPm = "AM";

            btnChoiceTime.setText(String.format("%02d:%02d", hourOfDay, minutes) + " " +amPm);
        }
    }

    @Override
    public void onStop() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onStart() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);

        setButtonsState(requestingLocationUpdates(getContext()));
        super.onStart();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            btnChoiceTime.setEnabled(false);
            btnRepeat.setEnabled(false);
        } else {
            btnChoiceTime.setEnabled(true);
            btnRepeat.setEnabled(true);
        }
    }
}