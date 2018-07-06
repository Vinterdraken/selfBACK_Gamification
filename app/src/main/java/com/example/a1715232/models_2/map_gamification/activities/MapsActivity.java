package com.example.a1715232.models_2.map_gamification.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.a1715232.models_2.R;
import com.example.a1715232.models_2.map_gamification.controller.GPXController;
import com.example.a1715232.models_2.map_gamification.models.Route;
import com.example.a1715232.models_2.map_gamification.models.User;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;

    private String gpxFileName;
    private int routeIndex;
    private Route route;

    private Marker userMarker;
    private Polyline userPolyline;

    private TextView textPercent;
    private TextView textDistance;
    private TextView textSteps;
    private EditText editText;

    private int stepCounter;

    ///////////////
    // FUNCTIONS //
    ///////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getExtras(); // Get fileName of the route selected

        setRouteDependingOnGPXStructure(); // Set route

        if(!hasUserAlreadyStartThisRoute()){
            User.setPosition(route.getWayPoints().get(0));
        }

        setActivityComponent(); // Init text views and edit text objects

        setTextViewsValues(); // Set default values of text views

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        displayRouteAndUser();

        showRouteCompleteDialogIfRouteIsComplete();

        setCameraOnUser(true); // set default zoom

        StepUpdater();
    }

    // INITIALIZATION //
    /**
     * Get the information given by the previous activity
     */
    private void getExtras(){
        Bundle extra = getIntent().getExtras();

        if(extra != null) {
            if (extra.getString("fileName") != null)
                gpxFileName = extra.getString("fileName");
            else
                Log.e("DEBUG", "extra.getString(\"fileName\") is null" );

            if (extra.getString("index") != null)
                routeIndex = Integer.parseInt(extra.getString("index"));
            else
                Log.e("DEBUG", "extra.getInt(\"index\") is null" );
        }

    }

    /**
     * As there is 3 different way to structure a GPX file
     * This function will try to parse the file using three different methods
     */
    private void setRouteDependingOnGPXStructure(){
        //Parsing for TrackPoint
        route = new Route(GPXController.getTrackPointsFromGPX(this, gpxFileName), routeIndex);

        //Parsing for WayPoints
        if(route.getWayPoints().size() == 0)
            route = new Route(GPXController.getWayPointsFromGPX(this, gpxFileName), routeIndex);

        //Parsing for RoutePoint
        if(route.getWayPoints().size() == 0)
            route = new Route(GPXController.getRoutePointsFromGPX(this, gpxFileName), routeIndex);

    }

    private void setActivityComponent() {

        textPercent = findViewById(R.id.textPercent);
        textDistance = findViewById(R.id.textDistance);
        textSteps = findViewById(R.id.textSteps);

        TextView textSelectedRouteName = findViewById(R.id.textSelectedRouteName);
        textSelectedRouteName.setText(
                gpxFileName.substring(0,gpxFileName.length() - 4)
        );

        TextView routeLength = findViewById(R.id.textRouteTotalLength);
        routeLength.setText(
                String.format(
                        "%s m",
                        Integer.toString((int) route.totalLengthOfTheRoute())
                )
        );

        TextView routeSteps = findViewById(R.id.textRouteTotalSteps);
        routeSteps.setText(
                String.format(
                        "%s steps",
                        Integer.toString(
                                User.distanceToStep(route.totalLengthOfTheRoute())
                        )
                )
        );

        // new steps done amount
        editText = findViewById(R.id.editNewStepAmount);

        // Confirm (change of steps done amount) Button
        Button confirmButton = findViewById(R.id.confirmNewStepAmount);
        confirmButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateActivityByEditTextView();
                    }
                }
        );

        //Increase Button
        Button increaseButton = findViewById(R.id.increaseStepAmountButton);
        increaseButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateActivityByIncreaseButton();
                    }
        });

        //Decrease Button
        Button decreaseButton = findViewById(R.id.decreaseStepAmountButton);
        decreaseButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateActivityByDecreaseButton();
                    }
                }
        );

        // Back to the list button (right hand up corner)
        ImageButton imageButton = findViewById(R.id.back);
        imageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goBackToListActivity();
                    }
        });
    }
    private void setTextViewsValues(){
        if(User.getStepsDone().get(routeIndex) == 0) {
            textPercent.setText(R.string.default_text_percentTextView);
            textDistance.setText(R.string.default_text_distanceTextView);
            textSteps.setText(R.string.default_text_stepsTextView);
        }
        else{
            if(haveTheUserReachTheEndOfTheRoute())
                textPercent.setText(R.string.textPercent_when_route_complete);
            else{
                int progress = (int)(
                        (User.distanceDone(routeIndex) / route.totalLengthOfTheRoute()) * 100
                );

                textPercent.setText(
                        String.format(
                                "%s%%",
                                Integer.toString(progress)
                        )
                );
            }

            textDistance.setText(
                    String.format(
                            "%s m",
                            Integer.toString((int) User.distanceDone(routeIndex))
                    )
            );

            textSteps.setText(
                    String.format(
                            "%s steps",
                            Integer.toString(User.getStepsDone().get(routeIndex))
                    )
            );

            editText.setText("");
        }
    }

    private boolean hasUserAlreadyStartThisRoute(){
        return User.getStepsDone().get(routeIndex) != 0;
    }

    // UPDATE //
    private void updateActivity(){
        route.update();

        updateUserMarkerAndUserPolyline();

        setTextViewsValues();

        setCameraOnUser(false); // here false mean that we won't use the default zoom, but keeping the current zoom level

        showRouteCompleteDialogIfRouteIsComplete();
    }
    private void setCameraOnUser(boolean useTheDefaultZoom){

        if(!useTheDefaultZoom){
            float zoomLevel = mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(User.getPosition(), zoomLevel));
        }
        else
            //default zoom value
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(User.getPosition(), 10));
    }

    /**
     * Function called when the Confirm button is clicked
     * Change the User steps done on this route depending of the content of the EditText
     */
    private void updateActivityByEditTextView(){

        if(editText.getText().toString().equals(""))
            User.getStepsDone().set(routeIndex, 0);
        else
            User.getStepsDone().set(
                    routeIndex,
                    Integer.parseInt(editText.getText().toString())
            );

        updateActivity();
    }

    /**
     * Function called when the Increase button (+) is clicked
     */
    private void updateActivityByIncreaseButton(){
        User.increaseCurrentStepsDoneAmount(routeIndex);

        updateActivity();
    }
    /**
     * Function called when the Decrease button (-) is clicked
     */
    private void updateActivityByDecreaseButton(){
        User.decreaseCurrentStepsDoneAmount(routeIndex);

        updateActivity();
    }

    private boolean haveTheUserReachTheEndOfTheRoute(){
        return User.distanceToStep(route.totalLengthOfTheRoute()) <= User.getStepsDone().get(routeIndex);
    }
    private AlertDialog createRouteCompleteDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("ROUTE DONE !");
        builder.setMessage(
        "Congratulation you have reach the amount of steps needed to complete this Trail for real!"
        );
        builder.setPositiveButton(
                "Back to the Trail list",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goBackToListActivity();
                    }
                }
        );
        builder.setNeutralButton(
                "Close",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
        );

        return builder.create();
    }
    private void showRouteCompleteDialogIfRouteIsComplete(){
        //If route is complete
        if(haveTheUserReachTheEndOfTheRoute()){

            final AlertDialog endOfTheRoute = createRouteCompleteDialog();

            endOfTheRoute.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    endOfTheRoute.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.DKGRAY);
                }
            });

            endOfTheRoute.show();
        }
    }

    // DRAWERS //
    private void displayRouteAndUser(){

        // Start point //
        drawMarker(
            route.getWayPoints().get(0),
            "Start",
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        );

        // End point //
        drawMarker(
            route.getWayPoints().get(route.getWayPoints().size()-1),
            "End",
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        );

        // Route Polyline //
        drawPolyline(route.getWayPoints(), Color.RED, 15);

        // User Stuff //
        drawUserMarkerAndUserPolyline();

        // Handle the graphical problem //
        graphicalProblemHandler();
    }

    private void drawUserMarkerAndUserPolyline(){
        // User marker //
        userMarker =
            drawMarker(
                User.getPosition(),
                "You",
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            );

        // User Polyline //
        userPolyline =
            drawPolyline(
                route.getRoutesPartDone(),
                Color.rgb(0,127,255),
                10
            );
    }
    private void updateUserMarkerAndUserPolyline(){
        removeUserMarkerAndUserPolyline();

        drawUserMarkerAndUserPolyline();
    }
    private void removeUserMarkerAndUserPolyline(){

        userMarker.remove();
        userPolyline.remove();
    }

    /**
     * Draw and return a Google Map's Marker
     * Keep the Marker in a variable will allow us to remove it later (for update)
     */
    private Marker drawMarker(LatLng position, String title, BitmapDescriptor icon){

        return mMap.addMarker(
                    new MarkerOptions()
                        .position(position)
                        .title(title)
                        .icon(icon)
        );

    }
    /**
     * Draw and return a Google Map's Polyline
     * Keep the Polyline in a variable will allow us to remove it later (for update)
     */
    private Polyline drawPolyline(ArrayList<LatLng> wayPointsList, int color, int width){

        return mMap.addPolyline(
                    new PolylineOptions()
                        .addAll(wayPointsList)
                        .color(color)
                        .width(width)
        );

    }

    // GRAPHICAL PROBLEM HANDLER //
    /**
     * There is a graphical problem when you display the routes
     * The problem is that when you open a route, it seem to first display it using the amount of steps made by the user on the route of index = 0
     * I've checked if the right index value was passing through the activities and yes because the TextViews are displaying the correct data
     * Then I've checked if I made mistake on the drawing functions, but here again nothing seems wrong
     * On top of that when you update the route with the 'UPDATE' button that display the right stuff, so that definitely don't come from the drawing functions
     * So I've use kind of the same function which is trigger by a click on the 'UPDATE' button to handle it on the onMapReady() function, that works now
     * But the problem still there if you remove the handler
     * And I still don't know where it's come from...
     */
    private void graphicalProblemHandler(){

        User.getStepsDone().set(routeIndex, User.getStepsDone().get(routeIndex));

        route.update();

        updateActivity();
    }

    // CHANGE OF ACTIVITY //
    private void goBackToListActivity(){
        Intent intent = new Intent(this, RoutesListActivity.class);
        startActivity(intent);
    }

    // SAVING USER DATA //
    @Override
    protected void onStop() {
        super.onStop();
        saveUserData();
        Log.e("SAVE", "by onStop");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveUserData();
        Log.e("SAVE", "by onDestroy");
    }
    @Override
    protected void onPause() {
        super.onPause();
        saveUserData();
        Log.e("SAVE", "by onPause");
    }

    /** Save User's data (steps done on each route & height) using DefaultSharedPreferences */
    private void saveUserData() {

        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = data.edit();

        for (int i = 0; i < User.getStepsDone().size(); ++i) {
            editor.putInt("steps_Route_" + i, User.getStepsDone().get(i));
        }

        editor.putInt("User_Height", User.getHeightInCentimeters());

        editor.apply();

        Log.e("DATA", "Data saved");

    }

    // STEP DETECTOR //
    private void StepUpdater(){

        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);


        mSensorManager.registerListener(
                new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {

                        User.getStepsDone().set(
                                routeIndex,
                                (User.getStepsDone().get(routeIndex) + 1)
                                );

                        updateActivity();
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                        Log.e("Sensor", "Accuracy Changed");
                    }
                },
                mSensor,
                30000
        );
    }

}

