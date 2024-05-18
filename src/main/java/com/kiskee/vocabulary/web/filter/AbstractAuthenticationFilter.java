package com.kiskee.vocabulary.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import com.kiskee.vocabulary.web.advice.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Getter
@AllArgsConstructor
public abstract class AbstractAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    protected void handleRequestException(Exception exception, HttpServletResponse response) throws IOException {
        log.error(exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .errors(Map.of("errorResponse", exception.getMessage()))
                .timestamp(Instant.now().atZone(TimeZoneContextHolder.getTimeZone()))
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
