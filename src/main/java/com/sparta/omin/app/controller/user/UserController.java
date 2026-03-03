package com.sparta.omin.app.controller.user;

import com.sparta.omin.app.model.user.dto.request.UserInfoEditRequest;
import com.sparta.omin.app.model.user.dto.response.UserInfo;
import com.sparta.omin.app.model.user.service.UserReadService;
import com.sparta.omin.app.model.user.service.UserWriteService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserReadService userReadService;
	private final UserWriteService userWriteService;

	@GetMapping
	public ResponseEntity<UserInfo> getUserInfo(Principal principal) {
		return ResponseEntity.ok()
			.body(UserInfo.from(userReadService.getUserInfo(principal.getName())));
	}

	@PutMapping
	public ResponseEntity<UserInfo> editUserInfo(Principal principal,
		@RequestBody UserInfoEditRequest request) {
		return ResponseEntity.ok().body(
			UserInfo.from(userWriteService.editInfo(principal.getName(),
				request.nickname(),
				request.password()))
		);
	}

	@DeleteMapping
	public ResponseEntity<Void> deleteUser(Principal principal) {
		userWriteService.deleteUser(principal.getName());
		return ResponseEntity.noContent().build();
	}

}
