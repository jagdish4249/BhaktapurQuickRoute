package com.example.jagdishduwal.bhaktapurquickroute.listeners;


public interface NavigatorListener {
    /**
     * the change on navigator: navigation is used or not
     *
     * @param on
     */
    void onStatusChanged(boolean on);
    
    void onNaviStart(boolean on);
}
