package com.attend;
import com.attend.entity.User;
import com.attend.repo.UserRepo;
import com.attend.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class SeedConfig {
  @Bean CommandLineRunner seed(UserRepo users, AuthService auth){
    return args -> {
      if(users.count()==0){
        for(String[] r: new String[][]{{"Prof Demo","prof@demo","prof123","PROFESSOR"},
            {"Aarav","s1@demo","s123","STUDENT"},{"Diya","s2@demo","s123","STUDENT"},{"Kabir","s3@demo","s123","STUDENT"}}){
          User u=new User(); u.name=r[0]; u.identifier=r[1];
          u.passwordHash=auth.hash(r[2]); // demo passwords are plaintext in source, hashed at rest
          u.role=r[3]; users.save(u);
        }
      }
    };
  }
}
