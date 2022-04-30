package com.lhester.polendey.trikila;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;


public class ActivityPlaces_S extends FragmentActivity implements OnMapReadyCallback, PermissionsListener {

    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;

    private MapboxMap mapboxMap;
    private MapView MyMapView;
    private com.mapbox.mapboxsdk.annotations.Marker passengerMarker, destinationMarkerr;
    // Variables needed to handle location permissions
    private PermissionsManager permissionsManager;
    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 1;
    // Variables needed to listen to location updates
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(ActivityPlaces_S.this);
    private Location mLastLocation;
    private Button btnSetDes, btnDes, btnsat, btnnorm;
    private TextView desti, pick;

    private static final int PLACE_SELECTION_REQUEST_CODE = 56789;

    com.mapbox.mapboxsdk.annotations.Marker marker_des;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(ActivityPlaces_S.this, getString(R.string.access_token));
        setContentView(R.layout.activity_places__s);
        MyMapView = findViewById(R.id.mapView);
        MyMapView.onCreate(savedInstanceState);
        MyMapView.getMapAsync(this);
        desti = findViewById(R.id.txt_destination);
        btnnorm = findViewById(R.id.button2);

        btnsat = findViewById(R.id.button3);
        pick = findViewById(R.id.pickuptext);
        btnsat.setOnClickListener(v -> {
            mapboxMap.setStyle(Style.SATELLITE_STREETS);
        });
        btnnorm.setOnClickListener(v -> {
            mapboxMap.setStyle(Style.MAPBOX_STREETS);
        });
        desti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   mapboxMap.setStyle(Style.MAPBOX_STREETS);
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken("pk.eyJ1IjoibGhlc3RlcjA4IiwiYSI6ImNrbmd2MjNqMTAwb24yc3QxOGVxNjJiMDAifQ.lEm25NigjnVbNSO8kUQuiQ")
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .country("PH")
                                //.addInjectedFeature(home)
                                //.addInjectedFeature(work)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(ActivityPlaces_S.this);
                startActivityForResult(intent, PLACE_SELECTION_REQUEST_CODE);
            }
        });

        btnSetDes = findViewById(R.id.btnSetDes);
        btnSetDes.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View v) {

                com.mapbox.mapboxsdk.geometry.LatLng latLng = mapboxMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
                if (destination_Marker != null) {
                    destination_Marker.remove();
                }
                //  destination_Marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Destination"));
                Common_Variables.customer_destination = new com.mapbox.mapboxsdk.geometry.LatLng(latLng);
                Geocoder geocoder2 = new Geocoder(ActivityPlaces_S.this, Locale.getDefault());
                String pick_address = "";

                try {
                    List<Address> addresses = geocoder2.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
                    pick_address = addresses.get(0).getAddressLine(0);
                    desti.setText(pick_address);

                    if (Common_Variables.customer_pickuploaction != null && Common_Variables.customer_destination != null) {
                        float d = 0;
                        float fare;
                        d = NewRide.computeDistance(Common_Variables.customer_pickuploaction, Common_Variables.customer_destination);
                        NewRide.distance.setText(String.format("%.02f", d));

                        fare = mf + (d - 1) * af;
                        if (fare < mf) {
                            fare = mf;
                        }
                        String strfare = String.format("%.2f", fare);
                        NewRide.fares.setText(strfare);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                NewRide.to.setText(pick_address);
                Common_Variables.setroute = 1;
                finish();
            }
        });
        getFare();

    }


    // Check for permission to access Location
    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(ActivityPlaces_S.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        ActivityCompat.requestPermissions(
                ActivityPlaces_S.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    final int REQ_PERMISSION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkPermission())
                        MyMapView.getMapAsync(ActivityPlaces_S.this);

                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ActivityPlaces_S.this, logincustomer.class);
                    startActivity(intent);
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //  mGoogleApiClient.disconnect();
        //   LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        // OnlineDrivers.=null ;
        NewRide.to.setText("");
        finish();
    }

    Marker destination_Marker;
    private String geojsonSourceLayerId = "geojsonSourceLayerId2";
    private String symbolIconId = "symbolIconId";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_SELECTION_REQUEST_CODE) {
            double lat = 0, lng = 0;
            LatLng LATLNG = null;
            if (resultCode == RESULT_OK) {
                CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

                if (mapboxMap != null) {
                    Style style = mapboxMap.getStyle();
                    if (style != null) {
                        GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                        if (source != null) {
                            source.setGeoJson(FeatureCollection.fromFeatures(
                                    new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                        }
                        lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                        lng = ((Point) selectedCarmenFeature.geometry()).longitude();
                        LATLNG = new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng);

                        Location loc1 = new Location("");
                        loc1.setLatitude(mLastLocation.getLatitude());
                        loc1.setLongitude(mLastLocation.getLongitude());

                        Location loc2 = new Location("");
                        loc2.setLatitude(lat);
                        loc2.setLongitude(lng);

                        if ((loc1.distanceTo(loc2) / 1000) <= 50) {
                            btnSetDes.setEnabled(true);
                            lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                            lng = ((Point) selectedCarmenFeature.geometry()).longitude();
                            LATLNG = new LatLng(lat, lng);
// Move map camera to the selected location
                            mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(LATLNG, 17));
                        } else {
                            Toast.makeText(this, "Exceeding distance Limit", Toast.LENGTH_SHORT).show();
                            btnSetDes.setEnabled(false);
                            NewRide.to.setText("");
                            //      Common_Variables.customer_destination = null;
                        }
                    }
                }
                Geocoder geocoder = new Geocoder(ActivityPlaces_S.this, Locale.getDefault());
                String address = "";
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                desti.setText(address);
                Common_Variables.customer_destination = LATLNG;
            }

            //    destination_Marker = mMap.addMarker(new MarkerOptions().position(Common_Variables.customer_destination).title("Selected Destination"));
            //  mMap.moveCamera(CameraUpdateFactory.newLatLng(Common_Variables.customer_destination));
            //  mMap.animateCamera(CameraUpdateFactory.zoomTo(20));

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(ActivityPlaces_S.this, status.getStatusMessage(), Toast.LENGTH_LONG);
        } else if (resultCode == RESULT_CANCELED) {
        }
        return;
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        ActivityPlaces_S.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        addDestinationIconSymbolLayer(style);
                        Double lat = OnlineDrivers.MyLocation.latitude;
                        Double lng = OnlineDrivers.MyLocation.longitude;
                        com.mapbox.mapboxsdk.geometry.LatLng LATLNG = new com.mapbox.mapboxsdk.geometry.LatLng(lat, lng);
                        mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(LATLNG, 15));
                    }
                });
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(ActivityPlaces_S.this)) {

// Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

// Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(ActivityPlaces_S.this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

// Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

// Enable to make component visible
            //     locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            // locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(ActivityPlaces_S.this);
            permissionsManager.requestLocationPermissions(ActivityPlaces_S.this);
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(ActivityPlaces_S.this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }


    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(ActivityPlaces_S.this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<ActivityPlaces_S> activityWeakReference;

        MainActivityLocationCallback(ActivityPlaces_S activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            ActivityPlaces_S activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

// Create a Toast which displays the new location's coordinates
                mLastLocation = location;
                com.mapbox.mapboxsdk.geometry.LatLng latLng = new com.mapbox.mapboxsdk.geometry.LatLng(location.getLatitude(), location.getLongitude());
            }

        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            ActivityPlaces_S activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyMapView.onStart();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MyMapView.onLowMemory();
    }

    private float mf = 0, af = 0;

    private void getFare() {
        DatabaseReference databaseReferenceFare = FirebaseDatabase.getInstance().getReference().child("Fare").child("current_fare");
        databaseReferenceFare.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("minimum_fare") != null) {
                        mf = Float.parseFloat(map.get("minimum_fare").toString());
                    }
                    if (map.get("additional_fare") != null) {
                        af = Float.parseFloat(map.get("additional_fare").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

