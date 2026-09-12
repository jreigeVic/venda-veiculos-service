package com.soat.vendaveiculos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Token";

    private final String tokenEsperado;

    public InternalTokenFilter(@Value("${app.internal-token}") String tokenEsperado) {
        this.tokenEsperado = tokenEsperado;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/interno/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tokenRecebido = request.getHeader(HEADER);
        if (!Objects.equals(tokenEsperado, tokenRecebido)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token interno inválido ou ausente");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
