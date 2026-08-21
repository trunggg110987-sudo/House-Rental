package vn.codegym.house_rental.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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
        User host1 = userRepository.save(User.builder().username("host1").password("123456").fullName("Nguyen Van Nam").email("nam@gmail.com").phone("0905123456").role(User.Role.ROLE_HOST).build());
        User host2 = userRepository.save(User.builder().username("host2").password("123456").fullName("Tran Thi Huong").email("huong@gmail.com").phone("0914987654").role(User.Role.ROLE_HOST).build());
        User user1 = userRepository.save(User.builder().username("user1").password("123456").fullName("Le Van Tuan").email("tuan@gmail.com").phone("0935111222").role(User.Role.ROLE_USER).build());

        // 3. Seed Sample Houses
        List<House> houses = Arrays.asList(
            House.builder()
                .name("Căn hộ Landmark 81 View Sông Bãi Dài High Floor")
                .address("720A Điện Biên Phủ, Quận Bình Thạnh, TP.HCM")
                .pricePerDay(850000.0)
                .pricePerMonth(22500000.0)
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
                .pricePerMonth(32000000.0)
                .numberOfBedrooms(4)
                .numberOfBathrooms(3)
                .description("<p><strong>Nhà mặt tiền vị trí đắc địa</strong>, vừa kết hợp ở vừa làm văn phòng công ty. Không gian rộng rãi, thoáng mát, khu vực sầm sầm sụa thương mại, giao thông thuận tiện.</p><p>Trang bị đầy đủ hệ thống điều hòa các phòng, sân thượng phơi đồ rộng thoáng.</p>")
                .thumbnailUrl("https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catTownhouse)
                .host(host1)
                .build(),

            House.builder()
                .name("Villa Biệt Thự Thảo Điền Đẳng Cấp Có Hồ Bơi Riêng")
                .address("21 Nguyễn Văn Hưởng, Thảo Điền, TP. Thủ Đức")
                .pricePerDay(2500000.0)
                .pricePerMonth(65000000.0)
                .numberOfBedrooms(5)
                .numberOfBathrooms(5)
                .description("<p><strong>Villa nghỉ dưỡng tiêu chuẩn 5 sao</strong> tại khu nhà giàu Thảo Điền. Sân vườn xanh mát, hồ bơi riêng sang trọng, gara để được 2 ô tô lớn.</p><p>Đầy đủ dịch vụ dọn dẹp và bảo vệ 24/7.</p>")
                .thumbnailUrl("https://images.unsplash.com/photo-1613977257363-707ba9348227?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catVilla)
                .host(host2)
                .build(),

            House.builder()
                .name("Phòng Trọ Studio Khép Kín Tiện Nghi Gần ĐH Bách Khoa")
                .address("48 Tạ Quang Bửu, Hai Bà Trưng, Hà Nội")
                .pricePerDay(350000.0)
                .pricePerMonth(6500000.0)
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
                .pricePerMonth(36000000.0)
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
                .pricePerMonth(25000000.0)
                .numberOfBedrooms(2)
                .numberOfBathrooms(2)
                .description("<p>Không gian lưu trú đậm chất văn hóa Hà Nội xưa kết hợp tiện nghi hiện đại. Nằm ngay phố trung tâm du lịch Hoàn Kiếm.</p>")
                .thumbnailUrl("https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catTownhouse)
                .host(host2)
                .build(),

            House.builder()
                .name("Villa Ocean Park Phú Quốc Mặt Biển Hoàng Hôn")
                .address("Khu Bãi Trường, Dương Tơ, TP. Phú Quốc")
                .pricePerDay(3200000.0)
                .pricePerMonth(80000000.0)
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
                .pricePerMonth(5000000.0)
                .numberOfBedrooms(1)
                .numberOfBathrooms(1)
                .description("<p>Phòng mini xinh xắn trang bị tủ lạnh, máy giặt riêng, bếp điện từ sẵn sàng ở ngay. Phù hợp cho sinh viên và chuyên gia làm việc tại Hòa Lạc.</p>")
                .thumbnailUrl("https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600")
                .status(House.HouseStatus.AVAILABLE)
                .category(catRoom)
                .host(host2)
                .build()
        );

        List<House> savedHouses = houseRepository.saveAll(houses);

        // 4. Seed Sample Booking (3 days = 850,000 * 3 = 2,550,000)
        bookingRepository.save(Booking.builder()
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .totalPrice(2550000.0)
                .status(Booking.BookingStatus.PENDING)
                .renter(user1)
                .house(savedHouses.get(0))
                .build());
    }
}
