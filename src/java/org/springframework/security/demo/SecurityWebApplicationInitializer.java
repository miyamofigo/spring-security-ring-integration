package org.springframework.security.demo;

import org.springframework.security.web.context.*;

public class SecurityWebApplicationInitializer 
 extends AbstractSecurityWebApplicationInitializer {

  public SecurityWebApplicationInitializer() {  
    super(SecurityConfig.class);
  }
}
