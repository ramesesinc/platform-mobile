/*
 * DBManager.java
 *
 * Created on January 28, 2014, 10:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.db.android;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author wflores
 */
public final class DBManager 
{
    private static DBManager instance;
    private static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager(); 
        }
        return instance; 
    }
    
    static synchronized void bind(String name, AbstractDB db) {
        Map map = getInstance().databases;
        if (map.containsKey(name)) return;
        
        map.put(name, db); 
    }
    
    public static synchronized AbstractDB get(String name) {
        return getInstance().databases.get(name);
    }
    
    
    private Map<String, AbstractDB> databases; 
    
    private DBManager() {
        databases = new HashMap(); 
    }
    
}
