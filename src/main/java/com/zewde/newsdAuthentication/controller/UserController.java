package com.zewde.newsdAuthentication.controller;


import com.zewde.newsdAuthentication.Exceptions.EmailAlreadyExistException;
import com.zewde.newsdAuthentication.Exceptions.RegistrationConfirmationTokenNotFoundException;
import com.zewde.newsdAuthentication.Exceptions.UserNameAlreadyExistException;
import com.zewde.newsdAuthentication.entities.EmailService;
import com.zewde.newsdAuthentication.entities.RegistrationConfirmationToken;
import com.zewde.newsdAuthentication.entities.User;
import com.zewde.newsdAuthentication.service.ArticleService;
import com.zewde.newsdAuthentication.service.RegistrationConfirmationTokenService;
import com.zewde.newsdAuthentication.service.UserDetailsServiceImplementation;
import com.zewde.newsdAuthentication.utils.CookiesUtils;
import com.zewde.newsdAuthentication.utils.JWTTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;


@RestController
@RequestMapping(value="/auth",consumes= {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.TEXT_PLAIN_VALUE})
public class UserController {


  @Autowired
  private UserDetailsServiceImplementation userService;

  @Autowired
  private AuthenticationManager authManager;

  @Autowired
  private JWTTokenUtils jwtTokenUtils;

  @Autowired
  private ArticleService articleService;

  @Autowired
  private CookiesUtils cookiesUtils;

  @Autowired
  private RegistrationConfirmationTokenService registrationConfirmationTokenService;


  @Autowired
  public SimpleMailMessage mailMessage;


  @Autowired
  private EmailService emailService;

  @GetMapping("/register")
  public @ResponseBody String getRegister(){
    return "please register";
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody User user) throws UserNameAlreadyExistException, EmailAlreadyExistException, IOException, MessagingException {
//    User u;
    RegistrationConfirmationToken token;

    try{
      token = userService.registerUserAndReturnToken(user);
      String textMail = String.format(mailMessage.getText(), token.toString());

    }catch(EmailAlreadyExistException e){
      System.out.println(e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists", e);
    } catch(UserNameAlreadyExistException e){
      System.out.println(e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists", e);
    }catch(Exception e){
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Sorry something went wrong from Backend");
    }
   ;
    String textMail = String.format(mailMessage.getText(), token.toString());

    emailService.sendEmail(user.getEmail(),"confirmation newsdMe Token", textMail);
    return new ResponseEntity<>("",HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<?> loginUser(@RequestBody User user, HttpServletResponse response) throws BadCredentialsException, DisabledException{

    try{
      authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
    }catch(BadCredentialsException e){
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong username or password", e);
    }catch(DisabledException e){
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is disabled", e);
    }catch(Exception e){
      System.out.println(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error from backend", e);
    }

    User u = userService.loginUser(user);
    String token = jwtTokenUtils.generateToken(u.getUsername());

    Cookie cookie = cookiesUtils.createCookie("jwtToken", token);
    System.out.println(cookie);
    response.addCookie(cookie);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/logout")
  public ResponseEntity<?> logout() {
//    System.out.println("logout Called");
//    Cookie cookie = cookiesUtils.createCookie("jwtToken", "",0);
//    response.addCookie(cookie);
    return new ResponseEntity<>("is this the boddy",HttpStatus.OK);
  }

  @GetMapping("/confirmUser")
  public ResponseEntity<?> confirmUser(@RequestParam("token") String token){
    System.out.println(token);
    try{
      RegistrationConfirmationToken confirmToken =  registrationConfirmationTokenService.getToken(token);
      System.out.println(confirmToken);
      userService.confirmUser(confirmToken);

    }catch(RegistrationConfirmationTokenNotFoundException ex){

      return new ResponseEntity<>(ex,HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>("registration confirmation successful",HttpStatus.OK);

  }

}