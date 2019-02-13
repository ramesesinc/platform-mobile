/*
 * SuspendTimer.java
 *
 * Created on February 5, 2014, 3:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.client.android;

import com.rameses.client.services.CommonService;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author wflores 
 */
class TimeTicker 
{
    private final static Object LOCKED = new Object();
    
    private UIApplication app;
    private Calendar calendar;
    private Date date; 
    private TaskImpl task;
    private Timer timer;
    
    public TimeTicker(UIApplication app) {
        this.app = app;
        timer = new Timer();
    }
    
    Date getDate() { return date; } 
    
    void start() {
        synchronized (LOCKED) { 
            Logger logger = Platform.getLogger();
            if (logger != null) logger.log("[TimeTicker] starting..."); 
            
            if (calendar == null) calendar = new GregorianCalendar(); 
            timer.schedule(new TimeFetcher(), 0);
            if (task == null) { 
                task = new TaskImpl(); 
                timer.schedule(task, 0, 1000);  
            } 
        } 
    }
    
    void restart() {
        synchronized (LOCKED) { 
            Logger logger = Platform.getLogger();
            if (logger != null) logger.log("[TimeTicker] restarting..."); 
            
            timer.schedule(new TimeFetcher(), 0);
        } 
    }
    
    private void dateChanged() {
        date = new java.sql.Timestamp(calendar.getTimeInMillis()); 
    }
    
    private class TaskImpl extends Task 
    {
        TimeTicker root = TimeTicker.this;
        
        public void run() {
            try {
                execute();
            } catch(Throwable t) {
                t.printStackTrace(); 
            } 
        } 
        
        private void execute() {
            synchronized (root.LOCKED) { 
                Calendar calendar = root.calendar; 
                calendar.add(Calendar.SECOND, 1); 
                root.dateChanged(); 
            } 
        }
    }
    
    private class TimeFetcher extends Task 
    {
        TimeTicker root = TimeTicker.this;
        
        public void run() {
            try {
                execute();
            } catch(Throwable t) {
                Logger logger = Platform.getLogger();
                if (logger != null) logger.log(t);  
            }             
        }
        
        private void execute() {
            synchronized (root.LOCKED) { 
                long timemillis = getServerTime();
                if (timemillis <= 0) { 
                    root.date = null;
                } else {
                    root.calendar.setTimeInMillis(timemillis); 
                    root.dateChanged();
                }
            } 
        }   
        
        private long getServerTime() {
            long timemillis = 0;
            try { 
                timemillis = new CommonService().getServerTime(); 
            } catch(Throwable t) {
                Logger logger = Platform.getLogger();
                if (logger != null) logger.log(t);             
            } 
            return timemillis; 
        }        
    }
}
