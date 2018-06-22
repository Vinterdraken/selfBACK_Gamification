package com.example.a1715232.models_2.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Route {

    private int index;

    private ArrayList<LatLng> wayPoints;
    private ArrayList<LatLng> routesPartDone;

    //CONSTRUCTOR
    public Route(ArrayList<LatLng> wayPoints, int index) {
        this.wayPoints = wayPoints;
        this.routesPartDone = pathDoneByUser();
        this.index = index;
    }

    //GETTERS
    public ArrayList<LatLng> getWayPoints() { return wayPoints; }
    public ArrayList<LatLng> getRoutesPartDone() { return routesPartDone; }

    ///////////////
    // FUNCTIONS //
    ///////////////

    /**
     * Update the amount of step of the user and set the new position of the user
     * @param newStepsAmount is the new amount of steps done by the user
     */
    public void update(){

        double distanceLeft = totalLengthOfTheRoute() - User.distanceDone(index);

        if(distanceLeft <= 0){
            User.setPosition(
                    wayPoints.get(
                            wayPoints.size()-1
                    )
            );

            routesPartDone = wayPoints;
        }
        else {
            User.setPosition(
                    getPrecisePositionOfUserMarker(
                        getIndexOfMarkerPrecedingUser(User.distanceDone(index)),
                    distanceLeft
                )
            );

            routesPartDone = pathDoneByUser();
        }

    }

    /**
     * Calculate the total length in meters of the route
     * Using distanceBetween2Markers()
     */
    public double totalLengthOfTheRoute(){
        double totalDistance = 0;

        for(int i = 0; i < wayPoints.size()-1; ++i){
            totalDistance += distanceBetween2Markers(wayPoints.get(i), wayPoints.get(i+1));
        }

        return totalDistance;
    }

    /**
     * Calculate the distance in meters from the start to a given wayPoint of the route
     * @param indexOfLastPointIncluded is the given wayPoint of the route
     */
    private double distanceOfASpecificPortionOfTheRoute(int indexOfLastPointIncluded){
        double distance = 0;

        for(int i = 0; i < indexOfLastPointIncluded; ++i){
            distance += distanceBetween2Markers(wayPoints.get(i), wayPoints.get(i+1));
        }

        return distance;
    }

    /**
     * Calculate the distance between 2 points without taking  in count mountains, seas, etc...
     */
    private double distanceBetween2Markers(LatLng point1, LatLng point2){

        double Lat1 = Math.toRadians(point1.latitude);
        double Long1 = Math.toRadians(point1.longitude);
        double Lat2 = Math.toRadians(point2.latitude);
        double Long2 = Math.toRadians(point2.longitude);

        double tmp = 2 * Math.asin(
                            Math.sqrt(
                                Math.pow(
                                    Math.sin( (Lat1 - Lat2) / 2 ),
                                    2
                                )
                                + Math.cos(Lat1) * Math.cos(Lat2)
                                * Math.pow(
                                    Math.sin( (Long1 - Long2) / 2 ),
                                    2
                                )
                            )
        );

        double distance = tmp * 6366; // * Earth radius, result in kilometers

        return distance * 1000; // result in meters
    }

    /**
     * Route's part which have been done by the user (Graphical part)
     */
    private ArrayList<LatLng> pathDoneByUser(){

        ArrayList<LatLng> pathDone = new ArrayList<>();

        int indexOfMarkerPrecedingUser = getIndexOfMarkerPrecedingUser(User.distanceDone(index));

        if(indexOfMarkerPrecedingUser == wayPoints.size()-1)
            return wayPoints;

        for(int i = 0; i < indexOfMarkerPrecedingUser + 1; ++i){
            pathDone.add(wayPoints.get(i));
        }

        double distanceLeftToDo =
            User.distanceDone(index)
                - distanceOfASpecificPortionOfTheRoute(indexOfMarkerPrecedingUser);

        User.setPosition(
            getPrecisePositionOfUserMarker(
                indexOfMarkerPrecedingUser,
                distanceLeftToDo
            )
        );

        pathDone.add(User.getPosition());

        return pathDone;
    }

    /**
     * Find where to put the user's marker between 2 point
     * @param indexOfMarkerPrecedingUser is the index of the marker preceding User on the wayPoints list
     * @param distanceLeftToDo is the distance left to do to complete the route in meters
     */
    private LatLng getPrecisePositionOfUserMarker(int indexOfMarkerPrecedingUser, double distanceLeftToDo){

        LatLng PointA = wayPoints.get(indexOfMarkerPrecedingUser);
        LatLng PointB = wayPoints.get(indexOfMarkerPrecedingUser + 1);

        double distanceBetweenMarkersDividedBy100 =
                distanceBetween2Markers(PointA, PointB) / 100;

        int numberOfTimeNeededToReachDistanceLeftToDo =
                getTheNumberOfTimeNeededToReachDistanceLeftToDo(
                        distanceLeftToDo,
                        distanceBetweenMarkersDividedBy100
                );

        LatLng coordinateDifferencesDividedBy100 =
                getTheDifferenceOfLatLngDividedBy100(PointA, PointB);

        return calculateUserPosition(
                determineSituation(PointA, PointB),
                numberOfTimeNeededToReachDistanceLeftToDo + 1,
                coordinateDifferencesDividedBy100,
                PointA
        );
    }

    /**
     * Take coordinate of point1 and change it until reach the calculate coordinate of the user position
     * @param situation specify where is the point2 from the point1
     * @param iteration specify how time the ratio has to be apply
     * @param ratio is the difference of LatLng divided by 100 between point1 and point2
     * @param start is the LatLng of point1
     */
    private LatLng calculateUserPosition(int situation, int iteration, LatLng ratio, LatLng start){
        double preciseLat = start.latitude;
        double preciseLong = start.longitude;

        switch (situation){
            //Above & Right
            case 0:
                for(int i = 0; i < iteration; ++i){
                    preciseLat += ratio.latitude;
                    preciseLong += ratio.longitude;
                }
                break;

            //Under & Right
            case 1:
                for(int i = 0; i < iteration; ++i){
                    preciseLat -= ratio.latitude;
                    preciseLong += ratio.longitude;
                }
                break;

            //Under & Left
            case 2:
                for(int i = 0; i < iteration; ++i){
                    preciseLat -= ratio.latitude;
                    preciseLong -= ratio.longitude;
                }
                break;

            //Above & Left
            case 3:
                for(int i = 0; i < iteration; ++i){
                    preciseLat += ratio.latitude;
                    preciseLong -= ratio.longitude;
                }
                break;

            //Right
            case 4:
                for(int i = 0; i < iteration; ++i){
                    preciseLong += ratio.longitude;
                }
                break;

            //Left
            case 5:
                for(int i = 0; i < iteration; ++i){
                    preciseLong -= ratio.longitude;
                }
                break;

            //Above
            case 6:
                for(int i = 0; i < iteration; ++i){
                    preciseLat += ratio.latitude;
                }
                break;

            //Under
            case 7:
                for(int i = 0; i < iteration; ++i){
                    preciseLat -= ratio.latitude;
                }
                break;

            //Center
            case 8:
                //Nothing
                break;
        }

        return new LatLng(preciseLat, preciseLong);
    }

    /**
     * Determine where is nextPoint from currentPoint (Above/Under on the right/left)
     * @param currentPoint is the point of origin
     * @param nextPoint is the point just after currentPoint in the wayPoints list
     */
    private int determineSituation(LatLng currentPoint, LatLng nextPoint){

        //Above & Right
        if(currentPoint.latitude < nextPoint.latitude
                && currentPoint.longitude < nextPoint.longitude)
            return 0;

        //Under & Right
        else if(currentPoint.latitude > nextPoint.latitude
                && currentPoint.longitude < nextPoint.longitude)
            return 1;

        //Under & Left
        else if(currentPoint.latitude > nextPoint.latitude
                && currentPoint.longitude > nextPoint.longitude)
            return 2;

        //Above & Left
        else if(currentPoint.latitude < nextPoint.latitude
                && currentPoint.longitude > nextPoint.longitude)
            return 3;

        //Right
        else if(currentPoint.latitude == nextPoint.latitude
                && currentPoint.longitude < nextPoint.longitude)
            return 4;

        //Left
        else if(currentPoint.latitude == nextPoint.latitude
                && currentPoint.longitude > nextPoint.longitude)
            return 5;

        //Above
        else if(currentPoint.latitude < nextPoint.latitude
                && currentPoint.longitude == nextPoint.longitude)
            return 6;

        //Under
        else if(currentPoint.latitude > nextPoint.latitude
                && currentPoint.longitude == nextPoint.longitude)
            return 7;

        //Center
        else
            return 8;
    }

    /**
     * Find the differences between the 2 markers' coordinates and divided it by 100
     * @param point1 is the origin point
     * @param point2 is the next point
     */
    private LatLng getTheDifferenceOfLatLngDividedBy100(LatLng point1, LatLng point2){
        double Lat = Math.abs(Math.abs(point1.latitude) - Math.abs(point2.latitude))/100;
        double Long = Math.abs(Math.abs(point1.longitude) - Math.abs(point2.longitude))/100;

        return new LatLng(Lat, Long);
    }

    /**
     * Find the number of time needed to reach the distance left to do to precisely place the user marker
     * @param distanceLeft is the distance done by the user - distance of the preceding marker from the start of the route
     * @param partOfTheDistance is total distance between the 2 waypoints between which the user marker has to be draw divided by 100
     */
    private int getTheNumberOfTimeNeededToReachDistanceLeftToDo(double distanceLeft, double partOfTheDistance){
        int i = 0;

        while(distanceLeft > 0){
            distanceLeft -= partOfTheDistance;
            ++i;
        }

        return i;
    }

    /**
     * Find the marker on the route preceding the user's one
     * @param distanceDone is the distance done by the user
     */
    private int getIndexOfMarkerPrecedingUser(double distanceDone){
        double distanceTmp = 0;

        //Route is complete
        if(distanceDone >= totalLengthOfTheRoute()){
            return wayPoints.size()-1;
        }

        //Find
        for(int i = 0; i < wayPoints.size(); ++i){
            if(distanceTmp == distanceDone)
                return i;
            else if(distanceTmp > distanceDone)
                return i-1;
            else
                distanceTmp += distanceBetween2Markers(wayPoints.get(i), wayPoints.get(i+1));
        }
        //Errors
        return -1;
    }
}
