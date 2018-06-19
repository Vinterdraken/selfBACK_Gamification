package com.example.a1715232.models_2.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.a1715232.models_2.R;
import com.example.a1715232.models_2.models.User;

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

        routeList = new ArrayList<>();

        try {
            AssetManager assetManager = getAssets();

            String[] routeNames = assetManager.list("gpx");

            Collections.addAll(routeList, routeNames);
        }
        catch (IOException ignored){

        }

        if(doUsersDataNeedToBeInitiate())
            User.init(180, routeList.size());

        displayRoutesList();
    }

    private boolean doUsersDataNeedToBeInitiate(){ return User.getStepsDone() == null; }

    private void displayRoutesList(){

        ListView listView = findViewById(android.R.id.list);

        ArrayList<String> routeNames = new ArrayList<>();

        for(int i = 0; i < routeList.size(); ++i){

                String routeName = routeList.get(i).substring(0, routeList.get(i).length() - 4);
                routeNames.add(routeName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_list_item, R.id.routeNameInList, routeNames);

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
