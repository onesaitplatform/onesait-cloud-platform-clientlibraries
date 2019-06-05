package com.minsait.onesait.platform.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//import com.minsait.onesait.platform.web.security.client.login.LoginService;


/**
 * This class contains E-Pricing REST service controller.
 * 
 * @author indra
 * @version 1.0
 */
@Component
@RestController
@RequestMapping({"/test"})
public class TestRest {
	
//	@Autowired
//	private LoginService loginService;
	
	public class DummyHostnameVerifier implements HostnameVerifier {
	    @Override
	    public boolean verify(String s, SSLSession sslSession) {
	        return true;
	    }
	}

	public TestRest() {
		HttpsURLConnection.setDefaultHostnameVerifier(new DummyHostnameVerifier());
		
	}
	
	@RequestMapping(value = { "/login" }, method = RequestMethod.GET)
	public String login(@RequestParam("user") String user, @RequestParam("password") String password) {
		try {
			
//			String token=loginService.login(user, password).getToken();
			return "token";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = { "/secured/authenticated" }, method = RequestMethod.GET)
	public String testSecuredAutehnticated() {

		Logger.getLogger(getClass().getName()).log(Level.INFO, "SECURED AUTHENTICATED REST TESTController test service");
		try {
			return "SECURED AUTHENTICATED REST TEST service controller is alive";
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage());
			return null;
		}
	}
	
	@PreAuthorize("hasAnyRole('ROLE_role4')")
	@RequestMapping(value = { "/secured/authenticated2" }, method = RequestMethod.GET)
	public String testSecuredAutehnticated2() {

		Logger.getLogger(getClass().getName()).log(Level.INFO, "SECURED AUTHENTICATED2 REST TESTController test service");
		try {
			return "SECURED AUTHENTICATED2 REST TEST service controller is alive";
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage());
			return null;
		}
	}

	@PreAuthorize("hasAuthority('role1')")
	@RequestMapping(value = { "/secured/test" }, method = RequestMethod.GET)
	public String testSecuredController() {

		Logger.getLogger(getClass().getName()).log(Level.INFO, "SECURED REST TESTController test service");
		try {
			return "SECURED REST TEST service controller is alive";
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage());
			return null;
		}
	}
	
	@PreAuthorize("hasAuthority('fake_authority')")
	@RequestMapping(value = { "/secured/testToFail" }, method = RequestMethod.GET)
	public String testSecuredToFailController() {

		Logger.getLogger(getClass().getName()).log(Level.INFO, "SECURED REST TESTController test service");
		try {
			return "SECURED REST TEST service controller is alive";
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage());
			return null;
		}
	}
	
	@RequestMapping(value = { "/test" }, method = RequestMethod.GET)
	public String testController() {

		Logger.getLogger(getClass().getName()).log(Level.INFO, "REST TEST Controller test service");
		try {
			return "REST TEST service controller is alive";
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage());
			return null;
		}
	}
	
	@RequestMapping(value = { "/check-token" }, method = RequestMethod.POST)
	public String checkTokenController() {

		Logger.getLogger(getClass().getName()).log(Level.INFO, "REST TEST Controller test service");
		try {
			return "REST TEST service controller is alive";
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage());
			return null;
		}
	}


}
// end EPricingController