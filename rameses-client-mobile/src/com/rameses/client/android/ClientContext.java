/*
 * ClientContext.java
 *
 * Created on January 22, 2014, 12:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.client.android;

import com.rameses.client.interfaces.DeviceContext;
import java.util.Map;

/**
 *
 * @author wflores
 */
public final class ClientContext 
{
    private final static Object LOCK = new Object();
    private static TaskManager taskManager;
    
    static {
        synchronized (LOCK) {
            taskManager = new TaskManager();
        }
    }
    
    private static ClientContext current;     
    public static ClientContext getCurrentContext() { return current; }    
    static synchronized void setCurrentContext(ClientContext newContext) {
        ClientContext old = current;
        if (old != null) old.close();
        
        current = newContext; 
        if (current != null) current.init();
    }
        
    
    private UIApplication uiapp;
    private AppContext appContext;
    
    ClientContext(UIApplication uiapp, AppContext appContext) {
        this.uiapp = uiapp;
        this.appContext = appContext; 
    }

    private void init() { 
        AppContext.setInstance(this.appContext); 
    } 
    
    public Map getAppEnv() { 
        return appContext.getEnv();  
    } 
    
    public Map getEnv() {
        SessionContext sess = appContext.getSession(); 
        return (sess == null? null: sess.getHeaders()); 
    }
        
    public TaskManager getTaskManager() { 
        return ClientContext.taskManager; 
    }
    
    void close() { 
        AppContext.setInstance(null);
        
        try { 
            taskManager.close();  
        } catch(Throwable t) { 
            t.printStackTrace(); 
        } 
        
        try { 
            appContext.close(); 
        } catch(Throwable t) { 
            t.printStackTrace(); 
        }         
    }
}
