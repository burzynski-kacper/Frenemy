package com.example.demo;

import com.example.demo.model.CharacterClass;
import com.example.demo.repository.CharacterClassRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner demo(CharacterClassRepository characterClassRepository) {
        return (args) -> {
            if (characterClassRepository.count() == 0) {
                characterClassRepository.save(new CharacterClass("Warrior", "Strength"));
                characterClassRepository.save(new CharacterClass("Mage", "Intelligence"));
                characterClassRepository.save(new CharacterClass("Scout", "Dexterity"));
                System.out.println("Default character class has been added to DB.");
            }
        };
    }
}
