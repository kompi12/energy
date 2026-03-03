package com.example.energy.security.jwt;

import com.example.energy.service.security.AuthUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthUserDetailsService uds;

    public JwtAuthFilter(JwtService jwtService, AuthUserDetailsService uds) {
        this.jwtService = jwtService;
        this.uds = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ✅ vidiš u konzoli za svaki request
        System.out.println("[JWT] " + method + " " + path);

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            System.out.println("[JWT] -> NO Authorization header");
            chain.doFilter(request, response);
            return;
        }

        System.out.println("[JWT] -> Authorization present");

        String token = auth.substring(7);

        if (!jwtService.isValid(token)) {
            System.out.println("[JWT] -> TOKEN INVALID");
            // ✅ odmah vrati 401 s porukom (da vidiš u Network > Response)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write("""
              {"success":false,"status":401,"message":"JWT invalid or expired. Login again.","data":null}
            """);
            return;
        }

        String username = jwtService.extractUsername(token);
        System.out.println("[JWT] -> username=" + username);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = uds.loadUserByUsername(username);

            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("[JWT] -> AUTH SET in SecurityContext");
        }

        chain.doFilter(request, response);
    }
}
