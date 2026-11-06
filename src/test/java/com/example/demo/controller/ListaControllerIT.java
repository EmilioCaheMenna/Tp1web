package com.example.demo.controller;

import com.example.demo.model.Lista;
import com.example.demo.model.Usuario;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ListaControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("tp3_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ListaRepository listaRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Usuario usuarioDueno;
    private Usuario usuarioIntruso;
    private Usuario usuarioAdmin;
    private Lista listaDeDueno;

    @BeforeEach
    void setUp() {
        favoritoRepository.findAll().forEach(f -> favoritoRepository.deleteById(f.getId()));
        listaRepository.findAll().forEach(l -> listaRepository.deleteById(l.getId()));
        usuarioRepository.findAll().forEach(u -> usuarioRepository.deleteById(u.getId()));

        usuarioDueno = usuarioRepository.save(new Usuario(
                null, "dueno@test.com", passwordEncoder.encode("clave123"), "USER"
        ));
        usuarioIntruso = usuarioRepository.save(new Usuario(
                null, "intruso@test.com", passwordEncoder.encode("clave123"), "USER"
        ));
        usuarioAdmin = usuarioRepository.save(new Usuario(
                null, "admin@test.com", passwordEncoder.encode("admin123"), "ADMIN"
        ));

        listaDeDueno = listaRepository.save(new Lista(null, "Lista Privada de Dueno", usuarioDueno.getId()));
    }

    @Test
    void login_credencialesValidas_devuelve200ConToken() throws Exception {
        String body = """
                {
                  "email": "dueno@test.com",
                  "password": "clave123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_credencialesInvalidas_devuelve401() throws Exception {
        String body = """
                {
                  "email": "dueno@test.com",
                  "password": "passwordIncorrecta"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_listaPropia_devuelve200() throws Exception {
        String tokenDueno = jwtService.generarToken(usuarioDueno.getId(), usuarioDueno.getEmail(), usuarioDueno.getRol());

        mockMvc.perform(get("/api/listas/" + listaDeDueno.getId())
                        .header("Authorization", "Bearer " + tokenDueno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaDeDueno.getId()))
                .andExpect(jsonPath("$.nombre").value("Lista Privada de Dueno"));
    }

    @Test
    void obtener_listaAjena_devuelve403() throws Exception {
        String tokenIntruso = jwtService.generarToken(usuarioIntruso.getId(), usuarioIntruso.getEmail(), usuarioIntruso.getRol());

        mockMvc.perform(get("/api/listas/" + listaDeDueno.getId())
                        .header("Authorization", "Bearer " + tokenIntruso))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_adminAccediendoAListaAjena_devuelve200() throws Exception {
        String tokenAdmin = jwtService.generarToken(usuarioAdmin.getId(), usuarioAdmin.getEmail(), usuarioAdmin.getRol());

        mockMvc.perform(get("/api/listas/" + listaDeDueno.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaDeDueno.getId()));
    }
}
