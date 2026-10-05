package com.universidad.tareas.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración de las dos capas sobre el MISMO contexto de Spring:
 * verifican los códigos HTTP de la API y que la vista y la API comparten el
 * mismo TareaService. @DirtiesContext reinicia los datos en memoria entre pruebas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TareaIntegracionTest {

    private static final String MANANA = LocalDate.now().plusDays(1).toString();

    @Autowired
    private MockMvc mvc;

    private String json(String titulo) {
        return "{\"titulo\":\"" + titulo + "\",\"descripcion\":\"desde la prueba\","
             + "\"prioridad\":\"MEDIA\",\"fechaLimite\":\"" + MANANA + "\"}";
    }

    // ---------------------------------------------------------------- API REST

    @Test
    void listarConFiltrosCombinados() throws Exception {
        mvc.perform(get("/api/tareas").param("prioridad", "ALTA").param("completada", "false"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(1)))
           .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void crearValidaDevuelve201ConLocation() throws Exception {
        mvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON).content(json("Documentar la API")))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", endsWith("/api/tareas/4")))
           .andExpect(jsonPath("$.id").value(4))
           .andExpect(jsonPath("$.completada").value(false));
    }

    @Test
    void crearInvalidaDevuelve400ConErroresPorCampoEnJson() throws Exception {
        mvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"\",\"prioridad\":\"ALTA\",\"fechaLimite\":\"2020-01-01\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.titulo").value("El título es obligatorio"))
           .andExpect(jsonPath("$.fechaLimite").value("La fecha límite no puede ser anterior a hoy"));
    }

    @Test
    void jsonConPrioridadInexistenteDevuelve400() throws Exception {
        mvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON)
                .content(json("Prioridad rara").replace("MEDIA", "URGENTE")))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void crearIgnoraElIdDelCliente() throws Exception {
        mvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON)
                .content(json("No debe pisar la 1").replace("{", "{\"id\":1,")))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(4));
        mvc.perform(get("/api/tareas/1"))
           .andExpect(jsonPath("$.titulo").value("Configurar entorno Spring Boot"));
    }

    @Test
    void putReemplazaYPatchCompleta() throws Exception {
        mvc.perform(put("/api/tareas/2").contentType(MediaType.APPLICATION_JSON).content(json("Modelo revisado")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.titulo").value("Modelo revisado"));
        mvc.perform(put("/api/tareas/99").contentType(MediaType.APPLICATION_JSON).content(json("No existe")))
           .andExpect(status().isNotFound());
        mvc.perform(patch("/api/tareas/2/completar"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.completada").value(true));
        mvc.perform(patch("/api/tareas/99/completar"))
           .andExpect(status().isNotFound());
    }

    @Test
    void deleteDevuelve204YLuego404() throws Exception {
        mvc.perform(delete("/api/tareas/1")).andExpect(status().isNoContent());
        mvc.perform(get("/api/tareas/1")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/tareas/1")).andExpect(status().isNotFound());
    }

    // ------------------------------------------- vista y API: mismo TareaService

    @Test
    void loQueSeEliminaEnLaApiDesapareceDeLaVista() throws Exception {
        mvc.perform(get("/tareas")).andExpect(content().string(containsString("Configurar entorno Spring Boot")));
        mvc.perform(delete("/api/tareas/1")).andExpect(status().isNoContent());
        mvc.perform(get("/tareas"))
           .andExpect(status().isOk())
           .andExpect(content().string(not(containsString("Configurar entorno Spring Boot"))));
    }

    @Test
    void loQueSeCreaEnLaVistaApareceEnLaApi() throws Exception {
        mvc.perform(post("/tareas/guardar")
                .param("titulo", "Creada desde el formulario")
                .param("prioridad", "BAJA")
                .param("fechaLimite", MANANA))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/tareas"));
        mvc.perform(get("/api/tareas/4"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.titulo").value("Creada desde el formulario"));
    }

    // ------------------------------------------------------------- vista web

    @Test
    void formularioInvalidoVuelveAMostrarseConErroresYDatos() throws Exception {
        mvc.perform(post("/tareas/guardar")
                .param("titulo", "")
                .param("descripcion", "Texto que no debe perderse")
                .param("prioridad", "ALTA")
                .param("fechaLimite", MANANA))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("El título es obligatorio")))
           .andExpect(content().string(containsString("Texto que no debe perderse")));
    }

    @Test
    void editarTareaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/tareas/99/editar")).andExpect(status().isNotFound());
    }

    @Test
    void completarPorGetNoEstaPermitido() throws Exception {
        mvc.perform(get("/tareas/1/completar")).andExpect(status().isMethodNotAllowed());
    }
}
