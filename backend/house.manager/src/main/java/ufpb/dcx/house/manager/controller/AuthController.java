package ufpb.dcx.house.manager.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufpb.dcx.house.manager.dto.AuthRequests.LoginRequest;
import ufpb.dcx.house.manager.dto.AuthRequests.RegisterRequest;
import ufpb.dcx.house.manager.dto.UserResponse;
import ufpb.dcx.house.manager.model.User;
import ufpb.dcx.house.manager.security.UserPrincipal;
import ufpb.dcx.house.manager.service.AuthService;
import ufpb.dcx.house.manager.service.AuthService.AuthResult;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = authService.register(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, createTokenCookie(result.token()).toString())
            .body(result.user());
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authService.login(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, createTokenCookie(result.token()).toString())
            .body(result.user());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(0)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = authService.getCurrentUser(principal.id());
        return authService.toResponse(user);
    }

    private ResponseCookie createTokenCookie(String token) {
        return ResponseCookie.from("token", token)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(60L * 60 * 24 * 7)
            .build();
    }
}
