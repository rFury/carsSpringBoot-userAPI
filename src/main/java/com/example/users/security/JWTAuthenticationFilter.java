package com.example.users.security;

import java.util.Map;  // For HashMap and Map
import jakarta.servlet.FilterChain;  // From Jakarta Servlet API
import jakarta.servlet.ServletException;  // From Jakarta Servlet API
import jakarta.servlet.http.HttpServletRequest;  // From Jakarta Servlet API
import jakarta.servlet.http.HttpServletResponse;  // From Jakarta Servlet API
import org.springframework.security.authentication.AuthenticationManager;  // From Spring Security
import org.springframework.security.authentication.DisabledException;  // For handling disabled users exception
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;  // For authenticating user
import org.springframework.security.core.Authentication;  // For handling Spring Security authentication
import org.springframework.security.core.AuthenticationException;  // For handling authentication exception
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;  // Extending this class for custom authentication
import com.auth0.jwt.JWT;  // For creating JWT tokens
import com.auth0.jwt.algorithms.Algorithm;  // For JWT algorithms
import com.example.users.entities.User;  // The User class from your project entity
import com.fasterxml.jackson.core.JsonParseException;  // For Jackson JSON parsing exceptions
import com.fasterxml.jackson.databind.JsonMappingException;  // For Jackson JSON mapping exceptions
import com.fasterxml.jackson.databind.ObjectMapper;  // For handling JSON to Object conversions
import java.io.IOException;  // For handling I/O exceptions
import java.io.PrintWriter;  // For writing responses
import java.util.HashMap;  // For using HashMap to store response data
import java.util.List;  // For handling the list of roles
import java.util.ArrayList;  // For initializing ArrayList
import java.util.Date;  // For handling token expiration


public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
		super();
		this.authenticationManager = authenticationManager;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		User user = null;
		try {
			user = new ObjectMapper().readValue(request.getInputStream(), User.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {

		org.springframework.security.core.userdetails.User springUser = (org.springframework.security.core.userdetails.User) authResult
				.getPrincipal();

		List<String> roles = new ArrayList<>();
		springUser.getAuthorities().forEach(au -> {
			roles.add(au.getAuthority());
		});

		String jwt = JWT.create().withSubject(springUser.getUsername())
				.withArrayClaim("roles", roles.toArray(new String[roles.size()]))
				.withExpiresAt(new Date(System.currentTimeMillis() + SecParams.EXP_TIME))
				.sign(Algorithm.HMAC256(SecParams.SECRET));

		response.addHeader("Authorization", jwt);

	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException failed)
			throws IOException, ServletException {
		if (failed instanceof DisabledException) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json");
			Map<String, Object> data = new HashMap<>();

			data.put("errorCause", "disabled");
			data.put("message", "L'utilisateur est désactivé !");
			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(data);
			PrintWriter writer = response.getWriter();
			writer.println(json);
			writer.flush();

		} else {
			super.unsuccessfulAuthentication(request, response, failed);
		}
	}

}
