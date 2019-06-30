package io.github.wigny.chegueiaomeudestino.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import io.github.wigny.chegueiaomeudestino.R;
import io.github.wigny.chegueiaomeudestino.classes.Utils;

import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMarkerPosition;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getRepeatDaysIsTrue;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.requestingLocationUpdates;

public class InfoFragment extends Fragment {
    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public TextView currentDistance;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        final TextView destination = view.findViewById(R.id.txtInfoDestination);
        TextView operating = view.findViewById(R.id.txtInfoOperating);
        TextView repeat = view.findViewById(R.id.txtInfoRepeating);
        TextView minimumDistance = view.findViewById(R.id.txtInfoDistanceMinimum);
        currentDistance = view.findViewById(R.id.txtInfoDistanceCurrent);
        TextView resume = view.findViewById(R.id.txtInfoResume);

        destination.setText(getAddress());
        operating.setText(getOperating());
        repeat.setText(getRepeat());
        minimumDistance.setText(getMinimumDistance());
        currentDistance.setText(getDistanceLocation());
        resume.setText(getResume());

        return view;
    }

    public String getResume() {
        return "";
    }

    public String getDistanceLocation() {
        String distance;

        if(requestingLocationUpdates(getContext()))
            distance = String.valueOf(Utils.getDistanceLocation(getContext()));
        else
            distance = getString(R.string.info_unavailable);

        return distance;
    }

    public String getMinimumDistance() {
        String minimumDistance = null;
        switch (Utils.getMinimumDistance(getContext())) {
            case 0:
                minimumDistance = "300 m";
                break;
            case 1:
                minimumDistance = "500 m";
                break;
            case 2:
                minimumDistance = "700 m";
                break;
            case 3:
                minimumDistance = "900 m";
                break;
            case 4:
                minimumDistance = "1 Km";
                break;
            case 5:
                minimumDistance = "1,5 Km";
                break;
        }
        return minimumDistance;
    }

    public String getRepeat() {
        String repeat;

        if(getRepeatDaysIsTrue(getContext())) repeat = getString(android.R.string.yes);
        else repeat = getString(android.R.string.no);

        return repeat;
    }

    public String getOperating() {
        String operating;

        if(requestingLocationUpdates(getContext())) operating = getString(android.R.string.yes);
        else operating = getString(android.R.string.no);

        return operating;
    }

    public String getAddress() {
        String address = null;
        LatLng latLng = getMarkerPosition(getContext());

        if (latLng != null) {
            Geocoder geocoder = new Geocoder(getContext());
            try {
                List<Address> addresses = geocoder.getFromLocation(Objects.requireNonNull(latLng).latitude, latLng.longitude, 1);
                Address obj = addresses.get(0);
                address = obj.getAddressLine(0);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else address = getString(R.string.info_unavailable);

        return address;
    }
}
