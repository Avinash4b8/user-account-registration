package com.avinash.registration.repository;

import org.springframework.data.repository.CrudRepository;

import com.avinash.registration.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

	User findByEmail(String email);

	User findByConfirmationToken(String confirmationToken);

}
