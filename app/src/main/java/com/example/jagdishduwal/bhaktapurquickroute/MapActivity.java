package com.example.jagdishduwal.bhaktapurquickroute;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.jagdishduwal.bhaktapurquickroute.db.Node;
import com.example.jagdishduwal.bhaktapurquickroute.db.NodeGateway;
import com.example.jagdishduwal.bhaktapurquickroute.db.SQLHelper;
import com.example.jagdishduwal.bhaktapurquickroute.map.Destination;
import com.example.jagdishduwal.bhaktapurquickroute.map.MapHandler;
import com.example.jagdishduwal.bhaktapurquickroute.map.Navigator;
import com.example.jagdishduwal.bhaktapurquickroute.navigator.NaviEngine;
import com.example.jagdishduwal.bhaktapurquickroute.util.Variable;
import com.example.jagdishduwal.bhaktapurquickroute.util.SetStatusBarColor;


import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.GeoPoint;
import org.oscim.core.GeometryBuffer;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Point;
import org.oscim.core.Tag;
import org.oscim.core.Tile;
import org.oscim.event.Gesture;
import org.oscim.tiling.source.mapfile.MapDatabase;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapReadResult;
import org.oscim.tiling.source.mapfile.PointOfInterest;
import org.oscim.tiling.source.mapfile.Way;
import org.oscim.utils.GeoPointUtils;