/*
    //SUB CLASS TO GET AN REAL TIME UPDATE OF STEP FROM THE DATABASE //
    private class StepUpdater {

        private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
        private static final String TAG = "Daily Total Steps ";

        private Context mContext;

        private int routeIndex;
        private int gDailyStepCount = 0;

        // CONSTRUCTOR //
        private StepUpdater(int routeIndex, Context context){
            io.realm.Realm.init(context);

            this.routeIndex = routeIndex;
            mContext = context;

            FitnessOptions fitnessOptions = createFitnessOptionsToReadStepCountDelta();
            checkPermissions(fitnessOptions);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data){
            if (resultCode == android.app.Activity.RESULT_OK) {
                if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                    getDailyTotalSteps();
                }
            }

        }

        // FUNCTIONS //
        private FitnessOptions createFitnessOptionsToReadStepCountDelta(){
            return FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .build();
        }
        private void checkPermissions(FitnessOptions fitnessOptions){
            if (!GoogleSignIn.hasPermissions(
                    GoogleSignIn.getLastSignedInAccount(mContext), fitnessOptions)
                    ) {

                GoogleSignIn.requestPermissions(
                        MapsActivity.this,
                        REQUEST_OAUTH_REQUEST_CODE,
                        GoogleSignIn.getLastSignedInAccount(mContext),
                        fitnessOptions
                );
            }
            else{
                getDailyTotalSteps();
            }
        }

        private Task getDailyTotalSteps() {

            return
            Fitness.getHistoryClient(
                MapsActivity.this,
                GoogleSignIn.getAccountForScopes(
                    mContext,
                    new Scope(Scopes.FITNESS_ACTIVITY_READ)
                )
            )
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener(
                new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet DataSet) {
                       dumpDataSet(DataSet);
                    }
                }
            )
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("READ", "Failed to read data");
                    }
                }
            );
        }

        private void dumpDataSet(DataSet dataSet){
            Log.e(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
            DateFormat dateFormat = getTimeInstance();

            for (DataPoint dp : dataSet.getDataPoints()) {
                Log.e(TAG, "Data point:");
                Log.e(TAG, "\tType: " + dp.getDataType().getName());
                Log.e(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                Log.e(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                for (Field field : dp.getDataType().getFields()) {
                    Log.e(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                }
            }
        }
    }
*/
