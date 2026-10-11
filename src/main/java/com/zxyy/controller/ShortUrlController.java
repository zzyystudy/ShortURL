package com.zxyy.controller;

import com.zxyy.dto.CreateShortUrlRequest;
import com.zxyy.dto.CreateShortUrlResponse;
import com.zxyy.entity.UrlMap;
import com.zxyy.mq.TransformEventPublisher;
import com.zxyy.service.UrlMapService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** 创建短链和访问短链的 HTTP 入口。 */
@RestController
@RequiredArgsConstructor
public class ShortUrlController {

    private final UrlMapService urlMapService;
    private final TransformEventPublisher eventPublisher;

    @PostMapping("/short-links")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateShortUrlResponse create(@Valid @RequestBody CreateShortUrlRequest request) {
        UrlMap urlMap = urlMapService.createShortUrl(request);
        return new CreateShortUrlResponse(urlMap.getId(), urlMap.getShortUrl(),
                urlMap.getCompressionCode(), urlMap.getLongUrl());
    }

    @GetMapping("/{compressionCode:[0-9a-zA-Z]{6}}")
    public ResponseEntity<Void> redirect(@PathVariable String compressionCode, HttpServletRequest request) {
        UrlMap urlMap = urlMapService.resolveShortUrl(compressionCode);
        eventPublisher.publish(urlMap, request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(urlMap.getLongUrl()))
                .build();
    }
}
