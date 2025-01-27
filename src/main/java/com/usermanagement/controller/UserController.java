package com.usermanagement.controller;

import java.security.Principal;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.usermanagement.model.UserDetails;
import com.usermanagement.repository.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {

	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;

	public UserController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@ModelAttribute
	private void userDetails(Model model, Principal principal) {
		String email = principal.getName();
		UserDetails details = userRepository.findByEmail(email);
		model.addAttribute("user", details);
	}

	@GetMapping("/")
	public String home() {
		return "user/home";
	}

	@GetMapping("/change-password")
	public String loadChangePassword() {
		return "user/changePassword";
	}

	@PostMapping("/update-password")
	public String changePassword(Principal principal, @RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, RedirectAttributes redirectAttributes) {

		String email = principal.getName();
		UserDetails loginUser = userRepository.findByEmail(email);

		boolean matches = passwordEncoder.matches(oldPassword, loginUser.getPassword());
		String message = null;
		if (matches) {
			loginUser.setPassword(passwordEncoder.encode(newPassword));
			UserDetails updatePasswordUser = userRepository.save(loginUser);
			if (updatePasswordUser != null) {
				message = "Password Changed Successfully";
			} else {
				message = "Something went wrong on the server";
			}
		} else {
			message = "Old Password is incorrect";
		}
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/user/change-password";
	}

	@GetMapping("/delete-account")
	public String showDeleteAccountPage() {
		return "user/deleteAccount";
	}

	@PostMapping("/confirm-delete")
	public String confirmDeleteAccount(@RequestParam("password") String password, Principal principal,
			RedirectAttributes redirectAttributes) {
		String email = principal.getName();
		UserDetails user = userRepository.findByEmail(email);
		if (user != null) {
			boolean matches = passwordEncoder.matches(password, user.getPassword());
			if (matches) {
				userRepository.delete(user);
				redirectAttributes.addFlashAttribute("message", "Account deleted successfully");
				return "redirect:/register";
			} else {
				redirectAttributes.addFlashAttribute("error", "Incorrect password. Please try again.");
				return "redirect:/user/delete-account";
			}
		} else {
			redirectAttributes.addFlashAttribute("error", "User not found");
			return "redirect:/user";
		}
	}
}
