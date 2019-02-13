/*
 * LoginService.java
 *
 * Created on January 22, 2014, 11:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.client.services;

import com.rameses.client.android.AppContext;
import com.rameses.client.android.SessionContext;
import com.rameses.util.Encoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author wflores
 */
public class LoginService extends AbstractService 
{
    public String getServiceName() { return "LoginService"; }

    public void login(String username, String password) {
        String encpwd = Encoder.MD5.encode(password, username); 
        Map param = new HashMap();
        param.put("username", username);
        param.put("password", encpwd);
        Map result = (Map) invoke("login", param);
        
        SessionProviderImpl sessImpl = new SessionProviderImpl(result);
        SessionContext sess = AppContext.getSession();
        sess.setProvider(sessImpl); 
        sess.set("encpwd", encpwd); 
        
        Map authOpts = (Map) result.remove("AUTH_OPTIONS");
        if (authOpts != null) {
            Iterator keys = authOpts.keySet().iterator(); 
            while (keys.hasNext()) { 
                String key = keys.next().toString(); 
                sess.set(key, authOpts.get(key)); 
            } 
        } 
        
        Map report = (Map) result.remove("REPORT");
        if (report != null) {
            Iterator keys = report.keySet().iterator(); 
            while (keys.hasNext()) { 
                String key = keys.next().toString(); 
                sess.set(key, report.get(key)); 
            } 
        } 
    } 
}
