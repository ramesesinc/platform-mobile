/*
 * Platform.java
 *
 * Created on January 31, 2014, 11:50 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.client.android;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author wflores
 */
public final class Platform 
{
    private final static Object LOCKED = new Object();
    private static Platform instance;
    private static boolean debug;

    public static Platform getInstance() { return instance; }
    
    public static UIMain getMainActivity() { 
        UIApplication uiapp = getApplication();
        return (uiapp == null? null: uiapp.getMainActivity()); 
    } 
    
    public static AbstractActivity getCurrentActivity() { 
        UIApplication uiapp = getApplication();
        return (uiapp == null? null: uiapp.getCurrentActivity()); 
    }     
    
    public static UIApplication getApplication() { 
        UIApplication uiapp = (instance == null? null: instance.uiapp); 
        if (uiapp == null) {
            System.out.println("Platform is not yet initialize"); 
        }
        return uiapp;
    }
        
    public static void runAsync(Runnable runnable) {
        synchronized (LOCKED) { 
            if (runnable == null) return;
            
            getTaskManager().schedule(runnable, 1); 
        }
    }
    
    static void setApplication(UIApplication uiapp) {
        synchronized (LOCKED) { 
            if (instance == null) { 
                if (uiapp == null) {
                    throw new RuntimeException("UIApplication must not be null");
                }

                instance = new Platform(uiapp); 
            }
        }      
    }
    
    public static TaskManager getTaskManager() {
        return (instance == null? null: instance.taskManager); 
    }
    
    public static Logger getLogger() {
        UIApplication uiapp = getApplication();
        return (uiapp == null? null: uiapp.getLogger()); 
    }
    
    public static void setDebug(boolean debug) {
        synchronized (LOCKED) { 
            Platform.debug = debug; 
            UIApplication uiapp = getApplication(); 
            Logger logger = (uiapp==null? null: uiapp.getLogger()); 
            if( logger != null ) logger.setEnabled(debug); 
        } 
    }
    
    
    private UIApplication uiapp;
    private TaskManager taskManager;
    private Map<String, AbstractActivity> activities;
    
    private Platform(UIApplication uiapp) {
        this.uiapp = uiapp;
        activities = new LinkedHashMap(); 
        taskManager = new TaskManager();
    }
    
    void register(AbstractActivity activity) {
        synchronized (LOCKED) {
            if (activity == null) return;
            
            String name = activity.getClass().getName();
            if (activities.containsKey(name)) return;
            
            activities.put(name, activity); 
        }
    }
    
    void unregister(AbstractActivity activity) {
        synchronized (LOCKED) {
            if (activity == null) return;
            
            String name = activity.getClass().getName(); 
            activities.remove(name); 
        } 
    } 
    
    public AbstractActivity find(Class activityClass) {
        if (activityClass == null) return null;
        
        return find(activityClass.getName());
    }
    
    public AbstractActivity find(String name) {
        synchronized (LOCKED) {
            if (name == null || name.length() == 0) return null;
            
            return activities.get(name); 
        } 
    }
    
    public void closeAll() {
        closeAll(null);
    }
    
    public void closeAll(AbstractActivity except) {
        List<String> removallist = new ArrayList();         
        Iterator<AbstractActivity> itr = activities.values().iterator(); 
        while (itr.hasNext()) {
            AbstractActivity ui = itr.next();
            if (ui instanceof UIMain) continue;            
            if (!ui.isCloseable()) continue; 
            if (except != null && except.equals(ui)) continue;
            
            try { 
                ui.finish();
            } catch(Throwable t) {
                t.printStackTrace();
            } finally {
                removallist.add(ui.getClass().getName()); 
            }
        } 
        
        while (!removallist.isEmpty()) {
            activities.remove(removallist.remove(0)); 
        }        
    }
    
    public void disposeAll() {
        disposeAllExcept(null); 
    }
    
    public void disposeAllExcept(AbstractActivity except) {
        List<String> removallist = new ArrayList();         
        Iterator<AbstractActivity> itr = activities.values().iterator(); 
        while (itr.hasNext()) {
            AbstractActivity ui = itr.next();
            if (ui instanceof UIMain) continue;            
            if (except != null && except.equals(ui)) continue;
            
            try { 
                ui.dispose();
            } catch(Throwable t) {
                t.printStackTrace();
            } finally {
                removallist.add(ui.getClass().getName()); 
            }
        } 
        
        while (!removallist.isEmpty()) {
            activities.remove(removallist.remove(0)); 
        } 
        
    }    
}
