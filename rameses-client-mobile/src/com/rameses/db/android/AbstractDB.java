/*
 * AbstractDB.java
 *
 * Created on January 28, 2014, 11:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.db.android;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.rameses.client.android.Platform;
import com.rameses.client.android.UIApplication;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

/**
 *
 * @author wflores
 */
public abstract class AbstractDB extends SQLiteOpenHelper
{
    private String databaseName;
    private int databaseVersion;
    
    public AbstractDB(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion); 
        this.databaseName = databaseName;
        this.databaseVersion = databaseVersion;
        DBManager.bind(databaseName, this); 
    }
    
    public final String getName() { return databaseName; } 
    public final int getVersion() { return databaseVersion; }
    
//    protected DBContext createDBContext(SQLiteDatabase sqldb) {
//        return new DBContext(sqldb); 
//    } 

    protected void onOpenProcess(SQLiteDatabase sqldb) {}
    protected void onCreateProcess(SQLiteDatabase sqldb) {}
    protected void onUpgradeProcess(SQLiteDatabase sqldb, int i0, int i1) {}
        
    public final void onOpen(SQLiteDatabase sqldb) {
        super.onOpen(sqldb);
        onOpenProcess(sqldb);
    }

    public final void onCreate(SQLiteDatabase sqldb) {
        onCreateProcess(sqldb);
    }

    public final void onUpgrade(SQLiteDatabase sqldb, int i0, int i1) { 
        onUpgradeProcess(sqldb, i0, i1); 
    }
    
    protected void loadDBResource(SQLiteDatabase sqldb, String name) {
        try {
            loadDBResourceImpl(sqldb, name);
        } catch(RuntimeException re) {
            throw re; 
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private void loadDBResourceImpl(SQLiteDatabase sqldb, String name) throws Exception {
        UIApplication uiapp = Platform.getApplication();
        int resid = uiapp.getResources().getIdentifier(name, "xml", uiapp.getPackageName()); 
        if (resid == 0) {
            System.out.println("'"+name+".xml' resource not found");
            return;
        }
        
        XmlResourceParser xml = uiapp.getResources().getXml(resid);
        xml.next();
        
        List<String> sqls = new ArrayList();
        List<String> paths = new ArrayList();
        int eventType = xml.getEventType(); 
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                paths.add(xml.getName()); 
                
            } else if (eventType == XmlPullParser.END_TAG) {
                if (!paths.isEmpty()) {
                    paths.remove(paths.size()-1);
                }
            } else if (eventType == XmlPullParser.TEXT) {
                String s = join(paths, "/");                
                if (s.equals("db/sql")) {
                    sqls.add(xml.getText()); 
                } 
            } 
            eventType = xml.next();
        }
        
        while (!sqls.isEmpty()) {
            String sqlstring = sqls.remove(0);
            sqldb.execSQL(sqlstring);
        }
    }    
    
    private String join(List<String> list, String delim) {
        StringBuffer sb = new StringBuffer();
        for (String str : list) {
            if (sb.length() > 0) sb.append("/"); 
            
            sb.append(str);
        }
        return sb.toString(); 
    }        
}
