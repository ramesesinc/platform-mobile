/*
 * AsyncHandler.java
 *
 * Created on October 24, 2010, 1:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.common;

import java.util.HashMap;

public class AsyncRequest extends HashMap  {
    
    public String getProvider() {
        return (String) get("provider");
    }
    
    public void setProvider(String conn) {
        put("provider", conn);
    }

    public String getChannel() {
        return (String) get("channel");
    }
    
    public void setChannel(String channel) {
        put("channel", channel);
    }
    
    
    
}
