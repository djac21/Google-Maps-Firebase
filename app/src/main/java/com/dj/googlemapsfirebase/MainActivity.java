package com.dj.googlemapsfirebase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
import static com.dj.googlemapsfirebase.R.id.map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private FirebaseAuth auth;
    private static final int SIGN_IN = 0;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    ChildEventListener mChildEventListener;
    AutoCompleteTextView searchText, addItem;
    EditText snippet_added;
    double add_longitude,add_latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            //Toast.makeText(this, "Current user: " + auth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setProviders(
                            AuthUI.FACEBOOK_PROVIDER,
                            AuthUI.GOOGLE_PROVIDER)
                    .build(), SIGN_IN);
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN) {
            Toast.makeText(this, "Current user: " + auth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
        } else {
            Log.d("AUTH", "NOT AUTHENTICATED");
        }
        switch (requestCode) {
            case 1:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        /*Location location;
                        mLastLocation = location;

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));*/
                        //onLocationChanged(mLastLocation);

                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle(R.string.no_location)
                                .setMessage(R.string.no_location_message)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

  /*  @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
        /* if (mGoogleApiClient != null) {
            locationRequest();
        }*/
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();

            locationRequest();
        }
    }

    public void locationRequest() {

        LocationRequest locationRequest = LocationRequest.create();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(
                                    MainActivity.this, 1);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

       /* if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.permission_denied)
                            .setCancelable(false)
                            .setMessage(R.string.permission)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            }

        }
    }

    public void addItem() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        final View promptsView = layoutInflater.inflate(R.layout.add_item, null);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Geocoder coder = new Geocoder(MainActivity.this);
                try {
                    ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocationName((String) place.getAddress(), 10);
                    for(Address add : addresses){
                        add_longitude = add.getLongitude();
                        add_latitude = add.getLatitude();
                    }
                } catch (IOException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptsView);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        final ArrayAdapter<String> autoComplete = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
        database.child("Item List").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()) {
                    String title = suggestionSnapshot.child("item").getValue(String.class);
                    autoComplete.add(title);
                }
            }

            @Override

            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addItem = (AutoCompleteTextView) promptsView.findViewById(R.id.add_item);
        addItem.setAdapter(autoComplete);
        addItem.setThreshold(0);

        addItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                String selection = (String) parent.getItemAtPosition(position);
                addItem.setText(selection);
                addItem.setSelection(addItem.getText().length());
            }
        });

        final EditText addPrice = (EditText) promptsView.findViewById(R.id.add_price);

        alertDialogBuilder
                .setTitle("Add Item")
                .setPositiveButton("Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String item_child = addItem.getText().toString();
                                String addPriceString = addPrice.getText().toString();

                                try {
                                    addItem = (AutoCompleteTextView) promptsView.findViewById(R.id.add_item);
                                    snippet_added = (EditText) promptsView.findViewById(R.id.add_snippet);

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    String key = database.getReference(item_child).push().getKey();

                                    Double addPriceDouble = Double.parseDouble(addPriceString);
                                    String formattedPrice = String.format("%.2f", addPriceDouble);
                                    Double formattedDouble = Double.valueOf(formattedPrice);

                                    FirebaseMarker add_items_firebase = new FirebaseMarker();
                                    add_items_firebase.setTitle(addItem.getText().toString());
                                    add_items_firebase.setSnippet(snippet_added.getText().toString());
                                    add_items_firebase.setLatitude(add_latitude);
                                    add_items_firebase.setLongitude(add_longitude);
                                    add_items_firebase.setPrice(formattedDouble);

                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put(key, add_items_firebase.toFirebaseObject());
                                    database.getReference(item_child).updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                Toast.makeText(MainActivity.this, "Item Added!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    Toast.makeText(MainActivity.this, "Error, no changes made", Toast.LENGTH_LONG).show();
                                }
                            }})
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {

        new AlertDialog.Builder(this)
                .setTitle("Confirm Price")
                .setMessage(marker.getSnippet())
                .setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(MainActivity.this, "Price Confirmed", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("Change Price",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                                View promptsView = layoutInflater.inflate(R.layout.change_price, null);

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                alertDialogBuilder.setView(promptsView);

                                final EditText editText = (EditText) promptsView.findViewById(R.id.price);

                                alertDialogBuilder
                                        .setTitle("Change Price")
                                        .setPositiveButton("Change",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        final String search_string = searchText.getText().toString();

                                                        DatabaseReference mItemRef = FirebaseDatabase.getInstance().getReference(search_string);

                                                        mChildEventListener = mItemRef.addChildEventListener(new ChildEventListener() {
                                                            @Override
                                                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                                FirebaseMarker marker = dataSnapshot.getValue(FirebaseMarker.class);
                                                               /* Double price = marker.getPrice();
                                                                String title = marker.getTitle();
                                                                String changePrices = Double.toString(price);*/
                                                                String change_string = editText.getText().toString();

                                                                try {
                                                                    Double changePrice = Double.parseDouble(change_string);
                                                                    String change = String.format("%.2f", changePrice);
                                                                    Toast.makeText(MainActivity.this, "The price is was changed to $" + change, Toast.LENGTH_LONG).show();

                                                                } catch (NumberFormatException e) {
                                                                    e.printStackTrace();
                                                                    Toast.makeText(MainActivity.this, "No changes made", Toast.LENGTH_LONG).show();
                                                                }
                                                            }

                                                            @Override
                                                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                                            }

                                                            @Override
                                                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                                                            }

                                                            @Override
                                                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                        //changePrice(marker);
                                                    }
                                                })
                                        .setNegativeButton("Cancel",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        final SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setQueryHint("Search An Item");
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | TYPE_TEXT_FLAG_CAP_SENTENCES);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        final ArrayAdapter<String> autoComplete = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
        database.child("Item List").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()) {
                    String title = suggestionSnapshot.child("item").getValue(String.class);
                    autoComplete.add(title);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        searchText = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setAdapter(autoComplete);
        searchText.setThreshold(1);

        searchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                String selection = (String) parent.getItemAtPosition(position);
                searchText.setText(selection);
                searchText.setSelection(searchText.getText().length());
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                String search_string = searchText.getText().toString();
                DatabaseReference mItemRef = FirebaseDatabase.getInstance().getReference(search_string);
//                searchView.clearFocus();

                mMap.clear();
                mChildEventListener = mItemRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        FirebaseMarker marker = dataSnapshot.getValue(FirebaseMarker.class);
                        Double latitude = marker.getLatitude();
                        Double longitude = marker.getLongitude();
                        Double price = marker.getPrice();
                        String snippet = marker.getSnippet();
                        String title = marker.getTitle();
                        String iconPrice = Double.toString(price);

                        IconGenerator iconGenerator = new IconGenerator(getBaseContext());
                        iconGenerator.setStyle(IconGenerator.STYLE_BLUE);
                        Bitmap iconBitmap = iconGenerator.makeIcon(iconPrice);

                        LatLng location = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(title)
                                .snippet("Location: " + snippet + "\n" + "Price: $" + price)
                                .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

                        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                            @Override
                            public View getInfoWindow(Marker arg0) {
                                return null;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                Context context = getApplicationContext();
                                LinearLayout info = new LinearLayout(context);
                                info.setOrientation(LinearLayout.VERTICAL);

                                TextView title = new TextView(context);
                                title.setTextColor(Color.BLACK);
                                title.setGravity(Gravity.CENTER);
                                title.setTypeface(null, Typeface.BOLD);
                                title.setText(marker.getTitle());

                                TextView snippet = new TextView(context);
                                snippet.setTextColor(Color.GRAY);
                                snippet.setText(marker.getSnippet());

                                info.addView(title);
                                info.addView(snippet);

                                return info;
                            }
                        });

                        mMap.setOnInfoWindowClickListener(MainActivity.this);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                return false;
            }

        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_menu) {
            addItem();
        }

        if (id == R.id.logout_menu) {
            startActivity(new Intent(this, MainActivity.class));
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

