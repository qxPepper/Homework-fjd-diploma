package ru.netology.homeworkfjddiploma.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.netology.homeworkfjddiploma.service.AuthService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        final String authTokenHeader = request.getHeader("auth-token");
        String username = null;
        String jwt = null;

        try {
            if (authTokenHeader != null) {
                if (authTokenHeader.startsWith("Bearer ")) {
                    jwt = authTokenHeader.substring(7);
                } else {
                    jwt = authTokenHeader;
                }
                username = jwtUtil.extractUsername(jwt);
            }

            if (username != null) {
                String listOfAuthorities = jwtUtil.extractAuthorities(jwt);
                List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(listOfAuthorities);

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
            chain.doFilter(request, response);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }
}
