package br.com.maykon.carga.exception;

import br.com.maykon.carga.dto.ErroDTO;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converte {@link CargaException} em uma resposta HTTP 500 com payload JSON.
 */
@Provider
public class CargaExceptionMapper implements ExceptionMapper<CargaException> {

    @Override
    public Response toResponse(CargaException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroDTO(500, exception.getMessage()))
                .build();
    }
}