import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapDatabase;
import org.oscim.tiling.source.mapfile.MapReadResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends Activity implements LocationListener {
    enum PermissionStatus { Enabled, Disabled, Requesting, Unknown };
    private MapView mapView;
    private static Location mCurrentLocation;
    private Location mLastLocation;
    private MapActions mapActions;
    private LocationManager locationManager;
    private PermissionStatus locationListenerStatus = PermissionStatus.Unknown;
    private String lastProvider;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastProvider = null;
        setContentView(R.layout.activity_map);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Variable.getVariable().setContext(getApplicationContext());
        Variable.getVariable().setZoomLevels(22, 1);
//        AndroidGraphicFactory.createInstance(getApplication());
        mapView = new MapView(this);
        mapView.setClickable(true);
//        mapView.setBuiltInZoomControls(false);
        MapHandler.getMapHandler()
                .init(this, mapView, Variable.getVariable().getCountry(), Variable.getVariable().getMapsFolder());
        try
        {
          MapHandler.getMapHandler().loadMap(new File(Variable.getVariable().getMapsFolder().getAbsolutePath(),
                Variable.getVariable().getCountry() + "-gh"));
          getIntent().putExtra("com.example.jagdishduwal.bhaktapurquickroute.MapActivity.SELECTNEWMAP", false);
        }
        catch (Exception e)
        {
          logUser("Map file seems corrupt!\nPlease try to re-download.");
          log("Error while loading map!");
          e.printStackTrace();
          finish();
          Intent intent = new Intent(this, MainActivity.class);
          intent.putExtra("com.example.jagdishduwal.bhaktapurquickroute.MapActivity.SELECTNEWMAP", true);
          startActivity(intent);
          return;
        }
        customMapView();
        checkGpsAvailability();
        ensureLastLocationInit();
        updateCurrentLocation(null);
        showAllPoints();

    }
    
    public void ensureLocationListener(boolean showMsgEverytime)
    {
      if (locationListenerStatus == PermissionStatus.Disabled) { return; }
      if (locationListenerStatus != PermissionStatus.Enabled)
      {
        boolean f_loc = Permission.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, this);
        if (!f_loc)
        {
          if (locationListenerStatus == PermissionStatus.Requesting)
          {
            locationListenerStatus = PermissionStatus.Disabled;
            return;
          }
          locationListenerStatus = PermissionStatus.Requesting;
          String[] permissions = new String[2];
          permissions[0] = android.Manifest.permission.ACCESS_FINE_LOCATION;
          permissions[1] = android.Manifest.permission.ACCESS_COARSE_LOCATION;
          Permission.startRequest(permissions, false, this);
          return;
        }
      }
      try
      {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider==null)
        {
          lastProvider = null;
          locationManager.removeUpdates(this);
          logUser("LocationProvider is off!");
          return;
        }
        else if (provider.equals(lastProvider))
        {
          if (showMsgEverytime)
          {
            logUser("LocationProvider: " + provider);
          }
          return;
        }
        locationManager.removeUpdates(this);
        lastProvider = provider;
        locationManager.requestLocationUpdates(provider, 3000, 5, this);
        logUser("LocationProvider: " + provider);
        locationListenerStatus = PermissionStatus.Enabled;
      }
      catch (SecurityException ex)
      {
        logUser("Location_Service not allowed by user!");
      }
    }

    /**
     * inject and inflate activity map content to map activity context and bring it to front
     */
    private void customMapView() {
        ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.custom_map_view_layout);
        View inflate = LayoutInflater.from(this).inflate(R.layout.activity_map_content, null);
        inclusionViewGroup.addView(inflate);

        inclusionViewGroup.getParent().bringChildToFront(inclusionViewGroup);
        new SetStatusBarColor().setSystemBarColor(findViewById(R.id.statusBarBackgroundMap),
                getResources().getColor(R.color.my_primary_dark_transparent), this);
        mapActions = new MapActions(this, mapView);
    }

    /**
     * check if GPS enabled and if not send user to the GSP settings
     */
    private void checkGpsAvailability() {
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    /**
     * Updates the users location based on the location
     *
     * @param location Location
     */
    private void updateCurrentLocation(Location location) {
        if (location != null) {
            mCurrentLocation = location;
        } else if (mLastLocation != null && mCurrentLocation == null) {
            mCurrentLocation = mLastLocation;
        }
        if (mCurrentLocation != null) {
            GeoPoint mcLatLong = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//            if (Tracking.getTracking().isTracking()) {
//                MapHandler.getMapHandler().addTrackPoint(mcLatLong);
//                Tracking.getTracking().addPoint(mCurrentLocation, mapActions.getAppSettings());
//            }
            if (NaviEngine.getNaviEngine().isNavigating())
            {
              NaviEngine.getNaviEngine().updatePosition(this, mCurrentLocation);
            }
            MapHandler.getMapHandler().setCustomPoint(mcLatLong);
            mapActions.showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp);
        } else {
            mapActions.showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp);
        }
    }

    @Override public void onBackPressed() {
        boolean back = mapActions.homeBackKeyPressed();
        if (back) {
            moveTaskToBack(true);
        }
        // if false do nothing
    }

    @Override protected void onStart() {
        super.onStart();
    }

    @Override public void onResume() {
        super.onResume();
        mapView.onResume();
        ensureLocationListener(true);
        ensureLastLocationInit();
    }

    @Override protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override protected void onStop() {
        super.onStop();
        if (mCurrentLocation != null) {
            GeoPoint geoPoint = mapView.map().getMapPosition().getGeoPoint();
            Variable.getVariable().setLastLocation(geoPoint);
            //                        log("last browsed location : "+mapView.getModel().mapViewPosition
            // .getMapPosition().latLong);
        }
        if (mapView != null) Variable.getVariable().setLastZoomLevel(mapView.map().getMapPosition().getZoomLevel());
        Variable.getVariable().saveVariables();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        lastProvider = null;
        mapView.onDestroy();
        if (MapHandler.getMapHandler().getHopper() != null) MapHandler.getMapHandler().getHopper().close();
        MapHandler.getMapHandler().setHopper(null);
        Navigator.getNavigator().setOn(false);
        MapHandler.reset();
        Destination.getDestination().setStartPoint(null);
        Destination.getDestination().setEndPoint(null);
        System.gc();
    }

    /**
     * @return my currentLocation
     */
    public static Location getmCurrentLocation() {
        return mCurrentLocation;
    }

    private void ensureLastLocationInit()
    {
      if (mLastLocation != null) { return; }
      try
      {
        Location lonet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lonet != null) { mLastLocation = lonet; return; }
      }
      catch (SecurityException|IllegalArgumentException e)
      {
        log("NET-Location is not supported: " + e.getMessage());
      }
      try
      {
        Location logps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (logps != null) { mLastLocation = logps; return; }
      }
      catch (SecurityException|IllegalArgumentException e)
      {
        log("GPS-Location is not supported: " + e.getMessage());
      }
    }

    /**
     * Called when the location has changed.
     * <p/>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override public void onLocationChanged(Location location) {
        updateCurrentLocation(location);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override public void onProviderEnabled(String provider) {
        logUser("LocationService is turned on!!");
    }

    @Override public void onProviderDisabled(String provider) {
        logUser("LocationService is turned off!!");
    }


    public void showAllPoints(){

        double a[]= new double[23];
        double b[]= new double[23];

        ArrayList<GeoPoint> favPoints = new ArrayList<GeoPoint>();
        NodeGateway nodeGateway = new NodeGateway(this);
        ArrayList<Node> nodes = new ArrayList<>(nodeGateway.getAllNodes());

        //Toast.makeText(this, "database loaded with node size" + nodes.size(), Toast.LENGTH_SHORT).show();

//        for(Node node: nodes) {
//
//            GeoPoint p1 = new GeoPoint(Double.parseDouble(node.getLatitude()), Double.parseDouble(node.getLatitude()));
//            favPoints.add(p1);
//        }

        for (int i = 0; i < nodes.size(); i++) {
            a[i]= Double.parseDouble(nodes.get(i).getLatitude());
            b[i] = Double.parseDouble(nodes.get(i).getLongitude());


        }

        // ArrayList<GeoPoint> favPoints = new ArrayList<GeoPoint>();
//double a[]= {27.670971,27.6720744,27.67097158695318,27.673474493619626,27.67349587210709,27.673395142356995,27.672568503700408,27.673395142356995};
//        double b[] = {85.4392238,85.4259136,85.43902314676984,85.43834189394795,85.43724951306183,85.43535768985748,85.4325145483017,85.43535768985748};
        for(int i= 0 ; i<a.length ; i++){
            GeoPoint p1 = new GeoPoint(a[i],b[i]);

            favPoints.add(p1);
        }
     MapHandler.getMapHandler().setFavPoint(favPoints);


    }



    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getName(), str);
    }
    
    private void logUser(String str) {
      Log.i(this.getClass().getName(), str);
      try
      {
        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
      }
      catch (Exception e) { e.printStackTrace(); }
    }



}
