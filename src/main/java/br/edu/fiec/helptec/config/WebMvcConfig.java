package br.edu.fiec.helptec.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MyRequestInterceptor myRequestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the interceptor
        registry.addInterceptor(myRequestInterceptor)
                .addPathPatterns("/api/**")         // Intercepts all paths starting with /api/
                .excludePathPatterns("/api/auth/**"); // Excludes authentication paths from interception
    }
}
