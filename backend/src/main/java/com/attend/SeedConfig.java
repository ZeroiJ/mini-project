package com.attend;
import com.attend.entity.User;
import com.attend.repo.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class SeedConfig {
  @Bean CommandLineRunner seed(UserRepo users){
    return args -> {
      if(users.count()==0){
        for(String[] r: new String[][]{{"Prof Demo","prof@demo","prof123","PROFESSOR"},
            {"Aarav","s1@demo","s123","STUDENT"},{"Diya","s2@demo","s123","STUDENT"},{"Kabir","s3@demo","s123","STUDENT"}}){
          User u=new User(); u.name=r[0]; u.identifier=r[1]; u.passwordHash=r[2]; u.role=r[3]; users.save(u);
        }
      }
    };
  }
}
