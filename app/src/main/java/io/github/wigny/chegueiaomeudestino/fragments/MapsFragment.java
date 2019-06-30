package io.github.wigny.chegueiaomeudestino.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

import io.github.wigny.chegueiaomeudestino.R;

import static android.app.Activity.RESULT_OK;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.LATITUDE;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.LONGITUDE;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMapControls;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMapTraffic;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMapType;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.getMarkerPosition;
import static io.github.wigny.chegueiaomeudestino.classes.Utils.setMarkerPosition;

public class MapsFragment extends Fragment implements GoogleMap.OnMapClickListener,
        OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener {

    private GoogleMap mMap;
    private Marker marker;
    private LatLng latLng;
    private FloatingActionButton floatBtn;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private static String FLOAT_BTN_BACK = "back";
    private static String FLOAT_BTN_SEARCH = "search";

    OnHeadlineSelectedListener mCallback;

    public void setOnHeadlineSelectedListener(Activity activity) {
        mCallback = (OnHeadlineSelectedListener) activity;
    }

    public interface OnHeadlineSelectedListener {
        void onOpenFragment(int id);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        MapView map = view.findViewById(R.id.mapView) ;
        map.onCreate(savedInstanceState);
        map.getMapAsync(this);
        map.onResume();

        floatBtn = view.findViewById(R.id.floatBtn);

        floatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(floatBtn.getTag() == FLOAT_BTN_BACK) mCallback.onOpenFragment(R.id.nav_main);
                if(floatBtn.getTag() == FLOAT_BTN_SEARCH) placeSearch();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        latLng = getMarkerPosition(getContext());
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (marker != null) {
            marker.remove();
        }
        addMarker(latLng);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
            setFloatBtn(FLOAT_BTN_SEARCH);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(getMapControls(getContext()));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setTrafficEnabled(getMapTraffic(getContext()));
        mMap.setMapType(getMapType(getContext()));
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraMoveStartedListener(this);

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(getMapControls(getContext()));
        }

        if (latLng != null) addMarker(latLng);

        setFloatBtn(FLOAT_BTN_SEARCH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(Objects.requireNonNull(getContext()), data);

                if (marker != null) {
                    marker.remove();
                }

                addMarker(place.getLatLng());
            }
        }
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        marker = mMap.addMarker(options);
        setMarkerPosition(getContext(), latLng);

        if (getArguments() != null) {
            double latitude = Double.parseDouble(Objects.requireNonNull(getArguments().getString(LATITUDE)));
            double longitude = Double.parseDouble(Objects.requireNonNull(getArguments().getString(LONGITUDE)));

            animateCamera(new LatLng(latitude, longitude), 13);
            setArguments(null);
        } else animateCamera(latLng, 15);

        setFloatBtn(FLOAT_BTN_BACK);
    }

    public void animateCamera(LatLng latLng, int z) {
        float zoom;
        if (mMap.getCameraPosition().zoom < 15) zoom = z;
        else zoom = mMap.getCameraPosition().zoom;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .bearing(0)
                .tilt(30)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void placeSearch() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(Objects.requireNonNull(getActivity()));
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException ignored) {
        }
    }

    private void setFloatBtn(String s) {
        floatBtn.setTag(s);
        if(s.equals(FLOAT_BTN_BACK)) {
            floatBtn.setImageResource(R.drawable.ic_button_back);
        } else if(s.equals(FLOAT_BTN_SEARCH)) {
            floatBtn.setImageResource(R.drawable.ic_button_search);
        }
    }
}
