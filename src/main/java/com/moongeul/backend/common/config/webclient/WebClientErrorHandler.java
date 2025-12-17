package com.moongeul.backend.common.config.webclient;

import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.InternalServerException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientErrorHandler {

    public static Mono<Throwable> handleApiError(ClientResponse response, String apiName) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("No error body content")
                .flatMap(errorBody -> {
                    log.error("[{}] API Error: Status Code: {}, Body: {}", apiName, response.statusCode(), errorBody);

                    if (response.statusCode().value() == 401) {
                        return Mono.error(new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage()));
                    } else if (response.statusCode().is4xxClientError()) {
                        return Mono.error(new BadRequestException(ErrorStatus.INVALID_TOKEN_REQUEST.getMessage()));
                    } else {
                        return Mono.error(new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage()));
                    }
                });
    }
}
