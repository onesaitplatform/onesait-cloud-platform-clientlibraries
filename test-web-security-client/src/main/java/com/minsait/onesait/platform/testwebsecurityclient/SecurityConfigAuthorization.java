//package com.minsait.onesait.platform.testwebsecurityclient;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
//
//
//@Configuration
//public class SecurityConfigAuthorization extends ResourceServerConfigurerAdapter {
//
//	
//	  @Override
//	  public void configure(HttpSecurity http) throws Exception {
//	  	http
//	  		.authorizeRequests()
//	  			.antMatchers("/**/check").permitAll()
//	  			.antMatchers("/**/login").permitAll()
//	  			//.antMatchers("/**/secured/**").authenticated()
//	  			.anyRequest().authenticated();
//	  }
//	  	
//	
//}
