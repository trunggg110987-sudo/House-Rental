package vn.codegym.house_rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.codegym.house_rental.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // ktra trung email
    boolean existsByEmail(String email);

    // ktra email o tai khoan khac id
    boolean existsByEmailAndIdNot(String email, Long id);

    Page<User> findByRole(User.Role role, Pageable pageable);

    Page<User> findByRoleOrHostStatus(
            User.Role role,
            User.HostStatus hostStatus,
            Pageable pageable
    );

    // [SỬA LỖI]: Bổ sung truy vấn ưu tiên sắp xếp các tài khoản đang chờ duyệt (PENDING) lên đầu danh sách Admin
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.role = :role OR u.hostStatus = :hostStatus ORDER BY CASE WHEN u.hostStatus = vn.codegym.house_rental.model.User.HostStatus.PENDING THEN 0 ELSE 1 END, u.id DESC")
    Page<User> findByRoleOrHostStatusSorted(
            @org.springframework.data.repository.query.Param("role") User.Role role,
            @org.springframework.data.repository.query.Param("hostStatus") User.HostStatus hostStatus,
            Pageable pageable
    );
}