/*
 * NewEmptyJUnitTest.java
 * JUnit based test
 *
 * Created on January 22, 2014, 1:21 PM
 */

package test;

import com.rameses.client.android.ClientContext;
import com.rameses.client.android.DeviceAppLoader;
import com.rameses.client.services.LoginService;
import java.util.HashMap;
import java.util.Map;
import junit.framework.*;

/**
 *
 * @author compaq
 */
public class NewEmptyJUnitTest extends TestCase {
    
    public NewEmptyJUnitTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void testMain() throws Exception {
        Map appenv = new HashMap();
        appenv.put("app.host", "localhost:8070");
        appenv.put("app.context", "clfc");
        appenv.put("app.cluster", "osiris3");
        
        DeviceAppLoader.load(appenv, null);
        System.out.println("appenv-> " + ClientContext.getCurrentContext().getAppEnv()); 
        
        LoginService loginSvc = new LoginService(); 
        loginSvc.login("cao", "1234");
        System.out.println("env-> " + ClientContext.getCurrentContext().getEnv()); 
        boolean allowed = com.rameses.client.android.SecurityManager.checkPermission(null, null, "system");
        System.out.println("allowed-> " + allowed);
    }
}
