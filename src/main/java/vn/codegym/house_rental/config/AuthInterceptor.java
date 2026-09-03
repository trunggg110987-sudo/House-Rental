package vn.codegym.house_rental.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.UserRepository;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Kiểm tra xem tài khoản có bị Admin khóa trong DB không
        Optional<User> dbUser = userRepository.findById(currentUser.getId());
        if (dbUser.isEmpty() || !Boolean.TRUE.equals(dbUser.get().getActive())) {
            session.invalidate();
            response.sendRedirect("/login?errorMessage=Tai khoan cua ban da bi khoa. Vui long lien he Quan tri vien.");
            return false;
        }

        // Cập nhật lại đối tượng user mới nhất từ DB vào Session
        session.setAttribute("currentUser", dbUser.get());
        currentUser = dbUser.get();

        // Kiểm tra quyền Admin đối với các route /admin/**
        if (uri.startsWith("/admin") && currentUser.getRole() != User.Role.ROLE_ADMIN) {
            response.sendRedirect("/?errorMessage=Ban khong co quyen truy cap trang Admin");
            return false;
        }

        // Kiểm tra quyền Host đối với thao tác của chủ nhà
        boolean isHostAction = uri.startsWith("/houses/create") ||
                uri.startsWith("/bookings/host-requests") ||
                uri.startsWith("/bookings/host-bookings") ||
                uri.startsWith("/bookings/host-reviews") ||
                uri.startsWith("/bookings/reviews/") ||
                uri.startsWith("/bookings/income-statistics") ||
                uri.matches("^/houses/\\d+/(edit|delete|status-period).*");

        if (isHostAction && currentUser.getRole() != User.Role.ROLE_HOST && currentUser.getRole() != User.Role.ROLE_ADMIN) {
            response.sendRedirect("/?errorMessage=Chi co chu nha moi co quyen thuc hien thao tac nay");
            return false;
        }

        return true;
    }
}