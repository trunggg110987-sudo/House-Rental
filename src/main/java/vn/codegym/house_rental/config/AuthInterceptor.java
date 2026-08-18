package vn.codegym.house_rental.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import vn.codegym.house_rental.model.User;

// [CẢI TIẾN]: Bộ lọc Interceptor kiểm tra đăng nhập và phân quyền truy cập theo Role (ADMIN/HOST/USER)
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        // [CẢI TIẾN]: Nếu chưa đăng nhập, bắt buộc quay về trang Login
        if (currentUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        // [CẢI TIẾN]: Kiểm tra quyền Admin đối với các route /admin/**
        if (uri.startsWith("/admin") && currentUser.getRole() != User.Role.ROLE_ADMIN) {
            response.sendRedirect("/?errorMessage=Ban khong co quyen truy cap trang Admin");
            return false;
        }

        // [CẢI TIẾN]: Kiểm tra quyền Host đối với thao tác đăng nhà và duyệt đặt phòng
        if ((uri.startsWith("/houses/create") || uri.startsWith("/houses/edit") || uri.startsWith("/bookings/host-requests"))
                && currentUser.getRole() != User.Role.ROLE_HOST && currentUser.getRole() != User.Role.ROLE_ADMIN) {
            response.sendRedirect("/?errorMessage=Chi co chu nha moi co quyen thuc hien thao tac nay");
            return false;
        }

        return true;
    }
}