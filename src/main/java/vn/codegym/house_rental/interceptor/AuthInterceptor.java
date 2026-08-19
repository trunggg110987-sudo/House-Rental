package vn.codegym.house_rental.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import vn.codegym.house_rental.controller.LoginController;
import vn.codegym.house_rental.model.User;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession(false);
        User user = (session != null)
                ? (User) session.getAttribute(LoginController.SESSION_USER_KEY)
                : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String uri = request.getRequestURI();
        if (uri.startsWith(request.getContextPath() + "/host/")
                && user.getRole() != User.Role.ROLE_HOST) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này");
            return false;
        }

        return true;
    }
}