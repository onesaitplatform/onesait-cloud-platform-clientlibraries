/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.controller;

import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.web.security.client.login.LoginService;

//import com.minsait.onesait.platform.web.security.client.login.LoginService;

/**
 * This class contains E-Pricing REST service controller.
 * 
 * @author indra
 * @version 1.0
 */
@Component
@RestController
@RequestMapping({ "/test" })
public class TestRest {

	@Autowired
	private LoginService loginService;

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

			String token = loginService.login(user, password).getToken();
			return token;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = { "/secured/authenticated" }, method = RequestMethod.GET)
	public String testSecuredAutehnticated(HttpServletRequest request, Authentication authentication1) {
		Principal principal = request.getUserPrincipal();
		principal.getName();

		System.out.println(authentication1);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		System.out.println(authentication.getPrincipal().getClass().getName());
		String currentPrincipalName = authentication.getName();
		System.out.println(currentPrincipalName);

		Logger.getLogger(getClass().getName()).log(Level.INFO,
				"SECURED AUTHENTICATED REST TESTController test service");
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

		Logger.getLogger(getClass().getName()).log(Level.INFO,
				"SECURED AUTHENTICATED2 REST TESTController test service");
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