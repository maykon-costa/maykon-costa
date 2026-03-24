package br.com.maykon.carga.exception;

import br.com.maykon.carga.dto.ErroDTO;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converte {@link CsvInvalidoException} em uma resposta HTTP 400 com payload JSON.
 */
@Provider
public class CsvInvalidoExceptionMapper implements ExceptionMapper<CsvInvalidoException> {

    @Override
    public Response toResponse(CsvInvalidoException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroDTO(400, exception.getMessage()))
                .build();
    }
}
