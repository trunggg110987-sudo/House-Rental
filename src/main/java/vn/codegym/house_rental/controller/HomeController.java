package vn.codegym.house_rental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.service.CategoryService;
import vn.codegym.house_rental.service.HouseService;

@Controller
public class HomeController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String index(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            Model model) {

        Page<House> housePage = houseService.searchHouses(keyword, categoryId, minPrice, maxPrice, page, size);

        model.addAttribute("houses", housePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", housePage.getTotalPages());
        model.addAttribute("totalItems", housePage.getTotalElements());

        // Dynamic filters
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "home";
    }
}
