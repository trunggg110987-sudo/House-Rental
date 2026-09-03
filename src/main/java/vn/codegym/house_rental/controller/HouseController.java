package vn.codegym.house_rental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.service.CategoryService;
import vn.codegym.house_rental.service.FileStorageService;
import vn.codegym.house_rental.service.HouseService;
import vn.codegym.house_rental.service.UserService;
import vn.codegym.house_rental.service.HouseStatusPeriodService;

import jakarta.validation.Valid;

import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/houses")
public class HouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HouseStatusPeriodService houseStatusPeriodService;

    // Xem chi tiết nhà cho thuê
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Optional<House> houseOptional = houseService.findById(id);
        if (houseOptional.isEmpty()) {
            return "redirect:/";
        }
        House house = houseOptional.get();
        model.addAttribute("house", house);
        model.addAttribute("booking", new Booking());
        model.addAttribute("statusPeriods", houseService.getStatusPeriods(house));
        return "house/detail";
    }

    // Form thêm nhà mới
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("house", new House());
        model.addAttribute("categories", categoryService.findAll());
        return "house/form";
    }

    // Lưu nhà mới kèm upload nhiều ảnh
    @PostMapping("/create")
    public String createHouse(@Valid @ModelAttribute("house") House house,
                             BindingResult bindingResult,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                             HttpSession session,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "house/form";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String uploadedUrl = fileStorageService.storeFile(imageFile);
                house.setThumbnailUrl(uploadedUrl);
            } else if (house.getThumbnailUrl() == null || house.getThumbnailUrl().trim().isEmpty()) {
                house.setThumbnailUrl("/images/house-placeholder.svg");
            }

            if (house.getPricePerDay() == null && house.getPricePerMonth() != null) {
                house.setPricePerDay(Math.round((house.getPricePerMonth() / 30.0) * 100.0) / 100.0);
            }

            house.setStatus(House.HouseStatus.AVAILABLE);
            house.setHost(currentUser);

            House savedHouse = houseService.save(house);

            // Lưu danh sách ảnh phụ với try-catch
            if (imageFiles != null && !imageFiles.isEmpty()) {
                houseService.saveHouseImages(savedHouse, imageFiles);
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "house/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Thêm nhà cho thuê mới thành công!");
        return "redirect:/houses/my-houses";
    }

    // Form Chỉnh sửa nhà cho thuê dành cho Chủ nhà
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id,
                               HttpSession session,
                               Model model) {

        User currentUser = (User) session.getAttribute("currentUser");

        // Chưa đăng nhập
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<House> houseOptional = houseService.findById(id);

        // Không tìm thấy nhà
        if (houseOptional.isEmpty()) {
            return "redirect:/houses/my-houses";
        }

        House house = houseOptional.get();

        // Nhà không có chủ hoặc không phải nhà của user hiện tại
        if (house.getHost() == null ||
                !house.getHost().getId().equals(currentUser.getId())) {

            return "redirect:/houses/my-houses";
        }

        model.addAttribute("house", house);
        model.addAttribute("categories", categoryService.findAll());

        return "house/form";
    }

    // Cập nhật thông tin nhà cho thuê kèm upload nhiều ảnh
    @PostMapping("/{id}/edit")
    public String updateHouse(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("house") House house,
                              BindingResult bindingResult,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              Model model) {


        // 1. KIỂM TRA ĐĂNG NHẬP

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }



        // 2. TÌM NHÀ CẦN CẬP NHẬT

        Optional<House> existingHouseOpt = houseService.findById(id);

        if (existingHouseOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không tìm thấy bài đăng."
            );

            return "redirect:/houses/my-houses";
        }


        House existingHouse = existingHouseOpt.get();



        // 3. KIỂM TRA QUYỀN SỞ HỮU

        if (existingHouse.getHost() == null ||
                !existingHouse.getHost().getId().equals(currentUser.getId())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Bạn không có quyền chỉnh sửa bài đăng này."
            );

            return "redirect:/houses/my-houses";
        }



        // 4. KIỂM TRA VALIDATION

        if (bindingResult.hasErrors()) {

            model.addAttribute("categories", categoryService.findAll());

            // Quan trọng:
            // Khi validation lỗi, phải lấy lại ảnh hiện tại
            // từ existingHouse thay vì dùng house từ form.

            model.addAttribute("house", existingHouse);

            return "house/form";
        }



        // 5. CẬP NHẬT THÔNG TIN CƠ BẢN

        existingHouse.setName(house.getName());
        existingHouse.setAddress(house.getAddress());
        existingHouse.setPricePerDay(house.getPricePerDay());
        existingHouse.setNumberOfBedrooms(house.getNumberOfBedrooms());
        existingHouse.setNumberOfBathrooms(house.getNumberOfBathrooms());
        existingHouse.setDescription(house.getDescription());
        existingHouse.setCategory(categoryService.findById(house.getCategory().getId()).orElse(null));



        // 6. THUMBNAIL

        if (imageFile != null && !imageFile.isEmpty()) {

            String uploadedUrl =
                    fileStorageService.storeFile(imageFile);

            if (uploadedUrl != null && !uploadedUrl.isBlank()) {
                existingHouse.setThumbnailUrl(uploadedUrl);
            }
        }



        // 7. LƯU THÔNG TIN HOUSE

        try {

            House savedHouse = houseService.save(existingHouse);



            // 8. THÊM ẢNH CHI TIẾT

            if (imageFiles != null && !imageFiles.isEmpty()) {

                houseService.saveHouseImages(
                        savedHouse,
                        imageFiles
                );
            }


        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            model.addAttribute(
                    "categories",
                    categoryService.findAll()
            );

            model.addAttribute(
                    "house",
                    existingHouse
            );

            return "house/form";
        }



        // 9. THÔNG BÁO THÀNH CÔNG

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Cập nhật thông tin nhà thành công!"
        );

        return "redirect:/houses/my-houses";
    }
    // Xóa một ảnh chi tiết của nhà
    @PostMapping("/{houseId}/images/{imageId}/delete")
    public String deleteHouseImage(@PathVariable("houseId") Long houseId,
                                   @PathVariable("imageId") Long imageId,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<House> houseOptional = houseService.findById(houseId);

        if (houseOptional.isEmpty()) {
            return "redirect:/houses/my-houses";
        }

        House house = houseOptional.get();

        // Kiểm tra nhà có thuộc chủ nhà đang đăng nhập không
        if (house.getHost() == null ||
                !house.getHost().getId().equals(currentUser.getId())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Bạn không có quyền xóa ảnh của căn nhà này."
            );

            return "redirect:/houses/my-houses";
        }

        try {
            houseService.deleteHouseImage(imageId, house);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã xóa ảnh chi tiết thành công."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể xóa ảnh."
            );
        }

        return "redirect:/houses/" + houseId + "/edit";
    }

    // Endpoint cập nhật trạng thái nhà theo giai đoạn thời gian (Task 29)
    @PostMapping("/{id}/status-period")
    public String addStatusPeriod(
            @PathVariable Long id,
            @RequestParam("status") House.HouseStatus status,
            @RequestParam(value = "startDate", required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam(value = "endDate", required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            )
            LocalDate endDate,

            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {

            House house =
                    houseService.findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Không tìm thấy căn nhà."
                                    )
                            );

            // Kiểm tra đúng chủ nhà
            if (house.getHost() == null
                    || house.getHost().getId() == null
                    || !house.getHost().getId()
                    .equals(currentUser.getId())) {

                throw new IllegalStateException(
                        "Bạn không có quyền cập nhật trạng thái căn nhà này."
                );
            }

            houseService.addStatusPeriod(
                    house,
                    status,
                    startDate,
                    endDate
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã cập nhật trạng thái căn nhà thành công."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/houses/my-houses";
    }
    // Endpoint Xóa nhà cho thuê
    @PostMapping("/{id}/delete")
    public String deleteHouse(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        Optional<House> houseOptional = houseService.findById(id);

        if (houseOptional.isPresent() && houseOptional.get().getHost().getId().equals(currentUser.getId())) {
            houseService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa nhà cho thuê thành công.");
        }
        return "redirect:/houses/my-houses";
    }

    // Danh sách nhà của chủ nhà (My Houses) kèm bộ lọc Trạng thái (Task 30)
    @GetMapping("/my-houses")
    public String myHouses(@RequestParam(name = "status", required = false) House.HouseStatus status,
                           @RequestParam(name = "page", defaultValue = "0") int page,
                           @RequestParam(name = "size", defaultValue = "6") int size,
                           HttpSession session,
                           Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Page<House> housePage = houseService.findByHostAndStatus(currentUser, status, page, size);
        model.addAttribute("houses", housePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", housePage.getTotalPages());
        model.addAttribute("selectedStatus", status);
        return "house/my_houses";
    }
}
