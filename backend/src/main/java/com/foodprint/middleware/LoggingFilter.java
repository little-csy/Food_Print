package com.foodprint.middleware;

// TODO [Shared]: Implement request/response logging filter
// Logs method, URI, status code, and response time for each request.

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        // TODO: Log request start time, method, URI
        chain.doFilter(request, response);
        // TODO: Log response status, duration
    }
}
