/*
 * AsyncPoller.java
 *
 * Created on January 20, 2013, 3:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.service;

import com.rameses.common.AsyncResponse;

import java.util.Map;

/**
 *
 * @author Elmo
 */
public class AsyncPoller extends AbstractServiceProxy {
    
    private String connection;
    private String channel;
    
    /**
     * Creates a new instance of AsyncPoller
     */
    public AsyncPoller(Map conf, String connection, String channel) {
        super(null, conf);
        this.connection = connection;
        this.channel = channel;
    }
    
    public AsyncResponse poll() throws Exception {
        String appContext = (String) super.conf.get("app.context");
        String path = "poll/" + appContext+"/"+connection+"/"+channel;
        String cluster = (String) super.conf.get("app.cluster");
        if( cluster !=null ) path = cluster + "/" + path;
        
        //add a version to enure it will always be new
        Object result = client.post( path+"?" + (System.currentTimeMillis()) );
        
        if( !(result instanceof AsyncResponse )) {
            return new AsyncResponse(result);
        }
        else {
            return (AsyncResponse)result;
        }
    }
    
    
}
