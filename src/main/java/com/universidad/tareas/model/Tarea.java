package com.universidad.tareas.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Modelo compartido por la vista Thymeleaf y la API REST. Las reglas de
 * validación viven aquí, una sola vez: ambos controladores aplican @Valid
 * sobre esta misma clase, así que nunca pueden divergir.
 */
public class Tarea {

    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
    private String titulo;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;

    @NotNull(message = "La prioridad es obligatoria")
    private Prioridad prioridad;

    // @FutureOrPresent y no @Future: una tarea que vence el mismo día en que se
    // registra es válida (@Future la rechazaría por no ser estrictamente posterior)
    @NotNull(message = "La fecha límite es obligatoria")
    @FutureOrPresent(message = "La fecha límite no puede ser anterior a hoy")
    private LocalDate fechaLimite;

    private boolean completada;

    // Constructor sin argumentos: lo necesitan el binding de formularios y Jackson
    public Tarea() {}

    public Tarea(Long id, String titulo, String descripcion, Prioridad prioridad,
                 LocalDate fechaLimite, boolean completada) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.fechaLimite = fechaLimite;
        this.completada = completada;
    }

    // Getters y setters: los usan Thymeleaf (th:field) y Jackson (JSON)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }
    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }
}
