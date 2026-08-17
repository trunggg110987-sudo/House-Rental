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
    public String showCreateForm(Model model) {
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
                house.setThumbnailUrl("https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=600");
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "house/form";
        }

        house.setStatus(House.HouseStatus.AVAILABLE);
        house.setHost(currentUser);

        House savedHouse = houseService.save(house);

        // Lưu danh sách ảnh phụ (Task 28)
        if (imageFiles != null && !imageFiles.isEmpty()) {
            houseService.saveHouseImages(savedHouse, imageFiles);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Thêm nhà cho thuê mới thành công!");
        return "redirect:/houses/my-houses";
    }

    // Form Chỉnh sửa nhà cho thuê dành cho Chủ nhà
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Optional<House> houseOptional = houseService.findById(id);

        if (houseOptional.isEmpty()) {
            return "redirect:/houses/my-houses";
        }

        House house = houseOptional.get();
        if (!house.getHost().getId().equals(currentUser.getId())) {
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

        User currentUser = (User) session.getAttribute("currentUser");
        Optional<House> existingHouseOpt = houseService.findById(id);

        if (existingHouseOpt.isEmpty() || !existingHouseOpt.get().getHost().getId().equals(currentUser.getId())) {
            return "redirect:/houses/my-houses";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "house/form";
        }

        House existingHouse = existingHouseOpt.get();
        existingHouse.setName(house.getName());
        existingHouse.setAddress(house.getAddress());
        existingHouse.setPricePerMonth(house.getPricePerMonth());
        existingHouse.setNumberOfBedrooms(house.getNumberOfBedrooms());
        existingHouse.setNumberOfBathrooms(house.getNumberOfBathrooms());
        existingHouse.setDescription(house.getDescription());
        existingHouse.setCategory(house.getCategory());
        existingHouse.setStatus(house.getStatus());

        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadedUrl = fileStorageService.storeFile(imageFile);
            existingHouse.setThumbnailUrl(uploadedUrl);
        }

        houseService.save(existingHouse);

        // Lưu danh sách ảnh phụ mới tải lên (Task 28)
        if (imageFiles != null && !imageFiles.isEmpty()) {
            houseService.saveHouseImages(existingHouse, imageFiles);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin nhà thành công!");
        return "redirect:/houses/my-houses";
    }

    // Endpoint cập nhật trạng thái nhà theo giai đoạn thời gian (Task 29)
    @PostMapping("/{id}/status-period")
    public String updateStatusPeriod(@PathVariable("id") Long id,
                                    @RequestParam("status") House.HouseStatus status,
                                    @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                    @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<House> houseOptional = houseService.findById(id);
        if (houseOptional.isEmpty() || !houseOptional.get().getHost().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy căn nhà hoặc bạn không có quyền.");
            return "redirect:/houses/my-houses";
        }

        try {
            houseService.addStatusPeriod(houseOptional.get(), status, startDate, endDate);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái nhà theo giai đoạn thời gian thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
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
