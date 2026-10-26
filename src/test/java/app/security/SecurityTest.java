package app.security;

import app.config.SecurityConfig;
import app.controller.AuthController;
import app.controller.PlayerController;
import app.controller.ProtectedController;
import app.models.User;
import app.repositories.UserRepository;
import app.service.PlayerService;
import app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AuthController.class, ProtectedController.class, PlayerController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.security.jwt.secret=test-signing-secret-with-at-least-32-bytes")
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void rejectsProtectedRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUserRoleForAdminRoute() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptsAdminRoleForAdminRoute() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void acceptsUserRoleForUserRoutes() throws Exception {
        mockMvc.perform(get("/api/user/private-resource")
                        .header("Authorization", "Bearer " + token("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsPlayerRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/players"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsAuthenticatedPlayerRequest() throws Exception {
        when(playerService.getAllPlayers()).thenReturn(List.of());

        mockMvc.perform(get("/players")
                        .header("Authorization", "Bearer " + token("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void loginReturnsTokenAcceptedByJwtDecoder() throws Exception {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("encoded-password");
        user.setRoles(Set.of("USER"));
        when(userRepository.findByUsername("test-user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        String token = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-user\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("test-user", jwtDecoder.decode(token).getSubject());
        assertEquals("USER", jwtDecoder.decode(token).getClaimAsString("role"));
    }

    @Test
    void jwtCanBeValidatedByAnotherApplicationInstanceUsingSameSecret() {
        String token = token("USER");

        assertEquals("test-user", new SecurityConfig(
                "test-signing-secret-with-at-least-32-bytes")
                .jwtDecoder()
                .decode(token)
                .getSubject());
    }

    private String token(String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject("test-user")
                .claim("role", role)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
