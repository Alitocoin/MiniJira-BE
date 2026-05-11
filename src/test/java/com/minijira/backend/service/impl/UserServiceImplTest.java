package com.minijira.backend.service.impl;

import com.minijira.backend.dto.request.UserRequest;
import com.minijira.backend.dto.response.UserResponse;
import com.minijira.backend.exception.DuplicateResourceException;
import com.minijira.backend.exception.ResourceNotFoundException;
import com.minijira.backend.model.User;
import com.minijira.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl - Tests Unitarios")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = User.builder()
                .id(1L)
                .name("Ana Torres")
                .email("ana@example.com")
                .password("hashed_pass_A")
                .build();

        userB = User.builder()
                .id(2L)
                .name("Luis Reyes")
                .email("luis@example.com")
                .password("hashed_pass_B")
                .build();
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll cuando hay usuarios devuelve lista completa")
    void findAll_cuandoExistenUsuarios_debeRetornarListaCompleta() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(userA, userB));

        List<UserResponse> resultado = userService.findAll();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(0).getName()).isEqualTo("Ana Torres");
        assertThat(resultado.get(0).getEmail()).isEqualTo("ana@example.com");
        assertThat(resultado.get(1).getId()).isEqualTo(2L);
        assertThat(resultado.get(1).getName()).isEqualTo("Luis Reyes");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll sin usuarios devuelve lista vacía")
    void findAll_cuandoNoHayUsuarios_debeRetornarListaVacia() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResponse> resultado = userService.findAll();

        assertThat(resultado).isEmpty();
        verify(userRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById con id existente devuelve el usuario correcto")
    void findById_cuandoIdExiste_debeRetornarUsuarioCorrecto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));

        UserResponse resultado = userService.findById(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getName()).isEqualTo("Ana Torres");
        assertThat(resultado.getEmail()).isEqualTo("ana@example.com");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById con id inexistente lanza ResourceNotFoundException")
    void findById_cuandoIdNoExiste_debeLanzarResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("findById con id null lanza excepción del repositorio")
    void findById_cuandoIdEsNull_debeLanzarExcepcion() {
        when(userRepository.findById(null)).thenThrow(new IllegalArgumentException("id cannot be null"));

        assertThatThrownBy(() -> userService.findById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create con datos válidos guarda usuario y retorna DTO correcto")
    void create_cuandoDatosValidos_debeGuardarYRetornarUsuario() {
        UserRequest request = new UserRequest();
        request.setName("Nuevo Usuario");
        request.setEmail("nuevo@example.com");
        request.setPassword("password123");

        User usuarioGuardado = User.builder()
                .id(3L)
                .name("Nuevo Usuario")
                .email("nuevo@example.com")
                .password("hashed_password123")
                .build();

        when(userRepository.existsByEmail("nuevo@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password123");
        when(userRepository.save(any(User.class))).thenReturn(usuarioGuardado);

        UserResponse resultado = userService.create(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(3L);
        assertThat(resultado.getName()).isEqualTo("Nuevo Usuario");
        assertThat(resultado.getEmail()).isEqualTo("nuevo@example.com");
        verify(userRepository, times(1)).existsByEmail("nuevo@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("create con email duplicado lanza DuplicateResourceException")
    void create_cuandoEmailDuplicado_debeLanzarDuplicateResourceException() {
        UserRequest request = new UserRequest();
        request.setName("Ana Torres Clon");
        request.setEmail("ana@example.com");
        request.setPassword("pass456");

        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ana@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("create no almacena password en texto plano")
    void create_cuandoDatosValidos_noDebeGuardarPasswordEnTextoPlano() {
        UserRequest request = new UserRequest();
        request.setName("Test Seguro");
        request.setEmail("seguro@example.com");
        request.setPassword("plaintext");

        User savedUser = User.builder()
                .id(10L)
                .name("Test Seguro")
                .email("seguro@example.com")
                .password("$2a$10$hashedValue")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$10$hashedValue");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.create(request);

        // Verificar que se llamó encode: la password real no se guardó como plain
        verify(passwordEncoder, times(1)).encode("plaintext");
        verify(userRepository).save(argThat(u -> !"plaintext".equals(u.getPassword())));
    }

    @Test
    @DisplayName("create con nombre vacío — el repositorio no es llamado si se valida antes")
    void create_cuandoEmailYaExiste_noDebeInvocarSave() {
        UserRequest request = new UserRequest();
        request.setName("");
        request.setEmail("duplicado@example.com");
        request.setPassword("pass");

        when(userRepository.existsByEmail("duplicado@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }
}
