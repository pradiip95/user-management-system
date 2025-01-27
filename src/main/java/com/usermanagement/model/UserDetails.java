package com.usermanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String fullname;
	private String email;
	private String mobileNumber;
	private String address;
	private String qualification;
	private String password;
	private String role;
	private boolean enabled;
	private String verificationCode;
}
