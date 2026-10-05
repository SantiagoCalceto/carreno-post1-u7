package com.universidad.tareas.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce los errores de la API a JSON. assignableTypes lo limita a
 * TareaApiController: los errores de validación del formulario Thymeleaf se
 * resuelven aparte, con BindingResult dentro de TareaController.guardar(),
 * porque ese flujo debe volver a renderizar HTML con los mensajes, no JSON.
 */
@RestControllerAdvice(assignableTypes = TareaApiController.class)
public class ApiErrorHandler {

    /** @Valid falló sobre el @RequestBody -> 400 con un mapa campo -> mensaje. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // Un título vacío viola @NotBlank y @Size(min = 3) a la vez: se deja un
            // solo mensaje por campo y, si hay varios, gana el de @NotBlank
            if (!errores.containsKey(error.getField()) || "NotBlank".equals(error.getCode())) {
                errores.put(error.getField(), error.getDefaultMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    /**
     * JSON mal formado o con un valor imposible de convertir (por ejemplo
     * "prioridad":"URGENTE" o una fecha inválida): también 400 en JSON, en vez de
     * la respuesta genérica de Spring Boot.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("error",
            "JSON mal formado o con un valor inválido (prioridad: ALTA, MEDIA o BAJA; fecha: yyyy-MM-dd)"));
    }

    /** Parámetro de URL con tipo incorrecto, por ejemplo ?prioridad=URGENTE o /api/tareas/abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> manejarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(Map.of(ex.getName(),
            "Valor no válido: " + ex.getValue()));
    }
}
