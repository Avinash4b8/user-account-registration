package com.avinash.registration.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.avinash.registration.entity.User;

@Repository("userRepository")
public interface UserRepository extends CrudRepository<User, Long> {

	User findByEmail(String email);

	User findByConfirmationToken(String confirmationToken);

}
