package vn.codegym.house_rental.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.HouseRepository;
import vn.codegym.house_rental.service.ChatService;

import java.util.List;

@Controller
@RequestMapping("/host/messages")
public class HostChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private HouseRepository houseRepository;

    @GetMapping
    public String viewMessages(
            @RequestParam(value = "houseId", required = false) Long houseId,
            @RequestParam(value = "guestId", required = false) Long guestId,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!currentUser.getRole().equals(User.Role.ROLE_HOST) && !currentUser.getRole().equals(User.Role.ROLE_ADMIN)) {
            return "redirect:/";
        }

        List<House> myHouses = houseRepository.findByHost(currentUser);
        model.addAttribute("myHouses", myHouses);

        Long currentHouseId = houseId;
        if (currentHouseId == null && !myHouses.isEmpty()) {
            currentHouseId = myHouses.get(0).getId();
        }
        model.addAttribute("selectedHouseId", currentHouseId);
        model.addAttribute("selectedGuestId", guestId);

        return "host/messages";
    }
}
