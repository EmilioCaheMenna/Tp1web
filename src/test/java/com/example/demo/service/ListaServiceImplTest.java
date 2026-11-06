package com.example.demo.service;

import com.example.demo.dto.ListaRequestDTO;
import com.example.demo.dto.ListaResponseDTO;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.model.Lista;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListaServiceImplTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    @InjectMocks
    private ListaServiceImpl listaService;

    @BeforeEach
    void setUp() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(42L, "usuario@test.com", "USER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                authenticatedUser, null, java.util.List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void crear_usuarioAutenticado_asociaListaAlUsuarioAutenticado() {
        ListaRequestDTO request = new ListaRequestDTO("Mis Regalos");
        when(listaRepository.save(any(Lista.class))).thenAnswer(inv -> {
            Lista arg = inv.getArgument(0);
            return new Lista(10L, arg.getNombre(), arg.getUsuarioId());
        });

        ListaResponseDTO response = listaService.crear(request);

        ArgumentCaptor<Lista> captor = ArgumentCaptor.forClass(Lista.class);
        verify(listaRepository).save(captor.capture());

        Lista guardada = captor.getValue();
        assertEquals(42L, guardada.getUsuarioId(), "La lista creada debe quedar asociada al id del usuario autenticado");
        assertEquals("Mis Regalos", guardada.getNombre());
        assertEquals(10L, response.id());
    }

    @Test
    void obtener_listaAjena_lanzaAccesoDenegado() {
        Lista listaAjena = new Lista(99L, "Lista de otro usuario", 999L);
        when(listaRepository.findById(99L)).thenReturn(Optional.of(listaAjena));

        assertThrows(AccesoDenegadoException.class, () -> listaService.obtenerPorId(99L));
    }
}
