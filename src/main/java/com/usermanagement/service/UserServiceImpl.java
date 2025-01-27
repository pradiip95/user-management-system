package com.usermanagement.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.usermanagement.model.UserDetails;
import com.usermanagement.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class UserServiceImpl implements UserService {

	private UserRepository userRepository;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private JavaMailSender javaMailSender;

	public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
			JavaMailSender javaMailSender) {
		this.userRepository = userRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.javaMailSender = javaMailSender;
	}

	@SuppressWarnings("deprecation")
	@Override
	public UserDetails createUser(UserDetails user, String url) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setRole("ROLE_USER");
		user.setEnabled(false);
		user.setVerificationCode(RandomStringUtils.randomAlphanumeric(64));
		UserDetails saveUser = userRepository.save(user);
		sendVerificationMail(user, url);
		return saveUser;
	}

	@Override
	public boolean checkEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public void sendVerificationMail(UserDetails user, String url) {
		String from = "pradipkumar.parida2000@gmail.com";
		String to = user.getEmail();
		String subject = "Account Verification";
		String content = "Dear [[name]],<br>" + "Please Click the link below to verify your registration:<br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" + "Thank you,<br>" + "Pradip";

		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
			helper.setFrom(from, "Pradip");
			helper.setTo(to);
			helper.setSubject(subject);
			content = content.replace("[[name]]", user.getFullname());
			String siteUrl = url + "/verify?code=" + user.getVerificationCode();
			content = content.replace("[[URL]]", siteUrl);
			helper.setText(content, true);
			javaMailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean verifyAccount(String verificationCode) {
		UserDetails user = userRepository.findByVerificationCode(verificationCode);
		if (user != null) {
			user.setEnabled(true);
			user.setVerificationCode(null);
			userRepository.save(user);
			return true;
		}
		return false;
	}
}
