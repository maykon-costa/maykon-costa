package br.com.maykon.carga.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Ponto de entrada JAX-RS. Todos os recursos REST ficam sob o prefixo /api.
 */
@ApplicationPath("/api")
public class CargaApplication extends Application {
}
