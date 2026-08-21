package vn.codegym.house_rental.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // [CẢI TIẾN]: Tiêm AuthInterceptor để đăng ký bảo vệ hệ thống
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        String uploadPath = uploadDir.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    // [CẢI TIẾN]: Đăng ký đường dẫn bảo vệ và ngoại lệ cho phép truy cập tự do
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**", "/profile/**", "/bookings/**", "/houses/create", "/houses/*/edit", "/houses/*/delete", "/houses/*/status-period", "/houses/my-houses", "/notifications/**")
                .excludePathPatterns("/login", "/register", "/", "/houses/{id:[0-9]+}", "/uploads/**", "/css/**", "/js/**");
    }
}