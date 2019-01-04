package com.avinash.useraccountregistration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.avinash.registration.UserAccountRegistrationApplication;
import com.avinash.registration.entity.User;
import com.avinash.registration.repository.UserRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
@SpringBootTest(classes = UserAccountRegistrationApplication.class)
public class UserRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	private User user = new User();

	@Test
	public void whenFindByEmail_thenReturnUser() {
		// Given
		user.setEmail("Avinash@gmail.com");
		user.setFirstName("Avinash Babu");
		user.setLastName("Yedlapalli");
		entityManager.persist(user);
		entityManager.flush();
		// When
		User found = userRepository.findByEmail("Avinash@gmail.com");
		// Then
		assertThat(found.getEmail()).isEqualTo(user.getEmail());

	}

	@Test
	public void whenFindByConfirmationToken_thenReturnUser() {

		user.setEmail("avinash@gmail.com");
		user.setFirstName("Avinash Babu");
		user.setLastName("yedlapalli");
		user.setConfirmationToken("8e6c5667-5bbb-45b4-ad1f-dbb89e71824b");
		entityManager.persist(user);
		entityManager.flush();
		User findByConfirmationToken = userRepository
				.findByConfirmationToken("8e6c5667-5bbb-45b4-ad1f-dbb89e71824b");
		System.out.println(findByConfirmationToken);
		assertThat(findByConfirmationToken.getConfirmationToken())
		.isEqualTo(user.getConfirmationToken());
	}
}
