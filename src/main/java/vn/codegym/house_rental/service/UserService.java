package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.codegym.house_rental.dto.ChangePassword;
import vn.codegym.house_rental.dto.Register;
import vn.codegym.house_rental.dto.UserProfile;
import vn.codegym.house_rental.exception.UserRegistrationException;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.repository.BookingRepository;
import vn.codegym.house_rental.repository.UserRepository;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.repository.HouseRepository;
import java.util.Optional;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public long countHouse(User host){
        return houseRepository.countByHost(host);
    }

    @Autowired
    BookingRepository bookingRepository;


    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Page<User> getAllUsers(Pageable pageable){
        return userRepository.findAll(pageable);
    }

    // [SỬA LỖI]: Cập nhật getAllHosts sử dụng truy vấn ưu tiên các tài khoản đang chờ duyệt (PENDING) lên trang đầu tiên
    public Page<User> getAllHosts(Pageable pageable) {
        return userRepository.findByRoleOrHostStatusSorted(
                User.Role.ROLE_HOST,
                User.HostStatus.PENDING,
                pageable
        );
    }
    public void lockUser(Long id){

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);

        userRepository.save(user);
    }

    public void unlockUser(Long id){

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);

        userRepository.save(user);
    }
        //ktra email đã tồn tại
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
        // ktra email đã tồn tại ở tài khoản khác id
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return userRepository.existsByEmailAndIdNot(email, id);
    }

    public User registerUser(Register register) {
        try {
            // [BỔ SUNG THEO YÊU CẦU TASK 7]: Kiểm tra nếu là Đăng ký làm Chủ nhà thì gán role ROLE_HOST và trạng thái PENDING
            User.Role targetRole = register.isHost() ? User.Role.ROLE_HOST : User.Role.ROLE_USER;
            User.HostStatus targetHostStatus = register.isHost() ? User.HostStatus.PENDING : User.HostStatus.NONE;

            User user = User.builder()
                    .username(register.getUsername())
                    .password(passwordEncoder.encode(register.getPassword()))
                    .fullName(register.getFullName())
                    .email(register.getEmail())
                    .phone(register.getPhone())
                    .role(targetRole)
                    .hostStatus(targetHostStatus)
                    .active(true)
                    .avatarUrl("https://cdn-icons-png.flaticon.com/512/149/149071.png")
                    .build();
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserRegistrationException("Dữ liệu đăng ký không hợp lệ hoặc đã tồn tại trong hệ thống.", e);
        } catch (Exception e) {
            throw new UserRegistrationException("Đã xảy ra lỗi hệ thống khi đăng ký tài khoản.", e);
        }
    }

    public User login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tên đăng nhập hoặc mật khẩu không chính xác"
                        )
                );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập hoặc mật khẩu không chính xác"
            );
        }

        if (!user.getActive()) {
            throw new IllegalStateException(
                    "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."
            );
        }

        return user;
    }

    public User updateProfile(String username, UserProfile userProfile) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (userRepository.existsByEmailAndIdNot(userProfile.getEmail(), user.getId())) {
            throw new IllegalArgumentException("Email này đã được sử dụng bởi tài khoản khác");
        }

        user.setFullName(userProfile.getFullName());
        user.setEmail(userProfile.getEmail());
        user.setPhone(userProfile.getPhone());
        
        // [BỔ SUNG THEO YÊU CẦU TASK 3]: Cập nhật địa chỉ và avatar của người dùng
        user.setAddress(userProfile.getAddress());
        if (userProfile.getAvatarUrl() != null && !userProfile.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(userProfile.getAvatarUrl());
        }

        return userRepository.save(user);
    }

    public User changePassword(String username, ChangePassword changePasswordDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(
                changePasswordDto.getCurrentPassword(),
                user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }

        if (changePasswordDto.getNewPassword() == null ||
                !changePasswordDto.getNewPassword()
                        .equals(changePasswordDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp mật khẩu mới");
        }

        if (passwordEncoder.matches(
                changePasswordDto.getNewPassword(),
                user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        return userRepository.save(user);
    }
    public User approveHost(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy người dùng"));

        if (user.getHostStatus() != User.HostStatus.PENDING) {
            throw new IllegalStateException(
                    "Tài khoản không ở trạng thái chờ duyệt"
            );
        }

        user.setRole(User.Role.ROLE_HOST);

        user.setHostStatus(User.HostStatus.APPROVED);

        return userRepository.save(user);
    }
    public User rejectHost(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy người dùng"));

        if (user.getHostStatus() != User.HostStatus.PENDING) {
            throw new IllegalStateException(
                    "Tài khoản không ở trạng thái chờ duyệt"
            );
        }

        user.setRole(User.Role.ROLE_USER);

        user.setHostStatus(User.HostStatus.REJECTED);

        return userRepository.save(user);
    }
    public void requestBecomeHost(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Chỉ gửi yêu cầu nếu chưa từng đăng ký
        if (user.getHostStatus() == User.HostStatus.NONE ||
                user.getHostStatus() == User.HostStatus.REJECTED) {

            user.setHostStatus(User.HostStatus.PENDING);
            userRepository.save(user);
        }
    }

    public Double getTotalSpent(User user){

        return bookingRepository.getTotalSpent(user);

    }

    public List<Booking> getBookingHistory(User user){

        return bookingRepository.findByRenter(user);

    }
    public Double getRevenue(User user){

        Double revenue = bookingRepository.getRevenue(user);

        if(revenue==null){

            return 0.0;

        }

        return revenue;

    }
    public List<House> getHostHouses(User host){

        return houseRepository.findByHost(host);

    }

    public long countHostBookings(User host) {
        return bookingRepository.countByHouse_Host(host);
    }

    public long countRenterBookings(User renter) {
        return bookingRepository.countByRenter(renter);
    }
}