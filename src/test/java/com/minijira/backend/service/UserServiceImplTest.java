package com.minijira.backend.service;

import com.minijira.backend.dto.UserRequest;
import com.minijira.backend.dto.UserResponse;
import com.minijira.backend.exception.BusinessException;
import com.minijira.backend.exception.ResourceNotFoundException;
import com.minijira.backend.model.User;
import com.minijira.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl — unit tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User buildUser(Long id, String username, String email) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("hashed_password");
        u.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        return u;
    }

    private UserRequest buildRequest(String username, String email) {
        UserRequest req = new UserRequest();
        req.setUsername(username);
        req.setEmail(email);
        return req;
    }

    // ------------------------------------------------------------------ //
    // findAll
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getAllUsers_returnsListOfUsers: devuelve todos los usuarios mapeados a DTO")
    void getAllUsers_returnsListOfUsers() {
        User u1 = buildUser(1L, "alice", "alice@test.com");
        User u2 = buildUser(2L, "bob", "bob@test.com");
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("alice");
        assertThat(result.get(1).getEmail()).isEqualTo("bob@test.com");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers_emptyRepository: devuelve lista vacía cuando no hay usuarios")
    void getAllUsers_emptyRepository_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.findAll();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    // ------------------------------------------------------------------ //
    // findById
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getUserById_existingId_returnsUser: retorna DTO correcto para id existente")
    void getUserById_existingId_returnsUser() {
        User user = buildUser(10L, "carlos", "carlos@test.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        UserResponse result = userService.findById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUsername()).isEqualTo("carlos");
        assertThat(result.getEmail()).isEqualTo("carlos@test.com");
        assertThat(result.getCreatedAt()).isNotNull();
        verify(userRepository).findById(10L);
    }

    @Test
    @DisplayName("getUserById_nonExistingId_throwsResourceNotFoundException: lanza excepción para id inexistente")
    void getUserById_nonExistingId_throwsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository).findById(999L);
    }

    // ------------------------------------------------------------------ //
    // create
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createUser_validRequest_returnsUserResponse: crea usuario y devuelve DTO")
    void createUser_validRequest_returnsUserResponse() {
        UserRequest request = buildRequest("newuser", "new@test.com");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        User saved = buildUser(5L, "newuser", "new@test.com");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse result = userService.create(request);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser_duplicateUsername_throwsBusinessException")
    void createUser_duplicateUsername_throwsBusinessException() {
        UserRequest request = buildRequest("existinguser", "otro@test.com");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existinguser");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser_duplicateEmail_throwsBusinessException")
    void createUser_duplicateEmail_throwsBusinessException() {
        UserRequest request = buildRequest("uniqueuser", "taken@test.com");
        when(userRepository.existsByUsername("uniqueuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("taken@test.com");

        verify(userRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    // update
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("updateUser_validRequest_updatesAndReturnsDto")
    void updateUser_validRequest_updatesAndReturnsDto() {
        User existing = buildUser(1L, "oldname", "old@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        User saved = buildUser(1L, "newname", "new@test.com");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserRequest request = buildRequest("newname", "new@test.com");
        UserResponse result = userService.update(1L, request);

        assertThat(result.getUsername()).isEqualTo("newname");
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser_sameUsernameAndEmail_doesNotThrowDuplicateException")
    void updateUser_sameUsernameAndEmail_doesNotThrowDuplicateException() {
        // Si el usuario cambia otros campos pero mantiene username y email, no debe fallar
        User existing = buildUser(1L, "samename", "same@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        // existsByUsername/Email no se llaman porque los valores son iguales
        User saved = buildUser(1L, "samename", "same@test.com");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserRequest request = buildRequest("samename", "same@test.com");

        assertThatCode(() -> userService.update(1L, request)).doesNotThrowAnyException();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser_nonExistingId_throwsResourceNotFoundException")
    void updateUser_nonExistingId_throwsResourceNotFoundException() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        UserRequest request = buildRequest("cualquier", "cualquier@test.com");

        assertThatThrownBy(() -> userService.update(42L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");

        verify(userRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    // delete
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("deleteUser_existingId_deletesSuccessfully: invoca deleteById para id existente")
    void deleteUser_existingId_deletesSuccessfully() {
        when(userRepository.existsById(3L)).thenReturn(true);

        userService.delete(3L);

        verify(userRepository).existsById(3L);
        verify(userRepository).deleteById(3L);
    }

    @Test
    @DisplayName("deleteUser_nonExistingId_throwsResourceNotFoundException: lanza excepción para id inexistente")
    void deleteUser_nonExistingId_throwsResourceNotFoundException() {
        when(userRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");

        verify(userRepository, never()).deleteById(any());
    }
}
