package com.usermanagement.service;

import com.usermanagement.model.UserDetails;

public interface UserService {
	
	public UserDetails createUser(UserDetails user, String url);

	public boolean checkEmail(String email);

	public boolean verifyAccount(String verificationCode);
}
