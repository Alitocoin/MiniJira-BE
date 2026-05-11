package com.minijira.backend.service;

import com.minijira.backend.dto.AuthResponse;
import com.minijira.backend.dto.LoginRequest;
import com.minijira.backend.dto.RegisterRequest;
import com.minijira.backend.exception.BusinessException;
import com.minijira.backend.model.User;
import com.minijira.backend.repository.UserRepository;
import com.minijira.backend.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl — unit tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // ------------------------------------------------------------------ //
    // Fixtures / helpers
    // ------------------------------------------------------------------ //

    private RegisterRequest buildRegisterRequest(String username, String email, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private LoginRequest buildLoginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private User buildUser(String username, String email, String hashedPassword) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        return user;
    }

    // ------------------------------------------------------------------ //
    // register()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("register_exitoso: username y email libres → guarda usuario, genera token, devuelve AuthResponse correcta")
    void register_exitoso_devuelveAuthResponseConDatosCorrectos() {
        RegisterRequest request = buildRegisterRequest("juanito", "juanito@test.com", "secret123");

        when(userRepository.existsByUsername("juanito")).thenReturn(false);
        when(userRepository.existsByEmail("juanito@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("juanito@test.com")).thenReturn("jwt.token.generado");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.generado");
        assertThat(response.getUsername()).isEqualTo("juanito");
        assertThat(response.getEmail()).isEqualTo("juanito@test.com");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register_usernameDuplicado: lanza BusinessException con mensaje que contiene 'username'")
    void register_usernameDuplicado_lanzaBusinessException() {
        RegisterRequest request = buildRegisterRequest("repetido", "nuevo@test.com", "pass123");

        when(userRepository.existsByUsername("repetido")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("username");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register_emailDuplicado: lanza BusinessException con mensaje que contiene 'email'")
    void register_emailDuplicado_lanzaBusinessException() {
        RegisterRequest request = buildRegisterRequest("nuevo_user", "tomado@test.com", "pass123");

        when(userRepository.existsByUsername("nuevo_user")).thenReturn(false);
        when(userRepository.existsByEmail("tomado@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register_passwordHasheada: passwordEncoder.encode() es llamado con la contraseña en plano")
    void register_passwordEsHasheadaAntesDeGuardar() {
        RegisterRequest request = buildRegisterRequest("usuario1", "u1@test.com", "miPasswordPlano");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("miPasswordPlano")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(anyString())).thenReturn("token");

        authService.register(request);

        verify(passwordEncoder).encode("miPasswordPlano");
    }

    @Test
    @DisplayName("register_tokenGeneradoConEmailCorrecto: jwtUtil.generateToken() se llama con el email del request")
    void register_tokenGeneradoConEmailDelUsuario() {
        RegisterRequest request = buildRegisterRequest("usuario2", "correo@dominio.com", "pass");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("correo@dominio.com")).thenReturn("jwt.correcto");

        authService.register(request);

        verify(jwtUtil).generateToken("correo@dominio.com");
    }

    @Test
    @DisplayName("register_tipoBearer: AuthResponse tiene type 'Bearer' por defecto")
    void register_authResponseTieneTypeBearer() {
        RegisterRequest request = buildRegisterRequest("bbearer", "bearer@test.com", "pass1234");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(anyString())).thenReturn("token.bearer");

        AuthResponse response = authService.register(request);

        assertThat(response.getType()).isEqualTo("Bearer");
    }

    // ------------------------------------------------------------------ //
    // login()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("login_exitoso: email existe y password coincide → devuelve AuthResponse con token y datos del usuario")
    void login_exitoso_devuelveAuthResponseCompleta() {
        LoginRequest request = buildLoginRequest("ana@test.com", "password123");
        User userEnBD = buildUser("ana", "ana@test.com", "$2a$hashed_password");

        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(userEnBD));
        when(passwordEncoder.matches("password123", "$2a$hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken("ana@test.com")).thenReturn("jwt.de.ana");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.de.ana");
        assertThat(response.getUsername()).isEqualTo("ana");
        assertThat(response.getEmail()).isEqualTo("ana@test.com");
    }

    @Test
    @DisplayName("login_emailNoEncontrado: lanza BusinessException con mensaje 'Credenciales inválidas'")
    void login_emailNoExiste_lanzaBusinessException() {
        LoginRequest request = buildLoginRequest("inexistente@test.com", "cualquier");

        when(userRepository.findByEmail("inexistente@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login_passwordIncorrecta: lanza BusinessException con mensaje 'Credenciales inválidas'")
    void login_passwordIncorrecta_lanzaBusinessException() {
        LoginRequest request = buildLoginRequest("pepe@test.com", "wrongpassword");
        User userEnBD = buildUser("pepe", "pepe@test.com", "$2a$hashed");

        when(userRepository.findByEmail("pepe@test.com")).thenReturn(Optional.of(userEnBD));
        when(passwordEncoder.matches("wrongpassword", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales inválidas");

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("login_tokenGeneradoConEmailCorrecto: jwtUtil.generateToken() se llama con el email del usuario encontrado")
    void login_tokenGeneradoConEmailDelUsuario() {
        LoginRequest request = buildLoginRequest("lucia@test.com", "miPass");
        User userEnBD = buildUser("lucia", "lucia@test.com", "hashed_pass");

        when(userRepository.findByEmail("lucia@test.com")).thenReturn(Optional.of(userEnBD));
        when(passwordEncoder.matches("miPass", "hashed_pass")).thenReturn(true);
        when(jwtUtil.generateToken("lucia@test.com")).thenReturn("jwt.lucia");

        authService.login(request);

        verify(jwtUtil).generateToken("lucia@test.com");
    }

    @Test
    @DisplayName("login_mensajeGenericoEnAmbosErrores: email-not-found y password-wrong devuelven el mismo mensaje (principio de seguridad)")
    void login_emailNoEncontradoYPasswordIncorrecta_mismaMensajeDeError() {
        LoginRequest requestEmailMal = buildLoginRequest("noexiste@test.com", "pass");
        LoginRequest requestPassMal = buildLoginRequest("existe@test.com", "wrongpass");
        User userEnBD = buildUser("alguien", "existe@test.com", "hash_correcto");

        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existe@test.com")).thenReturn(Optional.of(userEnBD));
        when(passwordEncoder.matches("wrongpass", "hash_correcto")).thenReturn(false);

        Throwable errorEmailMal = catchThrowable(() -> authService.login(requestEmailMal));
        Throwable errorPassMal = catchThrowable(() -> authService.login(requestPassMal));

        assertThat(errorEmailMal).isInstanceOf(BusinessException.class);
        assertThat(errorPassMal).isInstanceOf(BusinessException.class);
        assertThat(errorEmailMal.getMessage()).isEqualTo(errorPassMal.getMessage());
    }

    @Test
    @DisplayName("login_passwordMatchesLlamadoConArgumentosCorrectos: (rawPassword, hashedPassword)")
    void login_passwordMatchesLlamadoConArgumentosCorrectos() {
        String rawPass = "miContrasena";
        String hashedPass = "$2a$10$realHashAqui";
        LoginRequest request = buildLoginRequest("mario@test.com", rawPass);
        User userEnBD = buildUser("mario", "mario@test.com", hashedPass);

        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(userEnBD));
        when(passwordEncoder.matches(rawPass, hashedPass)).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("token");

        authService.login(request);

        verify(passwordEncoder).matches(rawPass, hashedPass);
    }
}
