package vn.codegym.house_rental.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

import java.util.Optional;

@Controller
@RequestMapping("/houses")
public class HouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileStorageService fileStorageService;

    // Lấy user đang đăng nhập từ session
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute(LoginController.SESSION_USER_KEY);
    }

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
        return "house/detail";
    }

    // Form thêm nhà mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("house", new House());
        model.addAttribute("categories", categoryService.findAll());
        return "house/form";
    }

    // Lưu nhà mới kèm upload ảnh
    @PostMapping("/create")
    public String createHouse(
            @Valid @ModelAttribute("house") House house,
            BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để đăng tin cho thuê");
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
            } else {
                house.setThumbnailUrl("https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=600");
            }
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("thumbnailUrl", "error.thumbnailUrl", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("imageError", e.getMessage());
            return "house/form";
        }

        house.setStatus(House.HouseStatus.AVAILABLE);
        house.setHost(currentUser);

        houseService.save(house);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm nhà cho thuê mới thành công!");
        return "redirect:/";
    }

    // Form sửa nhà đã đăng
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để tiếp tục");
            return "redirect:/login";
        }

        Optional<House> houseOptional = houseService.findById(id);
        if (houseOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhà cho thuê này");
            return "redirect:/houses/my-houses";
        }

        House house = houseOptional.get();

        if (house.getHost() == null || !house.getHost().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền sửa nhà này");
            return "redirect:/houses/my-houses";
        }

        model.addAttribute("house", house);
        model.addAttribute("categories", categoryService.findAll());
        return "house/form";
    }

    // Lưu thông tin nhà đã sửa
    @PostMapping("/{id}/edit")
    public String updateHouse(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("house") House house,
            BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để tiếp tục");
            return "redirect:/login";
        }

        Optional<House> existingOptional = houseService.findById(id);
        if (existingOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhà cho thuê này");
            return "redirect:/houses/my-houses";
        }

        House existing = existingOptional.get();

        if (existing.getHost() == null || !existing.getHost().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền sửa nhà này");
            return "redirect:/houses/my-houses";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            house.setId(id);
            house.setThumbnailUrl(existing.getThumbnailUrl());
            return "house/form";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String uploadedUrl = fileStorageService.storeFile(imageFile);
                existing.setThumbnailUrl(uploadedUrl);
            }
            // Không chọn ảnh mới -> giữ nguyên existing.getThumbnailUrl()
        } catch (IllegalArgumentException e) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("imageError", e.getMessage());
            house.setId(id);
            house.setThumbnailUrl(existing.getThumbnailUrl());
            return "house/form";
        }

        existing.setName(house.getName());
        existing.setAddress(house.getAddress());
        existing.setNumberOfBedrooms(house.getNumberOfBedrooms());
        existing.setNumberOfBathrooms(house.getNumberOfBathrooms());
        existing.setDescription(house.getDescription());
        existing.setPricePerMonth(house.getPricePerMonth());
        existing.setCategory(house.getCategory());

        houseService.save(existing);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin nhà thành công!");
        return "redirect:/houses/my-houses";
    }

    // Danh sách nhà của chủ nhà (My Houses)
    @GetMapping("/my-houses")
    public String myHouses(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem danh sách nhà của bạn");
            return "redirect:/login";
        }

        Page<House> housePage = houseService.findByHost(currentUser, page, size);
        model.addAttribute("houses", housePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", housePage.getTotalPages());
        return "house/my_houses";
    }
}