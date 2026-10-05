package com.universidad.tareas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada. Está en el paquete raíz para que @ComponentScan encuentre
 * los subpaquetes model, service y controller sin configuración adicional.
 */
@SpringBootApplication
public class TareasApplication {

    public static void main(String[] args) {
        SpringApplication.run(TareasApplication.class, args);
    }
}
