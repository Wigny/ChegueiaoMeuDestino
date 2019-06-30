package io.github.wigny.chegueiaomeudestino.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Objects;

import io.github.wigny.chegueiaomeudestino.R;

public class AboutFragment extends Fragment {
    OnHeadlineSelectedListener mCallback;

    public void setOnHeadlineSelectedListener(Activity activity) {
        mCallback = (OnHeadlineSelectedListener) activity;
    }

    public interface OnHeadlineSelectedListener {
        void onShowLocation(LatLng latLng);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView email = view.findViewById(R.id.email);
        TextView github = view.findViewById(R.id.github);
        TextView facebook = view.findViewById(R.id.facebook);
        TextView whatsapp = view.findViewById(R.id.whatsapp);
        TextView telegram = view.findViewById(R.id.telegram);
        TextView messenger = view.findViewById(R.id.messenger);
        TextView description = view.findViewById(R.id.description);
        TextView copyrights = view.findViewById(R.id.copyrights);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","wignybora@gmail.com", null)));
            }
        });

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Wigny")));
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(runClick("facebook", "wigny.almeida"));
            }
        });

        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(runClick("whatsapp", "5569993304603"));
            }
        });

        telegram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(runClick("telegram", "wigny_almeida"));
            }
        });

        messenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.me/wigny.almeida")));
            }
        });

        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng location = new LatLng(-10.7191038, -62.2549403);
                mCallback.onShowLocation(location);
            }
        });

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        String text = String.format(getResources().getString(R.string.about_copyright), year);
        copyrights.setText(text);

        return view;
    }

    public Intent runClick(String social, String user) {
        String appLink = null, urlLink = null, packageName = null;
        switch (social) {
            case "whatsapp":
                appLink = "whatsapp://send?phone=";
                urlLink = "https://api.whatsapp.com/send?phone=";
                packageName = "com.whatsapp";
                break;
            case "facebook":
                appLink = "fb://profile/";
                urlLink = "https://www.facebook.com/";
                packageName = "com.facebook.katana";
                break;
            case "telegram":
                appLink = "tg://resolve?domain=";
                urlLink = "https://t.me/";
                packageName = "org.telegram.messenger";
                break;
            case "messenger":
                appLink = "http://m.me/";
                urlLink = "https://www.facebook.com/messages/t/";
                packageName = "com.facebook.orca";
                break;
        }

        PackageManager pm = Objects.requireNonNull(getActivity()).getPackageManager();
        Uri uri;
        try {
            pm.getPackageInfo(packageName, 0);
            uri = Uri.parse(Objects.requireNonNull(appLink).concat(user));
        } catch (PackageManager.NameNotFoundException e) {
            uri = Uri.parse(Objects.requireNonNull(urlLink).concat(user));
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

}
