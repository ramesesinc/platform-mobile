/*
 * ServiceProxyInvocationHandler.java
 * Created on September 23, 2011, 11:29 AM
 *
 * Rameses Systems Inc
 * www.ramesesinc.com
 *
 */
package com.rameses.service;

import com.rameses.common.AsyncHandler;
import com.rameses.common.AsyncRequest;
import com.rameses.common.AsyncResponse;
import com.rameses.util.AppException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author jzamss
 */
public class ServiceProxyInvocationHandler implements InvocationHandler{
    
    private static final ExecutorService thread = Executors.newFixedThreadPool(10);
    
    private ServiceProxy proxy;
    
    /** Creates a new instance of ServiceProxyInvocationHandler */
    public ServiceProxyInvocationHandler(ServiceProxy p) {
        this.proxy = p;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable 
    {
        if (method.getName().equals("toString")) return proxy.getClass().getName();
                
        try {
            if( args == null ) {
                return this.proxy.invoke(method.getName());
            } else {
                AsyncHandler handler = null;
                if(args[args.length-1] instanceof AsyncHandler ) {
                    Object[] newArgs = new Object[args.length-1];
                    for(int i=0; i<newArgs.length; i++) {
                        newArgs[i] = args[i];
                    }
                    handler = (AsyncHandler)args[args.length-1];
                    args = newArgs;
                }
                
                if(handler !=null) {
                    thread.submit( new AsyncTask(method.getName(), args, handler) );
                    return null;
                } else {
                    return this.proxy.invoke( method.getName(), args );
                }
            }
        } 
        catch(Throwable t) 
        {
            t.printStackTrace();
            if (t instanceof AppException)
                throw t;
            else if (t instanceof RuntimeException) 
                throw (RuntimeException) t;
            else
                throw new RuntimeException(t.getMessage(), t);
        }
    }
    
    class AsyncTask implements Runnable {
        private String methodName;
        private Object[] args;
        private AsyncHandler handler;
        
        public AsyncTask( String methodName, Object[] args, AsyncHandler handler ) {
            this.methodName = methodName;
            this.args = args;
            this.handler = handler;
            if( this.handler == null ) {
                this.handler = new AsyncHandler() {
                    public void onMessage(AsyncResponse ar) {
                        System.out.println("unhandled message. No handler passed " + ar.getNextValue());
                    }
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                };
            }
        }
        public void run() {
            try {
                Object result = proxy.invoke( methodName, args );
                
                if(result!=null && (result instanceof AsyncRequest) ) {
                    
                    //get the location of queue
                    AsyncRequest arequest = (AsyncRequest)result;
                    
                    String provider = arequest.getProvider();
                    String channel = arequest.getChannel();
                    AsyncPoller poller = new AsyncPoller(proxy.getConf(), provider, channel);
                    boolean completed = false;
                    
                    while(!completed) {
                        completed = true;
                        //this is blocking until message arrives
                        AsyncResponse response = poller.poll();
                        if(response.getStatus() != AsyncResponse.COMPLETED) {
                            completed = false;
                        }
                        handler.onMessage(response);
                    }
                    
                } else {
                    handler.onMessage( new AsyncResponse(result) );
                }
            } catch(Exception e) {
                handler.onError( e );
            }
        }
    }
    
}
