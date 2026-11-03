/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.cloud.parent.web.common.filter;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.opensabe.common.http.SemicolonPathGuard;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

/**
 * Rejects requests whose URI path contains {@code ;} (matrix-variable style bypass).
 * Query string semicolons are ignored.
 */
@Log4j2
public class RejectSemicolonServletFilter extends OncePerRequestFilter implements Ordered {

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 20;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        if (SemicolonPathGuard.containsSemicolonInPath(requestUri)) {
            log.warn("RejectSemicolonServletFilter: reject path with semicolon uri={}", requestUri);
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Semicolon in URI path is not allowed");
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
