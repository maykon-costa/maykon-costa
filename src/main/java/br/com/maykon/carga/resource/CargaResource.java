package br.com.maykon.carga.resource;

import br.com.maykon.carga.dto.ResultadoCargaDTO;
import br.com.maykon.carga.service.CargaService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Endpoint REST para importação de arquivos CSV.
 *
 * <h2>Contrato da API</h2>
 * <pre>
 * POST /api/cargas/importar
 * Content-Type: multipart/form-data
 *
 * Parâmetros (form fields):
 *   arquivo  — arquivo CSV (obrigatório)
 *
 * Respostas:
 *   200 OK               — importação concluída; body: {@link ResultadoCargaDTO}
 *   400 Bad Request      — CSV inválido (linha malformada)
 *   500 Internal Server  — erro de persistência; compensação executada automaticamente
 * </pre>
 *
 * <h2>Formato do CSV</h2>
 * <ul>
 *   <li>Primeira linha: cabeçalho (ignorado).</li>
 *   <li>Delimitador: {@code ;} (ponto-e-vírgula).</li>
 *   <li>Colunas por linha: exatamente 6.</li>
 *   <li>Encoding: UTF-8.</li>
 * </ul>
 *
 * <h2>Exemplo de chamada (curl)</h2>
 * <pre>
 * curl -X POST http://localhost:8080/csv-carga/api/cargas/importar \
 *      -F "arquivo=@dados.csv;type=text/csv"
 * </pre>
 */
@Path("/cargas")
@Produces(MediaType.APPLICATION_JSON)
public class CargaResource {

    @Inject
    private CargaService cargaService;

    /**
     * Recebe um arquivo CSV via multipart e inicia o processo de importação em lotes.
     *
     * @param arquivoCsv stream do arquivo enviado pelo cliente
     * @return 200 com {@link ResultadoCargaDTO} em caso de sucesso
     */
    @POST
    @Path("/importar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importar(@FormParam("arquivo") InputStream arquivoCsv) {
        if (arquivoCsv == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"codigo\":400,\"mensagem\":\"O campo 'arquivo' é obrigatório.\"}")
                    .build();
        }

        ResultadoCargaDTO resultado = cargaService.importar(arquivoCsv);
        return Response.ok(resultado).build();
    }
}
