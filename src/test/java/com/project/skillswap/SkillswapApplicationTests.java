package com.project.skillswap;

import com.project.skillswap.controller.AuthController;
import com.project.skillswap.entity.User;
import com.project.skillswap.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

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

	@Test
	void publicRegistrationRejectsAdminRole() {
		AuthController controller = new AuthController();
		UserService userService = mock(UserService.class);
		ReflectionTestUtils.setField(controller, "userService", userService);

		String result = controller.registerUser(
				"Admin User",
				"admin@example.com",
				"password123",
				"password123",
				"ADMIN"
		);

		assertThat(result).isEqualTo("redirect:/register?error=Admin%20registration%20is%20not%20allowed.");
		verifyNoInteractions(userService);
	}

	@Test
	void studentAndMentorRegistrationRemainAllowed() {
		AuthController controller = new AuthController();
		UserService userService = mock(UserService.class);
		ReflectionTestUtils.setField(controller, "userService", userService);

		String studentResult = controller.registerUser(
				"Student User",
				"student@example.com",
				"password123",
				"password123",
				"STUDENT"
		);
		String mentorResult = controller.registerUser(
				"Mentor User",
				"mentor@example.com",
				"password123",
				"password123",
				"MENTOR"
		);

		assertThat(studentResult).isEqualTo("redirect:/login?success=Account%20created.");
		assertThat(mentorResult).isEqualTo("redirect:/login?success=Account%20created.");
	}

}
