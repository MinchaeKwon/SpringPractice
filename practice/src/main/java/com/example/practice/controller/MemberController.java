package com.example.practice.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.practice.dto.MemberDto;
import com.example.practice.entity.Member;
import com.example.practice.response.CommonResponse;
import com.example.practice.response.ErrorResponse;
import com.example.practice.response.Response;
import com.example.practice.security.JwtRequest;
import com.example.practice.security.JwtResponse;
import com.example.practice.security.JwtTokenUtil;
import com.example.practice.security.JwtUserDetailsService;
import com.example.practice.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {

	@Autowired
	private MemberService memberService;

	@GetMapping(value = "/member/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> findById(@PathVariable("id") Long id) {
		Map<String, Object> response = new HashMap<>();

		Optional<Member> oUser = memberService.findById(id);
		if (oUser.isPresent()) {
			response.put("result", "SUCCESS");
			response.put("user", oUser.get());
		} else {
			response.put("result", "FAIL");
			response.put("reason", "일치하는 회원 정보가 없습니다. 사용자 id를 확인해주세요.");
		}

		return response;
	}

	@GetMapping(value = "/member/list", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> test() {

		Map<String, Object> members = new HashMap<>();

		members.put("result", memberService.findAll());

		return members;
	}

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailService;

	@Autowired
	private AuthenticationManager authenticationManager;

	// 일반 로그인
	@PostMapping(value = "/signin")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		String token;

		authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());

		final Member userDetails = (Member) userDetailService
				.authenticateByEmailAndPassword(authenticationRequest.getEmail(), authenticationRequest.getPassword());

		token = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new JwtResponse(token));
	}

	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}

	@PostMapping(value = "/signup")
	public ResponseEntity<?> signup(@RequestBody MemberDto infoDto) { // 회원 추가
		try {
			userDetailService.save(infoDto);
			return ResponseEntity.ok().body(new Response("회원가입을 성공적으로 완료했습니다."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("회원가입을 하는 도중 오류가 발생했습니다.", "500"));
		}

	}

	// 로그인한 사용자의 정보 가져오기 -> 필요한 정보만 return 해야 함
	@GetMapping(value = "/account/profile")
	public ResponseEntity<?> getMyAccount(@AuthenticationPrincipal Member member) {
		MemberDto.Profile dto = new MemberDto.Profile(member.getId(), member.getEmail());
		return ResponseEntity.ok().body(new CommonResponse<MemberDto.Profile>(dto));
	}

	// 카카오 로그인

	public ResponseEntity<?> kakaoRequest(@RequestBody JwtRequest authenticationRequest) throws Exception {

		return null;

	}

}
