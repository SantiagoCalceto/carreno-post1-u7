package com.universidad.tareas.service;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Repositorio en memoria (la persistencia con JPA llega en la Unidad 8).
 *
 * @Service: Spring crea UNA sola instancia (singleton) y la inyecta tanto en
 * TareaController (vista) como en TareaApiController (API). Por eso ambas capas
 * leen y escriben exactamente los mismos datos.
 *
 * Al ser un singleton, varios hilos de Tomcat lo usan a la vez: se usa un mapa
 * concurrente ordenado por id y un AtomicLong, en lugar de LinkedHashMap y
 * Long++ (que no son seguros con peticiones simultáneas).
 */
@Service
public class TareaService {

    private final Map<Long, Tarea> tareas = new ConcurrentSkipListMap<>();
    private final AtomicLong contadorId = new AtomicLong(1);

    public TareaService() {
        // Datos de ejemplo para arrancar
        crear(new Tarea(null, "Configurar entorno Spring Boot", "Instalar JDK 17, Maven y el IDE",
                Prioridad.ALTA, LocalDate.now().plusDays(1), false));
        crear(new Tarea(null, "Diseñar el modelo de dominio", "Definir clase Tarea y enum Prioridad",
                Prioridad.MEDIA, LocalDate.now().plusDays(3), false));
        crear(new Tarea(null, "Escribir pruebas unitarias", "Cubrir TareaService con JUnit",
                Prioridad.BAJA, LocalDate.now().plusDays(7), true));
    }

    public List<Tarea> obtenerTodas() {
        return new ArrayList<>(tareas.values());
    }

    /** Filtro combinable: un parámetro null significa "no filtrar por ese criterio". */
    public List<Tarea> filtrar(Prioridad prioridad, Boolean completada) {
        List<Tarea> resultado = new ArrayList<>();
        for (Tarea t : tareas.values()) {
            boolean coincidePrioridad = (prioridad == null) || t.getPrioridad() == prioridad;
            boolean coincideEstado = (completada == null) || t.isCompletada() == completada;
            if (coincidePrioridad && coincideEstado) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    public Optional<Tarea> buscarPorId(Long id) {
        return Optional.ofNullable(tareas.get(id));
    }

    /**
     * Crea una tarea nueva. El id SIEMPRE lo asigna el servidor: si el cliente
     * envía uno (por ejemplo en el JSON de la API), se ignora. De lo contrario,
     * un POST con "id": 1 sobrescribiría una tarea existente.
     */
    public Tarea crear(Tarea tarea) {
        tarea.setId(contadorId.getAndIncrement());
        tareas.put(tarea.getId(), tarea);
        return tarea;
    }

    /**
     * Reemplaza los datos de una tarea existente. Devuelve vacío si el id no
     * existe: actualizar nunca crea registros con ids inventados por el cliente.
     */
    public Optional<Tarea> actualizar(Long id, Tarea datos) {
        if (id == null || !tareas.containsKey(id)) {
            return Optional.empty();
        }
        datos.setId(id);
        tareas.put(id, datos);
        return Optional.of(datos);
    }

    public Optional<Tarea> marcarCompletada(Long id) {
        Tarea tarea = tareas.get(id);
        if (tarea == null) {
            return Optional.empty();
        }
        tarea.setCompletada(true);
        return Optional.of(tarea);
    }

    public boolean eliminar(Long id) {
        return tareas.remove(id) != null;
    }
}
