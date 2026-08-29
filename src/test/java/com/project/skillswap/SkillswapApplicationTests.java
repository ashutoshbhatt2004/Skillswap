package com.project.skillswap;

import com.project.skillswap.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SkillswapApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void legacyRoleCodesAreNormalizedToFullValues() {
		User user = new User();
		user.setRole("M");
		assertThat(user.getRole()).isEqualTo("MENTOR");

		User student = new User();
		student.setRole("S");
		assertThat(student.getRole()).isEqualTo("STUDENT");
	}

}
