package com.example.jagdishduwal.bhaktapurquickroute.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.jagdishduwal.bhaktapurquickroute.R;
import com.example.jagdishduwal.bhaktapurquickroute.db.SQLHelper;
import com.example.jagdishduwal.bhaktapurquickroute.navigator.NaviEngine;
import com.example.jagdishduwal.bhaktapurquickroute.util.Variable;
import com.example.jagdishduwal.bhaktapurquickroute.util.Variable;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Constants;
import com.graphhopper.util.Parameters.Algorithms;
import com.graphhopper.util.Parameters.Routing;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;
import com.example.jagdishduwal.bhaktapurquickroute.listeners.MapHandlerListener;

import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.core.GeometryBuffer;
import org.oscim.core.MapPosition;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Point;
import org.oscim.core.Tag;
import org.oscim.core.Tile;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Layers;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapDatabase;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapReadResult;
import org.oscim.tiling.source.mapfile.PointOfInterest;
import org.oscim.tiling.source.mapfile.Way;
import org.oscim.utils.GeoPointUtils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MapHandler
{
  private static MapHandler mapHandler;
  private volatile boolean prepareInProgress = false;
  private volatile boolean calcPathActive = false;
  MapPosition tmpPos = new MapPosition();
  private GeoPoint startMarker;
  private GeoPoint endMarker;
  private boolean needLocation = false;
  private Activity activity;
  private MapView mapView;
  private ItemizedLayer<MarkerItem> itemizedLayer;
  private ItemizedLayer<MarkerItem> customLayer;
  private ItemizedLayer<MarkerItem> favPointLayer;
  private PathLayer pathLayer;
  private PathLayer polylineTrack;
  private GraphHopper hopper;
  private MapHandlerListener mapHandlerListener;
  private String currentArea;
  File mapsFolder;
  PointList trackingPointList;
  private int customIcon = R.drawable.ic_my_location_dark_24dp;
  private int favPointIcon = R.drawable.marker_icon_red;
  private MapFileTileSource tileSource;
  /**
   * need to know if path calculating status change; this will trigger MapActions function
   */
  private boolean needPathCal;
  
  public static MapHandler getMapHandler()
  {
    if (mapHandler == null)
    {
      reset();
    }
    return mapHandler;
  }

   /**
    * reset class, build a new instance
    */
  public static void reset()
  {
    mapHandler = new MapHandler();
  }

  private MapHandler()
  {
    setCalculatePath(false,false);
    startMarker = null;
    endMarker = null;
//        polylinePath = null;
    needLocation = false;
    needPathCal = false;
  }

  public void init(Activity activity, MapView mapView, String currentArea, File mapsFolder)
  {
    this.activity = activity;
    this.mapView = mapView;
    this.currentArea = currentArea;
    this.mapsFolder = mapsFolder; // path/to/map/area-gh/
  }
  
  public MapFileTileSource getTileSource() { return tileSource; }

  /**
   * load map to mapView
   *
   * @param areaFolder
   */
  public void loadMap(File areaFolder)
  {
    logUser("loading map");

    // Map events receiver
    mapView.map().layers().add(new MapEventsReceiver(mapView.map()));

    // Map file source
    tileSource = new MapFileTileSource();
    tileSource.setMapFile(new File(areaFolder, currentArea + ".map").getAbsolutePath());
    VectorTileLayer l = mapView.map().setBaseMap(tileSource);
    mapView.map().setTheme(VtmThemes.DEFAULT);
    mapView.map().layers().add(new BuildingLayer(mapView.map(), l));
    mapView.map().layers().add(new LabelLayer(mapView.map(), l));

    // Markers layer
    itemizedLayer = new ItemizedLayer<>(mapView.map(), (MarkerSymbol) null);
    mapView.map().layers().add(itemizedLayer);
    customLayer = new ItemizedLayer<>(mapView.map(), (MarkerSymbol) null);
    mapView.map().layers().add(customLayer);
    favPointLayer = new ItemizedLayer<MarkerItem>(mapView.map(),(MarkerSymbol) null);
    mapView.map().layers().add(favPointLayer);

    // Map position
//    GeoPoint mapCenter = tileSource.getMapInfo().boundingBox.getCenterPoint();
//    mapView.map().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), 1 << 12);

      //setting bounding box


  //    BoundingBox bBox = new BoundingBox(27.642614,85.347919,27.721053,85.504989);
      BoundingBox bBox = new BoundingBox(27.659871,85.407302,27.683564,85.447688);

      GeoPoint mapCenter = bBox.getCenterPoint();
      mapView.map().viewport().setMapLimit(bBox);

      mapView.map().viewport().setMinZoomLevel(15);
      mapView.map().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), 1 << 15);


    //set map position to libali bhaktapur
     // mapView.map().setMapPosition(27.670971,85.4392238, 1 << 15);

