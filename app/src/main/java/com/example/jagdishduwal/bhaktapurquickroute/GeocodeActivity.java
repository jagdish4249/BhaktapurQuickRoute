package com.example.jagdishduwal.bhaktapurquickroute;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.jagdishduwal.bhaktapurquickroute.db.Node;
import com.example.jagdishduwal.bhaktapurquickroute.db.NodeGateway;
import com.example.jagdishduwal.bhaktapurquickroute.fragments.MyAddressAdapter;
import com.example.jagdishduwal.bhaktapurquickroute.listeners.OnClickAddressListener;
import com.example.jagdishduwal.bhaktapurquickroute.util.Variable;
//import com.junjunguo.pocketmaps.fragments.MessageDialog;
//import com.junjunguo.pocketmaps.fragments.MyAddressAdapter;
//import com.junjunguo.pocketmaps.geocoding.AddressLoc;
//import com.junjunguo.pocketmaps.geocoding.GeocoderGlobal;
//import com.junjunguo.pocketmaps.model.listeners.OnClickAddressListener;
//import com.junjunguo.pocketmaps.util.Variable;

import org.oscim.core.GeoPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

public class GeocodeActivity extends AppCompatActivity
{
 // private static final String FAV_PROP_FILE = "Favourites.properties";
//  private static final String SEL_FROM = "Location from";
////  private static final String SEL_TO = "Location to";
////  private static final String SEL_CUR = "Current location";
//  public static final String ENGINE_OSM = "OpenStreetMap";
//  public static final String ENGINE_GOOGLE = "Google Maps";
//  public static final String ENGINE_OFFLINE = "Offline";
  private static OnClickAddressListener callbackListener;
  private static GeoPoint[] locations;
 // private static Properties favourites;
//  Spinner geoSpinner;
  EditText txtLocation;
//  Button okButton;
 // boolean statusLoading = false;
  
  /** Set pre-settings.
   *  @param newCallbackListener The Callback listener, called on selected Address.
   *  @param newLocations The [0]=start [1]=end and [2]=cur location used on Favourites, or null. **/
  public static void setPre(OnClickAddressListener newCallbackListener, GeoPoint[] newLocations)
  {
      callbackListener = newCallbackListener;
      locations = newLocations;
  }
  
  @Override protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_addresses);
    Log.i("Geocode error: ", "geocode activity started");

      RecyclerView listView ;
          listView = (RecyclerView) findViewById(R.id.my_addr_recycler_view);
          listView.setHasFixedSize(true);


      // use a linear layout manager
      LinearLayoutManager layoutManager = null;
      try {
          layoutManager = new LinearLayoutManager(this.getApplicationContext());
      } catch (Exception e) {
          logUser(e.getMessage());
      }
      layoutManager.setOrientation(LinearLayout.VERTICAL);
    listView.setLayoutManager(layoutManager);
    listView.setItemAnimator(new DefaultItemAnimator());



//      Node node1 = new Node(1, "23", "87");
//      Node node2= new Node(2,"27.670971","85.4392238");
//ArrayList<Node> nodeArrayList = new ArrayList<>();
//nodeArrayList.add(node1);
//nodeArrayList.add(node2);


//      double a[]= new double[23];
//      double b[]= new double[23];
//      ArrayList<GeoPoint> favPoints = new ArrayList<GeoPoint>();
      NodeGateway nodeGateway = new NodeGateway(this);
      ArrayList<Node> nodes = new ArrayList<>(nodeGateway.getAllNodes());


//      for (int i = 0; i < nodes.size(); i++) {
//          a[i]= Double.parseDouble(nodes.get(i).getLatitude());
//          b[i] = Double.parseDouble(nodes.get(i).getLongitude());
//
//
//      }
      // ArrayList<GeoPoint> favPoints = new ArrayList<GeoPoint>();
//double a[]= {27.670971,27.6720744,27.67097158695318,27.673474493619626,27.67349587210709,27.673395142356995,27.672568503700408,27.673395142356995};
//        double b[] = {85.4392238,85.4259136,85.43902314676984,85.43834189394795,85.43724951306183,85.43535768985748,85.4325145483017,85.43535768985748};
//      for(int i= 0 ; i<a.length ; i++){
//          GeoPoint p1 = new GeoPoint(a[i],b[i]);
//
//          favPoints.add(p1);
//      }





      Bundle extras = getIntent().getExtras();

      String value = "true";
      if(extras != null){
         value = extras.getString("isStartP");
      }

          MyAddressAdapter adapter = new MyAddressAdapter(nodes, this , Boolean.parseBoolean(value));
          listView.setAdapter(adapter);



  }



  
  private void log(String str)
  {
    Log.i(GeocodeActivity.class.getName(), str);
  }
    
  private void logUser(String str)
  {
    Log.i(GeocodeActivity.class.getName(), str);
    try
    {
      Toast.makeText(this.getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
    catch (Exception e) { e.printStackTrace(); }
  }
}

