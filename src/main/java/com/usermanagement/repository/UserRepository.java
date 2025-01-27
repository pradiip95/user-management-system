package com.usermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usermanagement.model.UserDetails;

public interface UserRepository extends JpaRepository<UserDetails, Integer> {

	public boolean existsByEmail(String email);

	public UserDetails findByEmail(String email);

	public UserDetails findByEmailAndMobileNumber(String email, String mobileNumber);

	public UserDetails findByVerificationCode(String verificationCode);
}