//    GuiMenu.getInstance().showMap(this);
//    setContentView(mapView);
    
    ViewGroup.LayoutParams params =
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    activity.addContentView(mapView, params);
    
    loadGraphStorage();
  }
  
  void loadGraphStorage() {
      logUser("loading graph (" + Constants.VERSION + ") ... ");
      new com.graphhopper.android.GHAsyncTask<Void, Void, Path>() {
          protected Path saveDoInBackground(Void... v) throws Exception {
              GraphHopper tmpHopp = new GraphHopper().forMobile();
              // Why is "shortest" missing in default config? Add!
              tmpHopp.getCHFactoryDecorator().addWeighting("shortest");
              tmpHopp.load(new File(mapsFolder, currentArea).getAbsolutePath() + "-gh");
              log("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
              hopper = tmpHopp;
              return null;
          }

          protected void onPostExecute(Path o) {
              if (hasError()) {
                  logUser("An error happened while creating graph:"
                          + getErrorMessage());
              } else {
                  logUser("Finished loading graph.");
              }

              prepareInProgress = false;
          }
      }.execute();
  }
  
  /**
   * center the LatLong point in the map and zoom map to zoomLevel
   *
   * @param latLong
   * @param zoomLevel (if 0 use current zoomlevel)
   */
  public void centerPointOnMap(GeoPoint latLong, int zoomLevel, float bearing, float tilt)
  {
    if (zoomLevel == 0)
    {
      zoomLevel = mapView.map().getMapPosition().zoomLevel;
    }
    log("Using cur zoom: " + zoomLevel);
    double scale = 1 << zoomLevel;
    tmpPos.setPosition(latLong);
    tmpPos.setScale(scale);
    tmpPos.setBearing(bearing);
    tmpPos.setTilt(tilt);
    mapView.map().animator().animateTo(300, tmpPos);
  }

  /**
   * @return
   */
  public boolean isNeedLocation()
  {
    return needLocation;
  }

  /**
   * set in need a location from screen point (touch)
   *
   * @param needLocation
   */
  public void setNeedLocation(boolean needLocation)
  {
    this.needLocation = needLocation;
  }

  /** Set start or end Point-Marker.
   *  @param p The Point to set, or null.
   *  @param isStart True for startpoint false for endpoint.
   *  @param recalculate True to calculate path, when booth points are set.
   *  @return Whether the path will be recalculated. **/
  public boolean setStartEndPoint(GeoPoint p, boolean isStart, boolean recalculate)
  {
    boolean result = false;
    int icon = R.drawable.ic_location_end_24dp;
    boolean refreshBooth = false;
    if (startMarker!=null && endMarker!=null && p!=null) { refreshBooth = true; }
      
    if (isStart)
    {
      startMarker = p;
      icon = R.drawable.ic_location_start_24dp;
    }
    else { endMarker = p; }

    // remove routing layers
    if ((startMarker==null || endMarker==null) || refreshBooth)
    {
      if (pathLayer!=null) { pathLayer.clearPath(); }
      itemizedLayer.removeAllItems();
    }
    if (refreshBooth)
    {
      itemizedLayer.addItem(createMarkerItem(startMarker, R.drawable.ic_location_start_24dp, 0.5f, 1.0f));
      itemizedLayer.addItem(createMarkerItem(endMarker, R.drawable.ic_location_end_24dp, 0.5f, 1.0f));
    }
    else if (p!=null)
    {
      itemizedLayer.addItem(createMarkerItem(p, icon, 0.5f, 1.0f));
    }
    if (startMarker!=null && endMarker!=null && recalculate)
    {
      recalcPath();
      result = true;
    }
    mapView.map().updateMap(true);
    return result;
  }
  
  public void recalcPath()
  {
    setCalculatePath(true, true);
    calcPath(startMarker.getLatitude(), startMarker.getLongitude(), endMarker.getLatitude(), endMarker.getLongitude());
  }

  /** Set the custom Point for current location, or null to delete.
   *  Sets the offset to center. **/
  public void setCustomPoint(GeoPoint p)
  {
    customLayer.removeAllItems();
    if (p!=null)
    {
      customLayer.addItem(createMarkerItem(p,customIcon, 0.5f, 0.5f));
      mapView.map().updateMap(true);
    }
  }
  
  public void setCustomPointIcon(int customIcon)
  {
    this.customIcon = customIcon;
    if (customLayer.getItemList().size() > 0)
    { // RefreshIcon
      MarkerItem curSymbol = customLayer.getItemList().get(0);
      MarkerSymbol marker = createMarkerItem(new GeoPoint(0,0), customIcon, 0.5f, 0.5f).getMarker();
      curSymbol.setMarker(marker);
    }
  }

    public void setFavPoint(ArrayList<GeoPoint> favPoints)
    {

        favPointLayer.removeAllItems();
        for(GeoPoint p : favPoints) {
            favPointLayer.addItem(createMarkerItem(p, favPointIcon, 0.5f, 0.5f));
        }


    }







    public void setFavpointIcon(int favpointIcon)
    {
        this.favPointIcon = favpointIcon;

    }

  private MarkerItem createMarkerItem(GeoPoint p, int resource, float offsetX, float offsetY) {
//      Drawable drawable = activity.getDrawable(resource); // Since API21
      Drawable drawable = ContextCompat.getDrawable(activity, resource);
      Bitmap bitmap = AndroidGraphics.drawableToBitmap(drawable);
      MarkerSymbol markerSymbol = new MarkerSymbol(bitmap, offsetX, offsetY);
      MarkerItem markerItem = new MarkerItem("", "", p);
      markerItem.setMarker(markerSymbol);
      return markerItem;
  }



    /**
     * remove all markers and polyline from layers
     */
    public void removeMarkers() {
      // setCustomPoint(null, 0);
      setStartEndPoint(null, true, false);
      setStartEndPoint(null, false, false);
    }

    public void removeFavMarkers(){
        favPointLayer.removeAllItems();
    }

    /**
     * @return true if already loaded
     */
    boolean isReady() {
      return !prepareInProgress;
    }

//    /**
//     * start tracking : reset polylineTrack & trackingPointList & remove polylineTrack if exist
//     */
//    public void startTrack() {
//        if (polylineTrack != null) {
//            removeLayer(mapView.map().layers(), polylineTrack);
//        }
//        polylineTrack = null;
//        trackingPointList = new PointList();
//        if (polylineTrack != null) { polylineTrack.clearPath(); }
//        polylineTrack = updatePathLayer(polylineTrack, trackingPointList, 0x99003399, 4);
//        NaviEngine.getNaviEngine().startDebugSimulator(activity, true);
//    }
//
//    /**
//     * add a tracking point
//     *
//     * @param point
//     */
//    public void addTrackPoint(GeoPoint point) {
//      trackingPointList.add(point.getLatitude(), point.getLongitude());
//      updatePathLayer(polylineTrack, trackingPointList, 0x9900cc33, 4);
//      mapView.map().updateMap(true);
//    }
    
  /**
   * remove a layer from map layers
   *
   * @param layers
   * @param layer
   */
  public static void removeLayer(Layers layers, Layer layer)
  {
    if (layers != null && layer != null && layers.contains(layer))
    {
      layers.remove(layer);
    }
  }

    public boolean isCalculatingPath() {
        return calcPathActive;
    }

    private void setCalculatePath(boolean calcPathActive, boolean callListener) {
        this.calcPathActive = calcPathActive;
        if (mapHandlerListener != null && needPathCal && callListener) mapHandlerListener.pathCalculating(calcPathActive);
    }

    public void setNeedPathCal(boolean needPathCal) {
        this.needPathCal = needPathCal;
    }

    /**
     * @return GraphHopper object
     */
    public GraphHopper getHopper() {
        return hopper;
    }

    /**
     * assign a new GraphHopper
     *
     * @param hopper
     */
    public void setHopper(GraphHopper hopper) {
        this.hopper = hopper;
    }

    /**
     * only tell on object
     *
     * @param mapHandlerListener
     */
    public void setMapHandlerListener(MapHandlerListener mapHandlerListener) {
        this.mapHandlerListener = mapHandlerListener;
    }

    public Activity getActivity() {
        return activity;
    }
    

    public void calcPath(final double fromLat, final double fromLon,
                         final double toLat, final double toLon) {
        setCalculatePath(true, false);
        log("calculating path ...");
        new AsyncTask<Void, Void, GHResponse>() {
            float time;

            @Override
            protected GHResponse doInBackground(Void... v) {
                StopWatch sw = new StopWatch().start();
                GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
                        setAlgorithm(Algorithms.DIJKSTRA_BI);
  //              req.getHints().put(Routing.INSTRUCTIONS, Variable.getVariable().getDirectionsON());
                req.setVehicle(Variable.getVariable().getTravelMode());
                req.setWeighting(Variable.getVariable().getWeighting());
                GHResponse resp = hopper.route(req);
                time = sw.stop().getSeconds();
                return resp;
            }

            @Override
            protected void onPostExecute(GHResponse ghResp) {
                if (!ghResp.hasErrors()) {
                    PathWrapper resp = ghResp.getBest();
                    log("from:" + fromLat + "," + fromLon + " to:" + toLat + ","
                            + toLon + " found path with distance:" + resp.getDistance()
                            / 1000f + ", nodes:" + resp.getPoints().getSize() + ", time:"
                            + time + " " + resp.getDebugInfo());
                    logUser("the route is " + (int) (resp.getDistance() / 100) / 10f
                            + "km long, time:" + resp.getTime() / 60000f + "min.");

                    int sWidth = 4;
                    pathLayer = updatePathLayer(pathLayer, resp.getPoints(), 0x9900cc33, sWidth);
                    mapView.map().updateMap(true);
                    Navigator.getNavigator().setGhResponse(resp);
//                    if (Variable.getVariable().isDirectionsON()) {
//                        Navigator.getNavigator().setGhResponse(resp);
//                    }
                } else {
                    //logUser("Multiple errors: " + ghResp.getErrors().size());
                    Toast.makeText(activity, ghResp.getErrors().get(0).toString(), Toast.LENGTH_SHORT).show();
                    log("Multiple errors, first: " + ghResp.getErrors().get(0));
                }


                if (NaviEngine.getNaviEngine().isNavigating())
                {
                    setCalculatePath(false, false);
                }
                else
                {
                    setCalculatePath(false, true);
                    try
                    {
                        activity.findViewById(R.id.map_nav_settings_path_finding).setVisibility(View.GONE);
                        activity.findViewById(R.id.nav_settings_layout).setVisibility(View.VISIBLE);

                    }
                    catch (Exception e) { e.printStackTrace(); }
                }
            }
        }.execute();


        SQLHelper dbHelper;
        dbHelper = new SQLHelper(activity);
        try {
            dbHelper.createDataBase();
        } catch (Exception ioe) {
           // Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
            log("failed");
        }
        dbHelper = new SQLHelper(activity);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();


        int num = 1;
        int v1 = 0;
        int v2 = 0;
        float weight = 0.0f;

        System.out.println("Help");

        int v = 1;
        String selectCount = "SELECT * FROM nodes";
        Cursor cursorq = db.rawQuery(selectCount, null);
        if (cursorq.getCount() > 0)

        {
            cursorq.moveToFirst();
            while (cursorq.moveToNext()) {
                v++;
            }

        }


        int n = 1;
        String Countn = "SELECT * FROM edges";
        Cursor cursorn = db.rawQuery(Countn, null);
        if (cursorn.getCount() > 0)

        {
            cursorn.moveToFirst();
            while (cursorn.moveToNext()) {
                n++;
            }

        }


        float adjacencyMatrix[][] = new float[v][v];


        for (num = 1; num <= n; num++) {


            String selectQuery = "SELECT * FROM edges where id=" + num + "";
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.getCount() > 0)

            {
                cursor.moveToFirst();

                String source = cursor.getString(cursor.getColumnIndex("start_node"));
                v1 = Integer.parseInt(source);
                String destination = cursor.getString(cursor.getColumnIndex("dest_node"));
                v2 = Integer.parseInt(destination);
                String weight1 = cursor.getString(cursor.getColumnIndex("weight"));
                weight = Float.parseFloat(weight1);


            }


            adjacencyMatrix[v1][v2] = weight;
            adjacencyMatrix[v2][v1] = weight;


        }


        dijkstra(adjacencyMatrix, 0);








    }
    
  private PathLayer updatePathLayer(PathLayer ref, PointList pointList, int color, int strokeWidth) {
      if (ref==null) {
          ref = createPathLayer(color, strokeWidth);
          mapView.map().layers().add(ref);
      }
      List<GeoPoint> geoPoints = new ArrayList<>();
      //TODO: Search for a more efficient way
      for (int i = 0; i < pointList.getSize(); i++)
          geoPoints.add(new GeoPoint(pointList.getLatitude(i), pointList.getLongitude(i)));
      ref.setPoints(geoPoints);
      return ref;
  }
    
  private PathLayer createPathLayer(int color, int strokeWidth)
  {
      Style style = Style.builder()
        .fixed(true)
        .generalization(Style.GENERALIZATION_SMALL)
        .strokeColor(color)
        .strokeWidth(strokeWidth * activity.getResources().getDisplayMetrics().density)
        .build();
      PathLayer newPathLayer = new PathLayer(mapView.map(), style);
      return newPathLayer;
  }
    
    class MapEventsReceiver extends Layer implements GestureListener {

      MapEventsReceiver(org.oscim.map.Map map) {
          super(map);
      }

      @Override
      public boolean onGesture(Gesture g, MotionEvent e) {
          if (g instanceof Gesture.Tap) {
              GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
              if (mapHandlerListener!=null && needLocation)
              {
                mapHandlerListener.onPressLocation(p);
              }
          }
          return false;
      }
  }
  
  private void logUser(String str)
  {
    log(str);
    try
    {
      Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
    }
    catch (Exception e) { e.printStackTrace(); }
  }
  
  private void log(String str)
  {
    Log.i(MapHandler.class.getName(), str);
  }

    public String reverseCode(GeoPoint p,Boolean checkStart){

       // GeoPoint p = new GeoPoint(27.670971,85.4392238);


        // Read all labeled POI and ways for the area covered by the tiles under touch

        long mapSize = MercatorProjection.getMapSize((byte) 16);

        double pixelX = MercatorProjection.longitudeToPixelX(p.getLongitude(), mapSize);
        double pixelY = MercatorProjection.latitudeToPixelY(p.getLatitude(), mapSize);

        int tileXMin = MercatorProjection.pixelXToTileX(pixelX,(byte) 16);
        int tileXMax = MercatorProjection.pixelXToTileX(pixelX , (byte) 16);
        int tileYMin = MercatorProjection.pixelYToTileY(pixelY , (byte) 16);
        int tileYMax = MercatorProjection.pixelYToTileY(pixelY , (byte)16);

        Tile upperLeft = new Tile(tileXMin, tileYMin, (byte)16);
        Tile lowerRight = new Tile(tileXMax, tileYMax, (byte)16);


        MapReadResult mapReadResult = null;
        mapReadResult = ((MapDatabase) tileSource.getDataSource()).readLabels(upperLeft, lowerRight);




        StringBuilder sb = new StringBuilder();
        String returnValue = new String();



            // Filter ways

        if(checkStart)
        sb.append("*** Start Point Details ***");
        else
            sb.append("*** End Point Details ***");

            for (Way way : mapReadResult.ways) {
                if (way.geometryType != GeometryBuffer.GeometryType.POLY
                        || !GeoPointUtils.contains(way.geoPoints[0], p)) {
                    continue;
                }
                sb.append("\n");
                List<Tag> tags = way.tags;
                for (Tag tag : tags) {
                    sb.append("\n").append(tag.key).append("=").append(tag.value);
                    returnValue = tag.value;

                }

            }
        // Filter POI
        sb.append("\n");
          sb.append("*** Points of Interest ***");
        for (PointOfInterest pointOfInterest : mapReadResult.pointOfInterests) {
            Point layerXY = new Point();
            mapView.map().viewport().toScreenPoint(pointOfInterest.position, false, layerXY);


            sb.append("\n");
            List<Tag> tags = pointOfInterest.tags;
            for (Tag tag : tags) {
                sb.append("\n").append(tag.key).append("=").append(tag.value);
            }
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//            builder.setIcon(android.R.drawable.ic_menu_search);
        builder.setTitle("Reverse geocoding ");
        builder.setMessage(sb);
//            builder.setPositiveButton(R.string.ok, null);
//            builder.show();

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        return returnValue;

    }


    //DIjkstra

    // A Java program for Dijkstra's
// single source shortest path
// algorithm. The program is for
// adjacency matrix representation

// of the graph.



    private static final float NO_PARENT = -1.0f;

    // Function that implements Dijkstra's
    // single source shortest path
    // algorithm for a graph represented
    // using adjacency matrix
    // representation
    private static void dijkstra(float[][] adjacencyMatrix,
                                 int startVertex)
    {
        int nVertices = adjacencyMatrix[0].length;

        // shortestDistances[i] will hold the
        // shortest distance from src to i
        float[] shortestDistances = new float[nVertices];

        // added[i] will true if vertex i is
        // included / in shortest path tree
        // or shortest distance from src to
        // i is finalized
        boolean[] added = new boolean[(int) nVertices];

        // Initialize all distances as
        // INFINITE and added[] as false
        for (int vertexIndex = 0; vertexIndex < nVertices;
             vertexIndex++)
        {
            shortestDistances[vertexIndex] = Float.MAX_VALUE;
            added[vertexIndex] = false;
        }

        // Distance of source vertex from
        // itself is always 0
        shortestDistances[startVertex] = 0.0f;

        // Parent array to store shortest
        // path tree
        int[] parents = new int[nVertices];

        // The starting vertex does not
        // have a parent
        parents[(int) startVertex] = (int) NO_PARENT;

        // Find shortest path for all
        // vertices
        for (int i = 1; i < nVertices; i++)
        {

            // Pick the minimum distance vertex
            // from the set of vertices not yet
            // processed. nearestVertex is
            // always equal to startNode in
            // first iteration.
            int nearestVertex = -1;
            float shortestDistance =Float.MAX_VALUE;
            for (int vertexIndex = 0;
                 vertexIndex < nVertices;
                 vertexIndex++)
            {
                if (!added[vertexIndex] &&
                        shortestDistances[vertexIndex] <
                                shortestDistance)
                {
                    nearestVertex = vertexIndex;
                    shortestDistance = shortestDistances[vertexIndex];
                }
            }

            // Mark the picked vertex as
            // processed
            added[nearestVertex] = true;

            // Update dist value of the
            // adjacent vertices of the
            // picked vertex.
            for (int vertexIndex = 0;
                 vertexIndex < nVertices;
                 vertexIndex++)
            {
                float edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex];

                if (edgeDistance > 0
                        && ((shortestDistance + edgeDistance) <
                        shortestDistances[vertexIndex]))
                {
                    parents[vertexIndex] = nearestVertex;
                    shortestDistances[vertexIndex] = (int) (shortestDistance +
                            edgeDistance);
                }
            }
        }

        printSolution(startVertex, shortestDistances, parents);
    }

    // A utility function to print
    // the constructed distances
    // array and shortest paths
    private static void printSolution(int startVertex,
                                      float[] distances,
                                      int[] parents)
    {
        float nVertices = distances.length;
        System.out.print("Vertex\t Distance\tPath");

        for (int vertexIndex = 0;
             vertexIndex < nVertices;
             vertexIndex++)
        {
            if (vertexIndex != startVertex)
            {
                System.out.print("\n" + startVertex + " -> ");
                System.out.print(vertexIndex + " \t\t ");
                System.out.print(distances[vertexIndex] + "\t\t");
                printPath(vertexIndex, parents);
            }
        }
    }

    // Function to print shortest path
    // from source to currentVertex
    // using parents array
    private static void printPath(int currentVertex,
                                  int[] parents)
    {

        // Base case : Source node has
        // been processed
        if (currentVertex == NO_PARENT)
        {
            return;
        }
        printPath(parents[currentVertex], parents);
        System.out.print(currentVertex + " ");
    }











}

