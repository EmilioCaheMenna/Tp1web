package com.example.demo.service;

import com.example.demo.dto.AuthRequestDTO;
import com.example.demo.dto.UsuarioResponseDTO;
import com.example.demo.exception.EmailYaRegistradoException;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void registrar_passwordEnTextoPlano_guardaPasswordHasheadaYNoEnTextoPlano() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, jwtService);

        String rawPassword = "miPasswordSecreta123";
        AuthRequestDTO request = new AuthRequestDTO("nuevo@test.com", rawPassword);

        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            return new Usuario(1L, u.getEmail(), u.getPasswordHash(), u.getRol());
        });

        UsuarioResponseDTO response = usuarioService.registrar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioGuardado = captor.getValue();
        assertNotEquals(rawPassword, usuarioGuardado.getPasswordHash(), "Nunca debe guardarse la contraseña en texto plano");
        assertTrue(passwordEncoder.matches(rawPassword, usuarioGuardado.getPasswordHash()), "El hash guardado debe corresponder a la contraseña original");
        assertEquals("USER", response.rol());
    }

    @Test
    void registrar_emailDuplicado_lanzaEmailYaRegistradoException() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, jwtService);
        AuthRequestDTO request = new AuthRequestDTO("existente@test.com", "123456");
        when(usuarioRepository.existsByEmail("existente@test.com")).thenReturn(true);

        assertThrows(EmailYaRegistradoException.class, () -> usuarioService.registrar(request));
    }
}
