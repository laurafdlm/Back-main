package edu.uclm.esi.fakeaccountsbe.security;

import edu.uclm.esi.fakeaccountsbe.services.UserService;
import edu.uclm.esi.fakeaccountsbe.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("token");
        System.out.println("Token recibido: " + token); // Log para verificar el token
        if (token != null && !token.isEmpty()) {
            User user = userService.findByToken(token);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }



}
