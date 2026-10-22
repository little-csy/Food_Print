package com.foodprint.security;

// TODO [Engineer A]: Implement JWT authentication filter
// Reference: BACKEND_DESIGN.md Section 10.1
//
// This filter:
// 1. Extracts JWT from Authorization header
// 2. Verifies JWT using Supabase anon-key (HMAC256)
// 3. Extracts userId from JWT subject
// 4. Sets SecurityContext for downstream controllers
//
// JWT is issued by Supabase Auth, NOT by this backend.

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        // TODO: Extract Bearer token from Authorization header
        // TODO: Verify JWT with Supabase anon-key
        // TODO: Set SecurityContext with userId
        chain.doFilter(request, response);
    }
}
