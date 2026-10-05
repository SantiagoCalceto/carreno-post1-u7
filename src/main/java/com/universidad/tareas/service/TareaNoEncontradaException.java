package com.universidad.tareas.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando se pide una tarea que no existe. @ResponseStatus hace que
 * Spring responda 404 (y muestre templates/error/404.html) en lugar de un 500,
 * que es lo que produciría una RuntimeException genérica.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TareaNoEncontradaException extends RuntimeException {

    public TareaNoEncontradaException(Long id) {
        super("Tarea no encontrada: " + id);
    }
}
