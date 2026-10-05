package com.universidad.tareas.controller;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaNoEncontradaException;
import com.universidad.tareas.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Vista web (Thymeleaf) del gestor de tareas: todas las rutas bajo /tareas. */
@Controller
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService servicio;

    // Inyección por constructor: se evita @Autowired sobre el campo porque
    // acopla la clase al contenedor y obliga a levantar el contexto completo
    // para probarla. Con un único constructor, Spring inyecta la dependencia
    // automáticamente (desde la versión 4.3), el campo puede ser final y la
    // clase también se instancia a mano en un test pasando un TareaService.
    public TareaController(TareaService servicio) {
        this.servicio = servicio;
    }

    /** GET /tareas?prioridad=ALTA&completada=false -> lista filtrada. */
    @GetMapping
    public String listar(
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Boolean completada,
            Model model) {
        model.addAttribute("tareas", servicio.filtrar(prioridad, completada));
        model.addAttribute("prioridades", Prioridad.values());
        model.addAttribute("prioridadSeleccionada", prioridad);
        model.addAttribute("completadaSeleccionada", completada);
        return "tareas/lista";
    }

    @GetMapping("/nueva")
    public String formularioNueva(Model model) {
        return mostrarFormulario(new Tarea(), model);
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Tarea tarea = servicio.buscarPorId(id)
            .orElseThrow(() -> new TareaNoEncontradaException(id));
        return mostrarFormulario(tarea, model);
    }

    /**
     * POST /tareas/guardar: valida con @Valid. Si hay errores se vuelve a
     * renderizar el formulario (sin redirect, para no perder los datos ni los
     * mensajes); si es válido, se aplica Post/Redirect/Get.
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("tarea") Tarea tarea,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return mostrarFormulario(tarea, model);
        }
        if (tarea.getId() == null) {
            servicio.crear(tarea);
            redirect.addFlashAttribute("mensaje", "Tarea creada: " + tarea.getTitulo());
        } else {
            Long id = tarea.getId();
            servicio.actualizar(id, tarea).orElseThrow(() -> new TareaNoEncontradaException(id));
            redirect.addFlashAttribute("mensaje", "Tarea actualizada: " + tarea.getTitulo());
        }
        return "redirect:/tareas";
    }

    // POST y no GET: completar y eliminar modifican el estado del servidor.
    // Un GET debe ser seguro (sin efectos secundarios); si no, el precargador
    // del navegador o un rastreador web podría completar o borrar tareas solo
    // con visitar el enlace.
    @PostMapping("/{id}/completar")
    public String completar(@PathVariable Long id, RedirectAttributes redirect) {
        Tarea tarea = servicio.marcarCompletada(id)
            .orElseThrow(() -> new TareaNoEncontradaException(id));
        redirect.addFlashAttribute("mensaje", "Tarea completada: " + tarea.getTitulo());
        return "redirect:/tareas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        if (servicio.eliminar(id)) {
            redirect.addFlashAttribute("mensaje", "Tarea eliminada");
        }
        return "redirect:/tareas";
    }

    /** Datos comunes del formulario de crear y editar (y del reintento tras un error). */
    private String mostrarFormulario(Tarea tarea, Model model) {
        model.addAttribute("tarea", tarea);
        model.addAttribute("prioridades", Prioridad.values());
        model.addAttribute("accion", tarea.getId() == null ? "Crear" : "Editar");
        return "tareas/formulario";
    }
}
