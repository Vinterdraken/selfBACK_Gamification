package com.example.a1715232.models_2.controllers;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Route;
import io.ticofab.androidgpxparser.parser.domain.RoutePoint;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;

public class GPXController {

    private static GPXParser  mParser;
    private static Gpx parsedGpx;
    private static InputStream file;

    //CONSTRUCTOR
    public GPXController() {
    }

    //FUNCTIONS
    public static ArrayList<LatLng> getTrackPointsFromGPX(Context appContext, String fileName){

        ArrayList<LatLng> trackPoints = new ArrayList<>();

        openFile(appContext, fileName);

        if (parsedGpx != null) {
            List<Track> tracks = parsedGpx.getTracks();
            for (int i = 0; i < tracks.size(); ++i){

                List<TrackSegment> segments = tracks.get(i).getTrackSegments();
                for (int j = 0; j < segments.size(); ++j){

                    for(TrackPoint trackPoint : segments.get(j).getTrackPoints()){
                        trackPoints.add(new LatLng(trackPoint.getLatitude(), trackPoint.getLongitude()));
                    }
                }
            }
        }

        return trackPoints;
    }

    public static ArrayList<LatLng> getWayPointsFromGPX(Context appContext, String fileName){
        ArrayList<LatLng> wayPoints = new ArrayList<>();

        openFile(appContext, fileName);

        if(parsedGpx != null){
            for (WayPoint wayPoint : parsedGpx.getWayPoints()){
                wayPoints.add(new LatLng(wayPoint.getLatitude(), wayPoint.getLongitude()));
            }
        }

        return wayPoints;
    }

    public static ArrayList<LatLng> getRoutePointsFromGPX(Context appContext, String fileName){
        ArrayList<LatLng> routePoints = new ArrayList<>();

        openFile(appContext, fileName);

        if(parsedGpx != null){
            for(Route route : parsedGpx.getRoutes()){
                for (RoutePoint routePoint : route.getRoutePoints()){
                    routePoints.add(new LatLng(routePoint.getLatitude(), routePoint.getLongitude()));
                }
            }
        }

        return routePoints;
    }

    private static void openFile(Context appContext, String fileName){
        mParser = new GPXParser();
        parsedGpx = null;
        file = null;

        try {
            file = appContext.getAssets().open("gpx/" + fileName);
            parsedGpx = mParser.parse(file);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}
