package com.sparta.omin.app.controller.user;

import com.sparta.omin.app.model.user.dto.request.UserInfoEditRequest;
import com.sparta.omin.app.model.user.dto.response.UserInfo;
import com.sparta.omin.app.model.user.service.UserReadService;
import com.sparta.omin.app.model.user.service.UserWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
	public ResponseEntity<UserInfo> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
		log.info("userDetails: {}", userDetails);
		log.info("userDetails.getUsername(): {}", userDetails.getUsername());
		log.info("userRoles: {}", userDetails.getAuthorities());
		return ResponseEntity.ok()
			.body(UserInfo.from(userReadService.getUserInfo(userDetails.getUsername())));
	}

	@PutMapping
	public ResponseEntity<UserInfo> editUserInfo(@AuthenticationPrincipal UserDetails userDetails,
		@RequestBody UserInfoEditRequest request) {
		return ResponseEntity.ok().body(
			UserInfo.from(userWriteService.editInfo(userDetails.getUsername(),
				request.nickname(),
				request.password()))
		);
	}

	@DeleteMapping
	public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
		userWriteService.deleteUser(userDetails.getUsername());
		return ResponseEntity.noContent().build();
	}

}
