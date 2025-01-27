package com.usermanagement.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.usermanagement.model.UserDetails;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

	private UserService userService;
	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;

	public HomeController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
			UserService userService) {
		this.userService = userService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@ModelAttribute
	private void userDetails(Model model, Principal principal) {
		if (principal != null) {
			String email = principal.getName();
			UserDetails details = userRepository.findByEmail(email);
			model.addAttribute("user", details);
		}
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@PostMapping("/createUser")
	public String createUser(@ModelAttribute UserDetails user, RedirectAttributes redirectAttributes,
			HttpServletRequest httpServletRequest) {

		String url = httpServletRequest.getRequestURL().toString();
		url = url.replace(httpServletRequest.getServletPath(), "");

		boolean checkEmail = userService.checkEmail(user.getEmail());
		String message = null;
		if (checkEmail) {
			message = "Email Already Exists";
		} else {
			UserDetails userDetails = userService.createUser(user, url);
			if (userDetails != null) {
				message = "Register Successfully";
			} else {
				message = "Something wrong on the server";
			}
		}
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/register";
	}

	@GetMapping("/verify")
	public String verifyAccount(@Param("code") String code) {
		boolean verifyAccount = userService.verifyAccount(code);
		if (verifyAccount) {
			return "verifySuccess";
		} else {
			return "failedVerify";
		}
	}

	@GetMapping("/forget-password")
	public String loadForgetPasswordPage() {
		return "forgetPassword";
	}

	@GetMapping("/reset-password/{id}")
	public String loadResetPasswordPage(@PathVariable int id, Model model) {
		model.addAttribute("id", id);
		return "resetPassword";
	}

	@PostMapping("/loadForgetPassword")
	public String forgetPassword(@RequestParam String email, @RequestParam String mobileNumber,
			RedirectAttributes redirectAttributes) {
		UserDetails user = userRepository.findByEmailAndMobileNumber(email, mobileNumber);
		if (user != null) {
			return "redirect:/reset-password/" + user.getId();
		} else {
			redirectAttributes.addFlashAttribute("message", "Invalid Credentials");
			return "redirect:/forget-password";
		}
	}

	@PostMapping("/loadResetPassword")
	public String resetPassword(@RequestParam String confirmPassword, @RequestParam Integer id,
			RedirectAttributes redirectAttributes) {
		UserDetails user = userRepository.findById(id).get();
		String newEncodedPassword = passwordEncoder.encode(confirmPassword);
		user.setPassword(newEncodedPassword);
		UserDetails UpdateUserDetails = userRepository.save(user);
		if (UpdateUserDetails != null) {
			redirectAttributes.addFlashAttribute("message", "Password Changed Successfully");
		}
		return "redirect:/forget-password";
	}
}
