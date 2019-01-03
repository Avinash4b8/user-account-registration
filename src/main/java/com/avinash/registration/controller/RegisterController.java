package com.avinash.registration.controller;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.avinash.registration.entity.User;
import com.avinash.registration.service.EmailService;
import com.avinash.registration.service.UserService;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

@Controller
public class RegisterController {

	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private UserService userService;
	private EmailService emailService;

	@Autowired
	public RegisterController(BCryptPasswordEncoder bCryptPasswordEncoder, UserService userService,
			EmailService emailService) {
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.userService = userService;
		this.emailService = emailService;
	}

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public ModelAndView showRegistrationPage(ModelAndView modelAndView, User user) {
		modelAndView.addObject("user", user);
		modelAndView.setViewName("register");
		return modelAndView;
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ModelAndView prcessRegistrationForm(ModelAndView modelAndView, @Valid User user, 
			BindingResult bindingResult, HttpServletRequest request) {
		User userExist = userService.findByEmail(user.getEmail());
		if (userExist != null) {
			modelAndView.addObject("alreadyRegisteredMessage",
					"Oops!  There is already a user registered with the email provided.");
			modelAndView.setViewName("register");
			bindingResult.reject("email");
		}
		if (bindingResult.hasErrors()) {
			modelAndView.setViewName("register");
		} else {
			user.setEnabled(false);
			user.setConfirmationToken(UUID.randomUUID().toString());
			userService.save(user);
			String appUrl = request.getScheme() + "://" + request.getServerName();
			SimpleMailMessage registrationMail = new SimpleMailMessage();
			registrationMail.setTo(user.getEmail());
			registrationMail.setSubject("Please confirm your registration to proceed further");
			registrationMail.setText("To confirm your email address, "
					+ "please click " + "on the link below:\n" + appUrl
					+ "/confirm?token=" + user.getConfirmationToken());
			registrationMail.setFrom("nonreply@domain.com");
			emailService.sendEmail(registrationMail);
			modelAndView.addObject("confirmationMessage",
					"A confirmation e-mail " + "has been sent to " + user.getEmail());
			modelAndView.setViewName("register");
		}
		return modelAndView;
	}

	// Process confirmation link
	@RequestMapping(value = "/confirm", method = RequestMethod.GET)
	public ModelAndView showConfirmationPage(ModelAndView modelAndView,
			@RequestParam("token") String token) {

		User user = userService.findByConfirmationToken(token);

		if (user == null) { // No token found in DB
			modelAndView.addObject("invalidToken", "Oops!  This is an invalid confirmation link.");
		} else { // Token found
			modelAndView.addObject("confirmationToken", user.getConfirmationToken());
		}

		modelAndView.setViewName("confirm");
		return modelAndView;
	}

	// Process confirmation link
	@RequestMapping(value = "/confirm", method = RequestMethod.POST)
	public ModelAndView processConfirmationForm(ModelAndView modelAndView,
			BindingResult bindingResult,
			@RequestParam Map requestParams, RedirectAttributes redir) {

		modelAndView.setViewName("confirm");

		Zxcvbn passwordCheck = new Zxcvbn();

		Strength strength = passwordCheck.measure((String) requestParams.get("password"));

		if (strength.getScore() < 3) {
			bindingResult.reject("password");

			redir.addFlashAttribute("errorMessage", "Your password is too weak. "
					+ " Choose a stronger one.");

			modelAndView.setViewName("redirect:confirm?token=" + requestParams.get("token"));
			System.out.println(requestParams.get("token"));
			return modelAndView;
		}

		// Find the user associated with the reset token
		User user = userService.findByConfirmationToken((String) requestParams.get("token"));

		// Set new password
		user.setPassword(bCryptPasswordEncoder.encode((CharSequence) 
				requestParams.get("password")));

		// Set user to enabled
		user.setEnabled(true);

		// Save user
		userService.save(user);

		modelAndView.addObject("successMessage", "Your password has been set!");
		return modelAndView;
	}

}
