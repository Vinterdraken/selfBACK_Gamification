package com.example.a1715232.models_2.map_gamification.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class User {

    private static int heightInCentimeters;

    private static ArrayList<Integer> stepsDone;
    private static LatLng position;

    // GETTERS //
    public static int getHeightInCentimeters(){ return heightInCentimeters; }
    public static ArrayList<Integer> getStepsDone() { return stepsDone; }
    public static LatLng getPosition() { return position; }

    // SETTERS //
    public static void setHeightInCentimeters(int heightInCentimeters){ User.heightInCentimeters = heightInCentimeters; }
    public static void setPosition(LatLng position) {
        User.position = position;
    }
    public static void setStepsDone(ArrayList<Integer> newArray){ User.stepsDone = newArray; }

    ///////////////
    // FUNCTIONS //
    ///////////////

    /**
     * Initialize the height of the user and the 2 ArrayList<>
     * @param heightInCentimeters is the height of the user IN CENTIMETERS
     * @param nbOfRoute Number of routes in the listActivity (use for init the ArrayList<>)
     */
    public static void init(int heightInCentimeters, int nbOfRoute){
        User.heightInCentimeters = heightInCentimeters;

        User.stepsDone = new ArrayList<>();

        for(int i = 0; i < nbOfRoute; ++i){
            User.stepsDone.add(0);
        }

    }

    /**
     * Convert steps done into distance with height of the user
     * @param index is index of the route
     */
    public static double distanceDone(int index){
        return stepsDone.get(index) * ((heightInCentimeters / 100) * 0.414);
    }

    /**
     * Convert a distance into a number of steps
     * @param distance is the distance IN METERS
     */
    public static int distanceToStep(double distance){
        return (int)(distance / ((heightInCentimeters/100) * 0.414));
    }

}
