package com.kiskee.vocabulary.web.filter;

import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class TimeZoneRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String timeZone = request.getHeader("X-Time-Zone");

            String timeZoneNonNull = Objects.requireNonNullElse(timeZone, "UTC");

            TimeZoneContextHolder.setTimeZone(timeZoneNonNull);

            filterChain.doFilter(request, response);
        } finally {
            TimeZoneContextHolder.clear();
        }
    }
}
