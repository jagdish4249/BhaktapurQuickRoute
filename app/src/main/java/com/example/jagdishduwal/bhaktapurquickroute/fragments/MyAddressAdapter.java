package com.example.jagdishduwal.bhaktapurquickroute.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jagdishduwal.bhaktapurquickroute.MainActivity;
import com.example.jagdishduwal.bhaktapurquickroute.MapActions;
import com.example.jagdishduwal.bhaktapurquickroute.MapActivity;
import com.example.jagdishduwal.bhaktapurquickroute.R;
import com.example.jagdishduwal.bhaktapurquickroute.db.Node;
import com.example.jagdishduwal.bhaktapurquickroute.map.Destination;
import com.example.jagdishduwal.bhaktapurquickroute.map.MapHandler;
//import com.example.jagdishduwal.bhaktapurquickroute.listeners.OnClickAddressListener;
//import com.example.jagdishduwal.bhaktapurquickroute.model.listeners.OnClickAddressListener;

import org.oscim.core.GeoPoint;

import java.util.List;


public class MyAddressAdapter extends RecyclerView.Adapter<MyAddressAdapter.ViewHolder> {
    private List<Node> addressList;
    private Context context;
    private boolean startP;




    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView firstLine, secondLine;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            this.firstLine = (TextView) itemView.findViewById(R.id.mapFirstLineTxt);
            this.secondLine = (TextView) itemView.findViewById(R.id.mapSecondLineTxt);
            this.linearLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout);

        }


    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAddressAdapter(List<Node> addressList,Context context,Boolean b) {
        Log.i("adapter error:","reached adapter for address");
        this.addressList = addressList;
        this.context = context;
        this.startP = b;
        //this.onClickAddressListener = onClickAddressListener;
    }

    // Create new views (invoked by the layout manager)
    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_entry, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Node address = addressList.get(position);

        holder.firstLine.setText(address.getLatitude());
        holder.secondLine.setText(address.getLongitude());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startP)
               Toast.makeText(context,"ID: "+address.getID()+ "latitude: "+ address.getLatitude() +"longitude: "+ address.getLongitude(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context,"ID: "+address.getID()+ "latitude: "+ address.getLatitude() +"longitude: "+ address.getLongitude(), Toast.LENGTH_SHORT).show();

            }

        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override public int getItemCount() {
        return addressList.size();
    }

    public static void log(String s)
    {
        Log.i(MyAddressAdapter.class.getName(), s);
    }


    

}
