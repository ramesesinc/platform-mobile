/*
 * CommonService.java
 *
 * Created on January 22, 2014, 11:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.client.services;

import java.util.Date;

/**
 *
 * @author wflores
 */
public class CommonService extends AbstractService 
{
    public String getServiceName() { return "MobileCommonService"; }

    public long getServerTime() { 
        return (Long) invoke("getServerTime", null);
    } 
    
    public Date getServerDate() { 
        long time = getServerTime(); 
        return new java.sql.Timestamp(time);
    } 
}
