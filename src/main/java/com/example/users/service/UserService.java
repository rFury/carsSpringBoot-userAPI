package com.example.users.service;

import java.util.List;
import java.util.Optional;

import com.example.users.entities.Role;
import com.example.users.entities.User;
import com.example.users.register.RegistationRequest;

public interface UserService {
	User saveUser(User user);
	User findUserByUsername (String username);
	Role addRole(Role role);
	User addRoleToUser(String username, String rolename);
	List<User> findAllUsers();
	User registerUser(RegistationRequest request);
	void sendEmailUser(User u, String code);
	User validateToken(String code);




}
