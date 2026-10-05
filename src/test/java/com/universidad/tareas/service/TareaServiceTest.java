package com.universidad.tareas.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba unitaria pura: no levanta Spring. Es posible porque TareaService no
 * depende del contenedor (se instancia con new, igual que en un test de los
 * controladores que reciben el servicio por constructor).
 */
class TareaServiceTest {

    private TareaService servicio;

    @BeforeEach
    void setUp() {
        servicio = new TareaService();   // 3 tareas de ejemplo: ALTA, MEDIA, BAJA(completada)
    }

    @Test
    void filtrarSinParametrosDevuelveTodas() {
        assertThat(servicio.filtrar(null, null)).hasSize(3);
    }

    @Test
    void filtrarCombinaPrioridadYEstado() {
        assertThat(servicio.filtrar(Prioridad.ALTA, false)).extracting(Tarea::getId).containsExactly(1L);
        assertThat(servicio.filtrar(null, true)).extracting(Tarea::getId).containsExactly(3L);
        assertThat(servicio.filtrar(Prioridad.BAJA, false)).isEmpty();
    }

    @Test
    void crearIgnoraElIdEnviadoPorElCliente() {
        Tarea intento = nueva("Intento de sobrescribir");
        intento.setId(1L);

        Tarea creada = servicio.crear(intento);

        assertThat(creada.getId()).isEqualTo(4L);
        assertThat(servicio.buscarPorId(1L)).hasValueSatisfying(
            t -> assertThat(t.getTitulo()).isEqualTo("Configurar entorno Spring Boot"));
    }

    @Test
    void actualizarIdInexistenteNoCreaNada() {
        assertThat(servicio.actualizar(99L, nueva("Fantasma"))).isEmpty();
        assertThat(servicio.obtenerTodas()).hasSize(3);
    }

    @Test
    void marcarCompletadaYEliminar() {
        assertThat(servicio.marcarCompletada(1L)).hasValueSatisfying(
            t -> assertThat(t.isCompletada()).isTrue());
        assertThat(servicio.eliminar(1L)).isTrue();
        assertThat(servicio.eliminar(1L)).isFalse();
        assertThat(servicio.marcarCompletada(1L)).isEmpty();
    }

    private Tarea nueva(String titulo) {
        return new Tarea(null, titulo, null, Prioridad.MEDIA, LocalDate.now(), false);
    }
}
