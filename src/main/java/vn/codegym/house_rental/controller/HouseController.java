package vn.codegym.house_rental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.security.CustomUserDetails;
import vn.codegym.house_rental.security.oauth2.CustomOAuth2User;
import vn.codegym.house_rental.service.CategoryService;
import vn.codegym.house_rental.service.FileStorageService;
import vn.codegym.house_rental.service.HouseService;
import vn.codegym.house_rental.service.UserService;

import jakarta.validation.Valid;
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

    @Autowired
    private UserService userService;

    private Optional<User> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails.getUser());
        } else if (principal instanceof CustomOAuth2User oauth2User) {
            return Optional.of(oauth2User.getUser());
        }
        return Optional.empty();
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
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "house/form";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadedUrl = fileStorageService.storeFile(imageFile);
            house.setThumbnailUrl(uploadedUrl);
        } else if (house.getThumbnailUrl() == null || house.getThumbnailUrl().trim().isEmpty()) {
            house.setThumbnailUrl("https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=600");
        }

        house.setStatus(House.HouseStatus.AVAILABLE);

        // Gán Host đang đăng nhập
        Optional<User> currentUser = getCurrentUser(authentication);
        if (currentUser.isPresent()) {
            house.setHost(currentUser.get());
        } else {
            Optional<User> defaultHost = userService.findByUsername("host1");
            defaultHost.ifPresent(house::setHost);
        }

        houseService.save(house);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm nhà cho thuê mới thành công!");
        return "redirect:/";
    }

    // Danh sách nhà của chủ nhà (My Houses)
    @GetMapping("/my-houses")
    public String myHouses(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            Authentication authentication,
            Model model) {

        Optional<User> currentUser = getCurrentUser(authentication);
        if (currentUser.isEmpty()) {
            currentUser = userService.findByUsername("host1");
        }

        if (currentUser.isPresent()) {
            Page<House> housePage = houseService.findByHost(currentUser.get(), page, size);
            model.addAttribute("houses", housePage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", housePage.getTotalPages());
        }
        return "house/my_houses";
    }
}
