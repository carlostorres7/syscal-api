package com.syscal.apisyscal.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

import com.syscal.apisyscal.security.services.UserDetailsServiceImpl;


@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtils jwtUtils;
  
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
  
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
  
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
      try {
        String jwt = parseJwt(request);
          System.out.println(request);
        log.info("this is jwt {}",jwt);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

          String username = jwtUtils.getUserNameFromJwtToken(jwt);
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);
          UsernamePasswordAuthenticationToken authentication = 
              new UsernamePasswordAuthenticationToken(userDetails,
                                                      null,
                                                      userDetails.getAuthorities());
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } else {
            filterChain.doFilter(request, response);
            return;
        }
      } catch (Exception e) {
        logger.error("Cannot set user authentication: {}", e);
      }
  

    }
  
    private String parseJwt(HttpServletRequest request) {
      String jwt = jwtUtils.getJwtFromCookies(request);
      log.info(jwt);
      return jwt;
    }

}