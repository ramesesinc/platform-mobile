/*
 * NetworkLocationProvider.java
 *
 * Created on January 30, 2014, 4:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.client.android;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author wflores 
 */
public final class NetworkLocationProvider 
{
    private static Object LOCKED = new Object();
    private static NetworkLocationProvider instance; 
    
    static {
        synchronized (LOCKED) {
            if (instance == null) {
                instance = new NetworkLocationProvider();  
            } 
        }
    }
    
    
    public static synchronized void setEnabled(boolean enabled) {
        if (enabled) {
            instance.enabled();
        } else {
            instance.disabled();
        }
    }
    
    public static synchronized Location getLocation() {
        return instance.location; 
    }

    private Timer timer;
    private Location location; 
    private LocationManager locationMgr;
    private LocationFetcher fetcher; 
    private DefaultLocationListener locationListener; 
    
    private NetworkLocationProvider() {
        timer = new Timer(); 
        locationListener = new DefaultLocationListener(); 
    }

    protected void finalize() throws Throwable {
        disabled();         
        super.finalize();
    }
    
    private void enabled() { 
        if (fetcher == null) {
            fetcher = new LocationFetcher();
            timer.schedule(fetcher, 0, 2000); 
        } 
    } 
    
    private void disabled() {
        try { timer.cancel(); } catch(Throwable t) {;} 
        try { timer.purge(); } catch(Throwable t) {;}  
        
        fetcher = null; 
        
        try { 
            LocationManager lm = getLocationManager();
            if (lm != null) lm.removeUpdates(locationListener); 
        } catch(Throwable t) {
            //do nothing 
        } 
    }
        
    private LocationManager getLocationManager() {
        if (locationMgr == null) {
            UIApplication uiapp = Platform.getApplication();
            if (uiapp == null) return null;

            locationMgr = (LocationManager) uiapp.getSystemService(Context.LOCATION_SERVICE); 
        } 
        return locationMgr; 
    }
    
    
    private final static int TIME_RANGE = 1000 * 10;
    
    private class LocationFetcher extends TimerTask 
    {
        NetworkLocationProvider root = NetworkLocationProvider.this;
        
        public void run() {
            LocationManager lm = root.getLocationManager();
            if (lm == null) {
                System.out.println("LocationManager is not set");
                return; 
            }
            
            boolean netEnabled = isProviderEnabled(lm, LocationManager.NETWORK_PROVIDER);
            boolean gpsEnabled = isProviderEnabled(lm, LocationManager.GPS_PROVIDER); 
            if (!gpsEnabled && !netEnabled) return;
            
            Location netLoc = null;
            Location gpsLoc = null;
            if (netEnabled) {
                requestLocationUpdates(lm, LocationManager.NETWORK_PROVIDER, 0, 0); 
                netLoc = getLastKnownLocation(lm, LocationManager.NETWORK_PROVIDER);
            } 
            if (gpsEnabled) {
                requestLocationUpdates(lm, LocationManager.GPS_PROVIDER, 0, 0); 
                gpsLoc = getLastKnownLocation(lm, LocationManager.GPS_PROVIDER);
            } 
            
            try { 
                lm.removeUpdates(root.locationListener); 
            } catch(Throwable t) { 
                //do nothing 
            } 
            
            if (netLoc == null && gpsLoc == null) return;
            
            Location newLoc = whichLocation(root.location, netLoc);
            if (gpsLoc != null) newLoc = whichLocation(newLoc, gpsLoc);
            
            root.location = newLoc;
//            if (root.location != null) {
//                System.out.println("provider="+ root.location.getProvider() +", lng=" + root.location.getLongitude() + ", lat="+root.location.getLatitude() + ", time="+root.location.getTime()); 
//            }
        } 
                
        private void requestLocationUpdates(LocationManager lm, String provider, long minTime, float minDistance) {
            try {
                lm.requestLocationUpdates(provider, minTime, minDistance, root.locationListener); 
            } catch(Throwable t) {
                //System.out.println("[warn] failed to request location updates caused by " + t.getMessage());
            } 
        } 
        
        private boolean isProviderEnabled(LocationManager lm, String provider) {
            try { 
                return lm.isProviderEnabled(provider);  
            } catch(Throwable t) {
                return false; 
            }
        }
        
        private Location getLastKnownLocation(LocationManager lm, String provider) {
            try { 
                return lm.getLastKnownLocation(provider); 
            } catch(Throwable t) { 
                return null; 
            }
        }        
        
        private Location whichLocation(Location oldLoc, Location newLoc) {
            if (oldLoc == null && newLoc == null) {
                return null; 
            } else if (newLoc == null) {
                return oldLoc; 
            } else if (oldLoc == null) {
                return newLoc;
            }
            
            // Check whether the new location fix is newer or older
            long timeDelta = newLoc.getTime() - oldLoc.getTime();
            boolean isSignificantlyNewer = timeDelta > TIME_RANGE;
            boolean isSignificantlyOlder = timeDelta < TIME_RANGE;
            boolean isNewer = timeDelta > 0;       
            
            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return newLoc;
            } else if (isSignificantlyOlder) {
                return oldLoc;
            }            
            
            int accuracyDelta = (int)(newLoc.getAccuracy() - oldLoc.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0; 
            boolean isMoreAccurate = accuracyDelta < 0; 
            boolean isSignificantlyLessAccurate = accuracyDelta > 200; 
            
            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(oldLoc, newLoc);
            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) { 
                return newLoc;
            } else if (isNewer && !isLessAccurate) {
                return newLoc; 
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return newLoc;
            } else {
                return oldLoc;
            } 
        } 
        
        private boolean isSameProvider(Location oldLoc, Location newLoc) {
            return oldLoc.getProvider().equals(newLoc.getProvider()); 
        }        
    }
    
    private class DefaultLocationListener implements LocationListener 
    {
        public void onLocationChanged(Location location) {}
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    } 
}
