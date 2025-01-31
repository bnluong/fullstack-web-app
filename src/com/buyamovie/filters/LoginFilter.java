package com.buyamovie.filters;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.buyamovie.usersession.UserSession;

/**
 * Servlet Filter implementation class LoginFilter
 */
//@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {

    /**
     * Default constructor. 
     */
    public LoginFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// place your code here

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if this URL is allowed to access without logging in
        if(this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }
		
        HttpSession session = httpRequest.getSession();
		UserSession currentUser = (UserSession) session.getAttribute("user_session");

        // Redirect to login page if the "user" attribute doesn't exist in session
        if(currentUser == null) {
            httpResponse.sendRedirect("login.html");
        } else {
    		// pass the request along the filter chain
            chain.doFilter(request, response);
        }
	}

	private boolean isUrlAllowedWithoutLogin(String requestURI) {
		if(requestURI.equals("/buy-a-movie/cart.html"))
			return false;
		return true;
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}
}
