package com.universidad.tareas.controller;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST JSON del gestor de tareas: todas las rutas bajo /api/tareas. */
@RestController
@RequestMapping("/api/tareas")
public class TareaApiController {

    private final TareaService servicio;

    // Mismo TareaService de la Parte 1: al ser un @Service singleton gestionado
    // por Spring, no se crea una segunda instancia ni un servicio paralelo.
    // TareaController (vista) y TareaApiController (API) leen y escriben sobre
    // exactamente los mismos datos en memoria.
    public TareaApiController(TareaService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/tareas?prioridad=ALTA&completada=false -> 200 OK + JSON filtrado. */
    @GetMapping
    public ResponseEntity<List<Tarea>> listar(
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Boolean completada) {
        return ResponseEntity.ok(servicio.filtrar(prioridad, completada));
    }

    /** GET /api/tareas/{id} -> 200 OK + JSON, o 404 Not Found. */
    @GetMapping("/{id}")
    public ResponseEntity<Tarea> buscar(@PathVariable Long id) {
        return servicio.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/tareas -> 201 Created + cabecera Location + JSON creado,
     * o 400 si falla @Valid (lo responde ApiErrorHandler).
     */
    @PostMapping
    public ResponseEntity<Tarea> crear(@Valid @RequestBody Tarea tarea) {
        Tarea creada = servicio.crear(tarea);   // el id lo asigna el servidor
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    /** PUT /api/tareas/{id} -> reemplazo COMPLETO del recurso: 200 OK, 404 o 400. */
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizar(@PathVariable Long id, @Valid @RequestBody Tarea tarea) {
        return servicio.actualizar(id, tarea)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/tareas/{id}/completar -> actualización PARCIAL de un solo campo
     * (completada): 200 OK, o 404. Con PUT el cliente tendría que reenviar
     * título, descripción, prioridad y fecha solo para marcar la tarea como hecha.
     */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<Tarea> completar(@PathVariable Long id) {
        return servicio.marcarCompletada(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/tareas/{id} -> 204 No Content, o 404. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!servicio.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
