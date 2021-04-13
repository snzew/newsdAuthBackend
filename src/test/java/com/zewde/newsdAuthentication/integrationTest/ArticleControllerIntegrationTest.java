package com.zewde.newsdAuthentication.integrationTest;


import com.zewde.newsdAuthentication.entities.Article;
import com.zewde.newsdAuthentication.entities.User;
import com.zewde.newsdAuthentication.repositories.ArticleRepository;
import com.zewde.newsdAuthentication.repositories.UserRepository;
import com.zewde.newsdAuthentication.unitTests.utils.JWTTokenUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.Cookie;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ArticleControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ArticleRepository articleRepository;

  @Autowired
  private JWTTokenUtils jwtTokenUtils;


  private String USERNAME = "someUser";

  private int getUserIdforUser(){
    User user = new User();
    user.setEmail("someEmail@email.com");
    user.setUsername(USERNAME);
    user.setPassword("somePass1234!");
    user.setEnabled(true);

    userRepository.save(user);

    User u = userRepository.findByUsername(user.getUsername()).get();

    return u.getId();
  }

  private void setUpArticlesForUser(){
    int userId = getUserIdforUser();
    articleRepository.save(new Article(userId,"first source","first author","first Article", "first description","first url", "first url to image", "2020-04-09", "first  content", true));
    articleRepository.save(new Article(userId,"some second source","some  second author","some second title", "some second description","some second url", "some second url to image", "2020-04-09", "some second more content", true));
  }



  @Test
  public void getArticlesPerUser() throws Exception {
    String token = jwtTokenUtils.generateToken(USERNAME);
    try{
      mockMvc.perform(MockMvcRequestBuilders.get("/articles?username="+USERNAME).contentType(MediaType.APPLICATION_JSON).with(csrf()).cookie(new Cookie("jwtToken", token)))
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(0))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("first Article"));

    }catch(Exception e){
      System.out.println("**********************************");
      System.out.println(e.getMessage());
      System.out.println(e.getCause());
    }

  }



}
