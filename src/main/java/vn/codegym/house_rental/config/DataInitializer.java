package vn.codegym.house_rental.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.Category;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.BookingRepository;
import vn.codegym.house_rental.repository.CategoryRepository;
import vn.codegym.house_rental.repository.HouseRepository;
import vn.codegym.house_rental.repository.UserRepository;

import java.time.LocalDate;
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
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            return;
        }

        // 1. Seed Categories
        Category catApartment = categoryRepository.save(Category.builder().name("Căn hộ cao cấp").description("Căn hộ chung cư đầy đủ nội thất hiện đại").build());
        Category catTownhouse = categoryRepository.save(Category.builder().name("Nhà nguyên căn").description("Nhà phố không gian riêng biệt cho gia đình").build());
        Category catVilla = categoryRepository.save(Category.builder().name("Biệt thự / Villa").description("Biệt thự nghỉ dưỡng sang trọng có hồ bơi").build());
        Category catRoom = categoryRepository.save(Category.builder().name("Phòng trọ sinh viên").description("Phòng trọ tiện nghi, giá rẻ cạnh trường đại học").build());

        // 2. Seed Users
        User host1 = userRepository.save(User.builder().username("host1").password(passwordEncoder.encode("123456")).fullName("Nguyen Van Nam").email("nam@gmail.com").phone("0905123456").role(User.Role.ROLE_HOST).build());
        User host2 = userRepository.save(User.builder().username("host2").password(passwordEncoder.encode("123456")).fullName("Tran Thi Huong").email("huong@gmail.com").phone("0914987654").role(User.Role.ROLE_HOST).build());
        User user1 = userRepository.save(User.builder().username("user1").password(passwordEncoder.encode("123456")).fullName("Le Van Tuan").email("tuan@gmail.com").phone("0935111222").role(User.Role.ROLE_USER).build());

        // 3. Seed Sample Houses
        List<House> houses = Arrays.asList(
            House.builder()
                .name("Căn hộ Landmark 81 View Sông Bãi Dài")
                .address("720A Điện Biên Phủ, Quận Bình Thạnh, TP.HCM")
                .pricePerMonth(15000000.0)
                .numberOfBedrooms(2)
                .numberOfBathrooms(2)
                .description("Căn hộ tầng cao view cực đẹp, đầy đủ nội thất nhập khẩu, tiện ích hồ bơi và gym miễn phí.")
                .thumbnailUrl("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catApartment)
                .host(host1)
                .build(),

            House.builder()
                .name("Nhà Nguyên Căn 3 Tầng Mặt Tiền Cầu Giấy")
                .address("150 Cầu Giấy, Phường Dịch Vọng, Hà Nội")
                .pricePerMonth(22000000.0)
                .numberOfBedrooms(4)
                .numberOfBathrooms(3)
                .description("Nhà mặt tiền thích hợp vừa ở vừa làm văn phòng công ty. Khu vực sầm uất, an ninh 24/7.")
                .thumbnailUrl("https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catTownhouse)
                .host(host1)
                .build(),

            House.builder()
                .name("Villa Biệt Thự Thảo Điền Có Hồ Bơi")
                .address("21 Nguyễn Văn Hưởng, Thảo Điền, TP. Thủ Đức")
                .pricePerMonth(45000000.0)
                .numberOfBedrooms(5)
                .numberOfBathrooms(5)
                .description("Villa nghỉ dưỡng sang trọng có sân vườn rộng rãi, hồ bơi riêng, gara để được 2 ô tô.")
                .thumbnailUrl("https://images.unsplash.com/photo-1613977257363-707ba9348227?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catVilla)
                .host(host2)
                .build(),

            House.builder()
                .name("Phòng Trọ Studio Khép Kín Gần ĐH Bách Khoa")
                .address("48 Tạ Quang Bửu, Hai Bà Trưng, Hà Nội")
                .pricePerMonth(4500000.0)
                .numberOfBedrooms(1)
                .numberOfBathrooms(1)
                .description("Phòng trọ mới xây có điều hòa, bình nóng lạnh, ban công thoáng mát, giờ giấc tự do.")
                .thumbnailUrl("https://images.unsplash.com/photo-1598928506311-c55ded91a20c?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catRoom)
                .host(host2)
                .build(),

            House.builder()
                .name("Căn Hộ Vinhomes Central Park 3 Phòng Ngủ")
                .address("208 Nguyễn Hữu Cảnh, Quận Bình Thạnh, TP.HCM")
                .pricePerMonth(28000000.0)
                .numberOfBedrooms(3)
                .numberOfBathrooms(2)
                .description("Căn hộ góc lầu cao view công viên 14ha, thiết kế hiện đại phong cách Bắc Âu.")
                .thumbnailUrl("https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catApartment)
                .host(host1)
                .build(),

            House.builder()
                .name("Nhà Phố Cổ Hà Nội Phong Cách Indochine")
                .address("32 Hàng Bạc, Hoàn Kiếm, Hà Nội")
                .pricePerMonth(18000000.0)
                .numberOfBedrooms(2)
                .numberOfBathrooms(2)
                .description("Không gian mang đậm dấu ấn Hà Nội xưa kết hợp tiện nghi hiện đại, vị trí trung tâm du lịch.")
                .thumbnailUrl("https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=600")
                .status(House.HouseStatus.RENTED)
                .category(catTownhouse)
                .host(host2)
                .build(),

            House.builder()
                .name("Villa Ocean Park Phú Quốc Mặt Biển")
                .address("Khu Bãi Trường, Dương Tơ, TP. Phú Quốc")
                .pricePerMonth(60000000.0)
                .numberOfBedrooms(4)
                .numberOfBathrooms(4)
                .description("Villa bờ biển ngắm hoàng hôn cực phẩm, có dịch vụ dọn dẹp hàng ngày.")
                .thumbnailUrl("https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catVilla)
                .host(host1)
                .build(),

            House.builder()
                .name("Phòng Mini House Full Nội Thất Gần ĐH FPT")
                .address("Khu Công Nghệ Cao Hòa Lạc, Thạch Thất, Hà Nội")
                .pricePerMonth(3800000.0)
                .numberOfBedrooms(1)
                .numberOfBathrooms(1)
                .description("Phòng mini xinh xắn trang bị tủ lạnh, máy giặt riêng, bếp điện từ sẵn sàng ở ngay.")
                .thumbnailUrl("https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catRoom)
                .host(host2)
                .build()
        );

        List<House> savedHouses = houseRepository.saveAll(houses);

        // 4. Seed Sample Booking
        bookingRepository.save(Booking.builder()
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(32))
                .totalPrice(15000000.0)
                .status(Booking.BookingStatus.PENDING)
                .renter(user1)
                .house(savedHouses.get(0))
                .build());
    }
}
