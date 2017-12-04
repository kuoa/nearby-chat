package pro.postaru.sandu.nearbychat.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.constants.Constant;
import pro.postaru.sandu.nearbychat.models.UserProfile;
import pro.postaru.sandu.nearbychat.utils.DatabaseUtils;

public class MapViewFragment extends Fragment {
    private Map<String, Marker> stringMarkerMap;
    private MapView mMapView;
    private GoogleMap googleMap;
    private GeoFire geoFire;
    private String userId;
    private Circle circle;
    private final GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            Log.d(Constant.NEARBY_CHAT, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
            LatLng latLng = new LatLng(location.latitude, location.longitude);
            UserProfile tempUserProfile = new UserProfile();
            tempUserProfile.setId(key);
            Marker marker = addMarker(latLng, tempUserProfile);

            //retrieve the user from the database with an async task

            DatabaseUtils.getUserProfileReferenceById(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserProfile profile = dataSnapshot.getValue(UserProfile.class);
                    if (profile != null) {
                        marker.setTitle(profile.getUserName());
                        marker.setSnippet(profile.getBio());
                        marker.showInfoWindow();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(Constant.NEARBY_CHAT, "onCancelled() called with: databaseError = [" + databaseError + "]");
                    Log.w(Constant.NEARBY_CHAT, "onCancelled: ", databaseError.toException());
                }
            });

            DatabaseUtils.loadProfileImage(key, bitmap -> marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        }

        @Override
        public void onKeyExited(String key) {
            Log.d(Constant.NEARBY_CHAT, String.format("Key %s is no longer in the search area", key));
            Marker marker = stringMarkerMap.remove(key);
            marker.remove();
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            Log.d(Constant.NEARBY_CHAT, String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            Marker marker = stringMarkerMap.get(key);
            LatLng position = new LatLng(location.latitude, location.longitude);
            updateMarkerPosition(marker, position);
            updateCircle(position, key);


        }

        @Override
        public void onGeoQueryReady() {
            Log.d(Constant.NEARBY_CHAT, "onGeoQueryReady: All initial data has been loaded and events have been fired!");
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            System.err.println("There was an error with this query: " + error);
        }
    };

    public static MapViewFragment newInstance() {

        Bundle args = new Bundle();

        MapViewFragment fragment = new MapViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void updateCameraPosition(LatLng position) {
        // For zooming automatically to the location of the marker
        Log.d(Constant.NEARBY_CHAT, "updateCameraPosition() called with: position = [" + position + "]");
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(googleMap.getMaxZoomLevel() - 6).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);

    }

    private void updateMarkerPosition(Marker marker, LatLng position) {
        marker.setPosition(position);
        marker.showInfoWindow();
    }

    private void updateCircle(LatLng center, String key) {
        if (userId.equals(key)) {
            circle.setCenter(center);
            updateCameraPosition(center);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stringMarkerMap = Collections.synchronizedMap(new HashMap<>());


        geoFire = DatabaseUtils.getNewLocationDatabase();
        userId = DatabaseUtils.getCurrentUUID();
        GeoLocation myLocation = new GeoLocation(48.846303, 2.355116);
        geoFire.setLocation(userId, myLocation);

        GeoQuery geoQuery = geoFire.queryAtLocation(myLocation, 0.150);

        geoQuery.addGeoQueryEventListener(geoQueryEventListener);
        //TODO remove when moving
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(mMap -> {

            googleMap = mMap;
            googleMap.setOnMarkerClickListener(marker -> {
                Log.d(Constant.NEARBY_CHAT, "onMarkerClick: " + marker.getTitle());

                //Add  function from the interface
                return false;
            });


        });


        return rootView;
    }

    @NonNull
    private Marker addMarker(LatLng latLng, UserProfile userProfile) {


        //Create maker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(userProfile.getUserName())
                .snippet(userProfile.getBio());

        Marker marker = googleMap.addMarker(markerOptions);
        marker.showInfoWindow();//show the windows

        drawCenteredCircle(latLng, userProfile.getId());
        //Save the reference in the map
        stringMarkerMap.put(userProfile.getId(), marker);
        return marker;
    }

    private void drawCenteredCircle(LatLng latLng, String key) {
        if (userId.equals(key)) {
            circle = googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(150)
                    .strokeColor(Color.CYAN)
                    .fillColor(0x220000FF)
                    .strokeWidth(5));
            updateCameraPosition(latLng);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    //TODO add interface when we want to add listener  for the main activity

}