package com.songbirds.filters;

import com.songbirds.app.MongoDBClient;
import com.songbirds.controller.DataController;
//import com.songbirds.util.MutableHttpServletRequest;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckAuthCookieFilter implements Filter {

    //private Logger logger = LoggerFactory.getLogger(getClass());

    private MongoDBClient mongoClient = MongoDBClient.getInstance();
    private List<String> unauthenticatedRoutes = new ArrayList<String>(Arrays.asList("/login", "/callback/", "/prime_login", "/", "/friends", "/manifest.json", "/favicon.ico", "/profile-page", "/sign-in-page", "/register-page", "/landing-page"));
    private static final Logger LOGGER = Logger.getLogger(CheckAuthCookieFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        Cookie[] cookies = httpServletRequest.getCookies();

        boolean authenticatedFlag = false;

        LOGGER.log(Level.INFO, "Request method URI: " + httpServletRequest.getRequestURI());

        if (unauthenticatedRoutes.contains(httpServletRequest.getRequestURI()) || httpServletRequest.getRequestURI().contains("static")) {
            chain.doFilter(request, response);
            return;
        }

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                //logger.debug(cookie.getName() + " : " + cookie.getValue());
                //System.out.println("Cookie Session: " + cookie.getName());
                //System.out.println("Cookie Value: " + cookie.getValue());

                if (cookie.getName().equalsIgnoreCase("SESSION")) {
                    byte[] decodedBytes = Base64.getDecoder().decode(cookie.getValue());
                    String sessionID = new String(decodedBytes);
                    if( mongoClient.sessionCookieExists(sessionID) != null){
                        //mutableRequest.putHeader("Authorization", URLDecoder.decode(cookie.getValue(), "utf-8"));
                        authenticatedFlag = true;
                    }
                }
            }
        }

        if (authenticatedFlag) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(401);
        }

        return;
    }

}
