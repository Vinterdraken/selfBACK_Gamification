package com.example.a1715232.models_2.map_gamification.activities;

import android.app.ListActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.a1715232.models_2.R;
import com.example.a1715232.models_2.map_gamification.models.User;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;

public class RoutesListActivity extends ListActivity {

    private ArrayList<String> routeList;

    ///////////////
    // FUNCTIONS //
    ///////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        getGpxList();

        checkIfUserIsInit();

        displayRoutesList();
    }

    /**
     * Get all the name of the GPX File which are in the assets/gpx folder
     */
    private void getGpxList(){

        routeList = new ArrayList<>();

        try {
            AssetManager assetManager = getAssets();

            String[] routeNames = assetManager.list("gpx");

            Collections.addAll(routeList, routeNames);
        }
        catch (IOException ignored){

        }
    }

    private void checkIfUserIsInit(){
        if(doUserStepsStorageNeedToBeInitiate())
            getUserData();
    }
    private boolean doUserStepsStorageNeedToBeInitiate(){ return User.getStepsDone() == null; }

    /**
     * Load User's data (steps done on each route & height) using DefaultSharedPreferences
     * If there  isn't any user data saved then it will put the value 0 on each route's steps amount
     * and 180 for the height of the user
     * */
    private void getUserData(){

        SharedPreferences data =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
/*
        if(data.contains("User_Height")
            && data.contains("steps_Route_0")
            && data.contains("steps_Route_" + (routeList.size()-1))
        ) {
            Log.e("DATA", "Data found");
        }
        else{
            Log.e("DATA", "Data not found");
        }
*/
        ArrayList<Integer> savedStepsDone = new ArrayList<>();

        for (int i = 0; i < routeList.size(); ++i) {
            savedStepsDone.add(
                    data.getInt("steps_Route_" + i, 0)
            );
        }

        User.setStepsDone(savedStepsDone);

        User.setHeightInCentimeters(
                data.getInt("User_Height", 180)
        );

    }

    private void displayRoutesList(){

        ListView listView = findViewById(android.R.id.list);

        ArrayList<String> routeNames = new ArrayList<>();

        for(int i = 0; i < routeList.size(); ++i){
                String routeName = routeList.get(i).substring(0, routeList.get(i).length() - 4);
                routeNames.add(routeName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_list_item, R.id.itemRouteName, routeNames);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToMapActivity((int) id);
            }
        });
    }

    public void goToMapActivity(int index){
        Intent intent = new Intent(this, MapsActivity.class);

        intent.putExtra("fileName", routeList.get(index));
        intent.putExtra("index", String.valueOf(index));

        startActivity(intent);
    }

}
