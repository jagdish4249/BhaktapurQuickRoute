package com.example.jagdishduwal.bhaktapurquickroute.listeners;

import android.view.View;
import android.widget.TextView;


public interface OnClickMapListener {
    /**
     * tell Activity what to do when map FAB is clicked
     *
     * @param view
     * @param downloadStatus 
     */
    void onClickMap(View view, int pos, TextView downloadStatus);
}
