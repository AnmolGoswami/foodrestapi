package in.foodies.foodiesapi.config;

import in.foodies.foodiesapi.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Skip filter for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip filter for permitAll endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/login") || requestURI.startsWith("/api/auth/register") ||
                requestURI.startsWith("/api/foods")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = jwtService.getTokenFromHeader(request);
            if (jwt != null && jwtService.validateToken(jwt)) {
                String username = jwtService.getUsernameFromToken(jwt);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("Authenticated user: " + username + ", authorities: " + userDetails.getAuthorities());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JWT validation failed: " + e.getMessage());
            // Continue the filter chain instead of throwing an exception
        }

        filterChain.doFilter(request, response);
    }
}