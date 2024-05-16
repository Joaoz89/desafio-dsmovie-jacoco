package com.devsuperior.dsmovie.services;

import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;
	
	@Mock
	private UserRepository repository;
	
	@Mock
	private CustomUserUtil util;

	private UserEntity user;
	
	private List<UserDetailsProjection> userDetails;
	
	private String validUserName, invalidUserName;
	
	@BeforeEach
	void setUp() {
		
		validUserName = "Maria";
		invalidUserName = "Jubileuz";
		
		user = UserFactory.createUserEntity();
		userDetails = UserDetailsFactory.createCustomAdminUser(validUserName);
				
		Mockito.when(util.getLoggedUsername()).thenReturn(validUserName);
		
		Mockito.when(repository.searchUserAndRolesByUsername(validUserName)).thenReturn(userDetails);
		Mockito.when(repository.searchUserAndRolesByUsername(invalidUserName)).thenReturn(new ArrayList<>());
	}
	
	@Test
	public void authenticatedShouldReturnUserEntityWhenUserExists() {
		
		Mockito.when(repository.findByUsername(validUserName)).thenReturn(Optional.of(user));
		
		UserEntity result = service.authenticated();
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(user.getName(), result.getName());
		Assertions.assertEquals(user.getId(), result.getId());
		Assertions.assertEquals(user.getUsername(), result.getUsername());
		Assertions.assertEquals(user.getRoles(), result.getAuthorities());
		
		Mockito.verify(repository, times(1)).findByUsername(validUserName);
		Mockito.verify(util, times(1)).getLoggedUsername();
	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		
		Mockito.when(repository.findByUsername(validUserName)).thenReturn(Optional.empty());
		//Mockito.when(repository.findByUsername(invalidUserName)).thenThrow(UsernameNotFoundException.class);
		
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.authenticated();
		});
	}

	@Test
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
		
		UserDetails result = service.loadUserByUsername(validUserName);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(validUserName, result.getUsername());
	}

	@Test
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.loadUserByUsername(invalidUserName);
		});
	}
}
