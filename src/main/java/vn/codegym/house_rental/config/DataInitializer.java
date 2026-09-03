package vn.codegym.house_rental.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.Category;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.Review;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.BookingRepository;
import vn.codegym.house_rental.repository.CategoryRepository;
import vn.codegym.house_rental.repository.HouseRepository;
import vn.codegym.house_rental.repository.ReviewRepository;
import vn.codegym.house_rental.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456"))
                    .fullName("Administrator")
                    .email("admin@house-rental.local")
                    .role(User.Role.ROLE_ADMIN)
                    .hostStatus(User.HostStatus.NONE)
                    .active(true)
                    .build());
        }

        // 2. Khởi tạo Users nếu chưa có
        User host1 = userRepository.findByUsername("host1").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("host1")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Nguyễn Văn Nam")
                        .email("nam@gmail.com")
                        .phone("0905123456")
                        .role(User.Role.ROLE_HOST)
                        .hostStatus(User.HostStatus.APPROVED)
                        .active(true)
                        .build())
        );

        User host2 = userRepository.findByUsername("host2").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("host2")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Trần Thị Hương")
                        .email("huong@gmail.com")
                        .phone("0914987654")
                        .role(User.Role.ROLE_HOST)
                        .hostStatus(User.HostStatus.APPROVED)
                        .active(true)
                        .build())
        );

        User user1 = userRepository.findByUsername("user1").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("user1")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Lê Văn Tuấn")
                        .email("tuan@gmail.com")
                        .phone("0935111222")
                        .role(User.Role.ROLE_USER)
                        .hostStatus(User.HostStatus.NONE)
                        .active(true)
                        .build())
        );

        // 3. Khởi tạo Categories & Houses nếu chưa có
        if (categoryRepository.count() == 0) {
            Category catApartment = categoryRepository.save(Category.builder().name("Căn hộ cao cấp").description("Căn hộ chung cư đầy đủ nội thất hiện đại").build());
            Category catTownhouse = categoryRepository.save(Category.builder().name("Nhà nguyên căn").description("Nhà phố không gian riêng biệt cho gia đình").build());
            Category catVilla = categoryRepository.save(Category.builder().name("Biệt thự / Villa").description("Biệt thự nghỉ dưỡng sang trọng có hồ bơi").build());
            Category catRoom = categoryRepository.save(Category.builder().name("Phòng trọ sinh viên").description("Phòng trọ tiện nghi, giá rẻ cạnh trường đại học").build());

            List<House> houses = Arrays.asList(
                House.builder()
                    .name("Căn Hộ Landmark 81 Vinhomes Central Park View Sông Sài Gòn (Tầng Cao)")
                    .address("720A Điện Biên Phủ, Quận Bình Thạnh, TP.HCM")
                    .pricePerDay(850000.0)
                    .pricePerMonth(25500000.0)
                    .numberOfBedrooms(2)
                    .numberOfBathrooms(2)
                    .description("<p><strong>Căn hộ tầng cao view cực đẹp</strong> trực diện sông Sài Gòn. Đầy đủ nội thất nhập khẩu từ Châu Âu, tiện ích hồ bơi vô cực và phòng gym chuẩn quốc tế hoàn toàn miễn phí.</p><ul><li>Diện tích: 85m²</li><li>Wifi tốc độ cao 500Mbps</li><li>Bảo vệ an ninh 24/7</li></ul>")
                    .thumbnailUrl("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600")
                    .status(House.HouseStatus.AVAILABLE)
                    .category(catApartment)
                    .host(host1)
                    .build(),

                House.builder()
                    .name("Nhà Nguyên Căn 3 Tầng Mặt Tiền Phố Cầu Giấy")
                    .address("150 Cầu Giấy, Phường Dịch Vọng, Hà Nội")
                    .pricePerDay(1200000.0)
                    .pricePerMonth(36000000.0)
                    .numberOfBedrooms(4)
                    .numberOfBathrooms(3)
                    .description("<p><strong>Nhà mặt tiền vị trí đắc địa</strong>, vừa kết hợp ở vừa làm văn phòng công ty. Không gian rộng rãi, thoáng mát, khu vực sầm uất, kinh doanh và ở thuận tiện, giao thông kết nối hoàn hảo.</p><p>Trang bị đầy đủ hệ thống điều hòa các phòng, sân thượng phơi đồ rộng thoáng.</p>")
                    .thumbnailUrl("https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=600")
                    .status(House.HouseStatus.AVAILABLE)
                    .category(catTownhouse)
                    .host(host1)
                    .build(),

                House.builder()
                    .name("Villa Biệt Thự Thảo Điền Đẳng Cấp Có Hồ Bơi Riêng")
                    .address("21 Nguyễn Văn Hưởng, Thảo Điền, TP. Thủ Đức")
                    .pricePerDay(2500000.0)
                    .pricePerMonth(75000000.0)
                    .numberOfBedrooms(5)
                    .numberOfBathrooms(5)
                    .description("<p><strong>Villa nghỉ dưỡng tiêu chuẩn 5 sao</strong> tại khu biệt thự Thảo Điền. Sân vườn xanh mát, hồ bơi riêng sang trọng, gara để được 2 ô tô lớn.</p><p>Đầy đủ dịch vụ dọn dẹp và bảo vệ 24/7.</p>")
                    .thumbnailUrl("https://images.unsplash.com/photo-1613977257363-707ba9348227?w=600")
                    .status(House.HouseStatus.AVAILABLE)
                    .category(catVilla)
                    .host(host2)
                    .build(),

                House.builder()
                    .name("Phòng Trọ Studio Khép Kín Tiện Nghi Gần ĐH Bách Khoa")
                    .address("48 Tạ Quang Bửu, Hai Bà Trưng, Hà Nội")
                    .pricePerDay(350000.0)
                    .pricePerMonth(10500000.0)
                    .numberOfBedrooms(1)
                    .numberOfBathrooms(1)
                    .description("<p><strong>Phòng Studio hiện đại đầy đủ đồ</strong>: điều hòa Inverter, bình nóng lạnh, tủ lạnh riêng, ban công thoáng mát, giờ giấc tự do 100%, không chung chủ.</p>")
                    .thumbnailUrl("https://images.unsplash.com/photo-1598928506311-c55ded91a20c?w=600")
                    .status(House.HouseStatus.AVAILABLE)
                    .category(catRoom)
                    .host(host2)
                    .build(),

                House.builder()
                    .name("Căn Hộ Vinhomes Central Park 3 Phòng Ngủ View Công Viên")
                    .address("208 Nguyễn Hữu Cảnh, Quận Bình Thạnh, TP.HCM")
                    .pricePerDay(1400000.0)
                    .pricePerMonth(42000000.0)
                    .numberOfBedrooms(3)
                    .numberOfBathrooms(2)
                    .description("<p>Căn hộ góc lầu cao view trọn vẹn công viên ven sông 14ha. Thiết kế nội thất phong cách Bắc Âu ấm cúng, tinh tế.</p>")
                    .thumbnailUrl("https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=600")
                    .status(House.HouseStatus.AVAILABLE)
                    .category(catApartment)
                    .host(host1)
                    .build(),

                House.builder()
                    .name("Nhà Phố Cổ Hà Nội Phong Cách Indochine Hoàn Kiếm")
                    .address("32 Hàng Bạc, Hoàn Kiếm, Hà Nội")
                    .pricePerDay(950000.0)
                    .pricePerMonth(28500000.0)
                    .numberOfBedrooms(2)
                    .numberOfBathrooms(2)
                    .description("<p>Không gian lưu trú đậm chất văn hóa Hà Nội xưa kết hợp tiện nghi hiện đại. Nằm ngay phố trung tâm du lịch Hoàn Kiếm.</p>")
                    .thumbnailUrl("https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=600")
                    .status(House.HouseStatus.AVAILABLE)
                    .category(catTownhouse)
                    .host(host2)
                    .build(),

                House.builder()
                    .name("Villa Nghỉ Dưỡng Sát Biển Bãi Trường Phú Quốc")
                    .address("Khu Bãi Trường, Dương Tơ, TP. Phú Quốc")
                    .pricePerDay(3200000.0)
                    .pricePerMonth(96000000.0)
                    .numberOfBedrooms(4)
                    .numberOfBathrooms(4)
                    .description("<p>Villa sát bờ biển ngắm trọn vẹn hoàng hôn Phú Quốc cực phẩm. Có bếp nướng BBQ ngoài trời và dịch vụ dọn dẹp phòng hàng ngày.</p>")
                    .thumbnailUrl("https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=600")
                    .status(House.HouseStatus.AVAILABLE)
                    .category(catVilla)
                    .host(host1)
                    .build(),

                House.builder()
                    .name("Phòng Mini House Full Nội Thất Gần ĐH FPT Hòa Lạc")
                    .address("Khu Công Nghệ Cao Hòa Lạc, Thạch Thất, Hà Nội")
                    .pricePerDay(300000.0)
                    .pricePerMonth(9000000.0)
                    .numberOfBedrooms(1)
                    .numberOfBathrooms(1)
                    .description("<p>Phòng mini xinh xắn trang bị tủ lạnh, máy giặt riêng, bếp điện từ sẵn sàng ở ngay. Phù hợp cho sinh viên và chuyên gia làm việc tại Hòa Lạc.</p>")
                    .thumbnailUrl("https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600")
                    .status(House.HouseStatus.AVAILABLE)
                    .category(catRoom)
                    .host(host2)
                    .build()
            );

            houseRepository.saveAll(houses);
        } else {
            // Tự động sửa các tiêu đề và mô tả cũ nếu có sai sót trong database hiện tại
            List<House> existingHouses = houseRepository.findAll();
            for (House h : existingHouses) {
                boolean changed = false;
                if (h.getName() != null && h.getName().contains("Bãi Dài")) {
                    h.setName("Căn Hộ Landmark 81 Vinhomes Central Park View Sông Sài Gòn (Tầng Cao)");
                    changed = true;
                }
                if (h.getDescription() != null && h.getDescription().contains("sầm sầm sụa")) {
                    h.setDescription(h.getDescription().replace("sầm sầm sụa thương mại", "sầm uất, kinh doanh và ở thuận tiện"));
                    changed = true;
                }
                if (changed) {
                    houseRepository.save(h);
                }
            }
        }

        // 4. Seed Thêm Danh Sách Booking Đa Dạng (Đơn đã hoàn thành, đang ở, đã duyệt, chờ duyệt, đã hủy)
        List<House> allHouses = houseRepository.findAll();
        if (!allHouses.isEmpty() && bookingRepository.count() <= 1) {
            House h1 = allHouses.get(0);
            House h2 = allHouses.size() > 1 ? allHouses.get(1) : h1;
            House h3 = allHouses.size() > 2 ? allHouses.get(2) : h1;
            House h4 = allHouses.size() > 3 ? allHouses.get(3) : h1;

            int currentYear = LocalDate.now().getYear();

            // Các đơn đã hoàn thành (CHECKED_OUT) tạo doanh thu các tháng
            Booking b1 = Booking.builder()
                    .startDate(LocalDate.of(currentYear, 1, 10))
                    .endDate(LocalDate.of(currentYear, 1, 14))
                    .totalPrice(h1.getDisplayPricePerDay() * 4)
                    .status(Booking.BookingStatus.CHECKED_OUT)
                    .renter(user1)
                    .house(h1)
                    .build();

            Booking b2 = Booking.builder()
                    .startDate(LocalDate.of(currentYear, 2, 5))
                    .endDate(LocalDate.of(currentYear, 2, 9))
                    .totalPrice(h2.getDisplayPricePerDay() * 4)
                    .status(Booking.BookingStatus.CHECKED_OUT)
                    .renter(user1)
                    .house(h2)
                    .build();

            Booking b3 = Booking.builder()
                    .startDate(LocalDate.of(currentYear, 2, 15))
                    .endDate(LocalDate.of(currentYear, 2, 18))
                    .totalPrice(h3.getDisplayPricePerDay() * 3)
                    .status(Booking.BookingStatus.CHECKED_OUT)
                    .renter(user1)
                    .house(h3)
                    .build();

            Booking b4 = Booking.builder()
                    .startDate(LocalDate.of(currentYear, 3, 2))
                    .endDate(LocalDate.of(currentYear, 3, 6))
                    .totalPrice(h1.getDisplayPricePerDay() * 4)
                    .status(Booking.BookingStatus.CHECKED_OUT)
                    .renter(user1)
                    .house(h1)
                    .build();

            // Đơn đang ở (CHECKED_IN)
            Booking bCheckin = Booking.builder()
                    .startDate(LocalDate.now().minusDays(1))
                    .endDate(LocalDate.now().plusDays(2))
                    .totalPrice(h4.getDisplayPricePerDay() * 3)
                    .status(Booking.BookingStatus.CHECKED_IN)
                    .renter(user1)
                    .house(h4)
                    .build();

            // Đơn đã duyệt (APPROVED) - sắp tới, dùng để test hủy hoặc check-in
            Booking bApproved = Booking.builder()
                    .startDate(LocalDate.now().plusDays(4))
                    .endDate(LocalDate.now().plusDays(7))
                    .totalPrice(h1.getDisplayPricePerDay() * 3)
                    .status(Booking.BookingStatus.APPROVED)
                    .renter(user1)
                    .house(h1)
                    .build();

            // Đơn chờ duyệt (PENDING) - dùng để host test phê duyệt / từ chối
            Booking bPending = Booking.builder()
                    .startDate(LocalDate.now().plusDays(8))
                    .endDate(LocalDate.now().plusDays(11))
                    .totalPrice(h2.getDisplayPricePerDay() * 3)
                    .status(Booking.BookingStatus.PENDING)
                    .renter(user1)
                    .house(h2)
                    .build();

            // Đơn đã hủy (CANCELLED)
            Booking bCancelled = Booking.builder()
                    .startDate(LocalDate.now().plusDays(12))
                    .endDate(LocalDate.now().plusDays(15))
                    .totalPrice(h3.getDisplayPricePerDay() * 3)
                    .status(Booking.BookingStatus.CANCELLED)
                    .renter(user1)
                    .house(h3)
                    .build();

            bookingRepository.saveAll(Arrays.asList(b1, b2, b3, b4, bCheckin, bApproved, bPending, bCancelled));
        }

        // 5. Seed Đánh Giá (Reviews) Mẫu
        if (reviewRepository.count() == 0 && !allHouses.isEmpty()) {
            House h1 = allHouses.get(0);
            House h2 = allHouses.size() > 1 ? allHouses.get(1) : h1;
            House h3 = allHouses.size() > 2 ? allHouses.get(2) : h1;

            Review r1 = Review.builder()
                    .house(h1)
                    .renter(user1)
                    .rating(5)
                    .comment("Căn hộ Landmark view sông Sài Gòn rất đẹp, nội thất sang trọng chuẩn 5 sao, chủ nhà hỗ trợ cực kỳ nhiệt tình!")
                    .createdAt(LocalDateTime.now().minusDays(15))
                    .build();

            Review r2 = Review.builder()
                    .house(h2)
                    .renter(user1)
                    .rating(5)
                    .comment("Nhà mặt tiền Cầu Giấy sạch sẽ, rộng rãi, khu vực xung quanh có nhiều hàng quán tiện ích. Rất hài lòng!")
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build();

            Review r3 = Review.builder()
                    .house(h3)
                    .renter(user1)
                    .rating(4)
                    .comment("Villa nghỉ dưỡng không gian thoáng đãng, hồ bơi sạch sẽ cho gia đình. Trải nghiệm rất đáng tiền.")
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .build();

            reviewRepository.saveAll(Arrays.asList(r1, r2, r3));
        }
    }
}
