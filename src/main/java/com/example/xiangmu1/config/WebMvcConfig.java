package com.example.xiangmu1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    private final JwtInterceptor jwtInterceptor;
    private final OptionalAuthInterceptor optionalAuthInterceptor;

    public WebMvcConfig(JwtInterceptor jwtInterceptor, OptionalAuthInterceptor optionalAuthInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
        this.optionalAuthInterceptor = optionalAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(optionalAuthInterceptor)
                .addPathPatterns(
                        "/article/list",
                        "/article/detail",
                        "/article/hot",
                        "/user/home"
                )
                .excludePathPatterns(SWAGGER_PATHS);

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(
                        "/article/add",
                        "/article/edit",
                        "/article/delete",
                        "/article/like",
                        "/article/unlike",
                        "/favorite/add",
                        "/favorite/remove",
                        "/favorite/list",
                        "/comment/add",
                        "/comment/delete",
                        "/ai/**"
                )
                .excludePathPatterns(SWAGGER_PATHS);
    }
}
