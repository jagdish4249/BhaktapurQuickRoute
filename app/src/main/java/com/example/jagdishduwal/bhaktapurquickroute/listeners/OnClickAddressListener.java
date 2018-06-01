package com.example.jagdishduwal.bhaktapurquickroute.listeners;



import com.example.jagdishduwal.bhaktapurquickroute.db.Node;


public interface OnClickAddressListener {
    /**
     * tell Activity what to do when address is clicked
     *
     * @param view
     */
    void onClick(Node addr);
}
