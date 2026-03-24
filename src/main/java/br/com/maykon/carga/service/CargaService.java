package br.com.maykon.carga.service;

import br.com.maykon.carga.dto.CsvLinhaDTO;
import br.com.maykon.carga.dto.ResultadoCargaDTO;
import br.com.maykon.carga.exception.CargaException;
import br.com.maykon.carga.exception.CsvInvalidoException;
import br.com.maykon.carga.util.CsvParser;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orquestrador da importação de arquivos CSV.
 *
 * <h2>Estratégia de Transações</h2>
 * <p>Este serviço opera com {@link TransactionAttributeType#NEVER}: ele <em>não</em>
 * participa de nenhuma transação JTA. A responsabilidade transacional fica
 * inteiramente em {@link CargaLoteService}, que usa {@code REQUIRES_NEW} para criar
 * uma transação curta e independente por lote. Isso evita:</p>
 * <ul>
 *   <li>Transações de longa duração que bloqueiam recursos do banco.</li>
 *   <li>Timeouts de transação ao processar ≈100 mil linhas.</li>
 * </ul>
 *
 * <h2>Garantia de Atomicidade (Padrão de Compensação)</h2>
 * <p>Todos os registros de uma carga compartilham o mesmo {@code nuCarga} (UUID gerado
 * no início da requisição). Se qualquer lote falhar, o método
 * {@link CargaLoteService#reverterCarga(String)} é invocado para excluir atomicamente
 * <em>todos</em> os registros já comitados para aquele {@code nuCarga}. O cliente
 * recebe um erro e o banco fica em estado consistente.</p>
 *
 * <h2>Processamento em Stream</h2>
 * <p>O arquivo é lido linha a linha via {@link BufferedReader}, garantindo que o heap
 * da JVM não seja saturado por arquivos grandes. O lote acumula até
 * {@link #TAMANHO_LOTE} linhas antes de ser enviado ao banco.</p>
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class CargaService {

    private static final Logger LOG = Logger.getLogger(CargaService.class.getName());

    /**
     * Quantidade de linhas por lote (transação). Ajuste conforme o perfil de carga
     * e as configurações de timeout de transação do servidor.
     */
    static final int TAMANHO_LOTE = 1_000;

    @Inject
    private CargaLoteService cargaLoteService;

    @Inject
    private CsvParser csvParser;

    /**
     * Processa um arquivo CSV enviado como {@link InputStream}.
     *
     * <p>A primeira linha é tratada como cabeçalho e ignorada.</p>
     *
     * @param arquivoCsv stream do arquivo recebido na requisição multipart
     * @return DTO com o {@code nuCarga} gerado e o total de registros importados
     * @throws CsvInvalidoException se alguma linha do CSV for malformada
     * @throws CargaException       se ocorrer erro de persistência; nesse caso todos os
     *                              registros já inseridos são removidos via compensação
     */
    public ResultadoCargaDTO importar(InputStream arquivoCsv) {
        String nuCarga = UUID.randomUUID().toString();
        LOG.info("Iniciando importação. nuCarga=" + nuCarga);

        int totalLinhas = 0;
        try {
            totalLinhas = processarStream(arquivoCsv, nuCarga);
        } catch (CsvInvalidoException e) {
            // Erro de formato: não há dados inseridos a compensar (falha na validação)
            LOG.warning("Arquivo CSV inválido. nuCarga=" + nuCarga
                    + " | " + e.getMessage());
            compensar(nuCarga);
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Erro durante a importação. Iniciando compensação. nuCarga=" + nuCarga, e);
            compensar(nuCarga);
            throw new CargaException("Erro ao processar a importação: " + e.getMessage(), e);
        }

        LOG.info("Importação concluída. nuCarga=" + nuCarga
                + " | totalRegistros=" + totalLinhas);
        return ResultadoCargaDTO.sucesso(nuCarga, totalLinhas);
    }

    // -------------------------------------------------------------------------
    // Métodos privados
    // -------------------------------------------------------------------------

    private int processarStream(InputStream arquivoCsv, String nuCarga) throws IOException {
        int totalLinhas = 0;
        List<CsvLinhaDTO> lote = new ArrayList<>(TAMANHO_LOTE);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(arquivoCsv, StandardCharsets.UTF_8))) {

            // Ignora o cabeçalho
            String linha = reader.readLine();
            if (linha == null) {
                throw new CargaException("O arquivo CSV está vazio.");
            }

            int numeroLinha = 1;
            while ((linha = reader.readLine()) != null) {
                numeroLinha++;

                if (linha.isBlank()) {
                    continue;
                }

                CsvLinhaDTO dto = csvParser.parsear(linha, numeroLinha);
                lote.add(dto);

                if (lote.size() >= TAMANHO_LOTE) {
                    cargaLoteService.processarLote(List.copyOf(lote), nuCarga);
                    totalLinhas += lote.size();
                    lote.clear();
                }
            }

            // Processa o lote restante (último lote, geralmente < TAMANHO_LOTE)
            if (!lote.isEmpty()) {
                cargaLoteService.processarLote(List.copyOf(lote), nuCarga);
                totalLinhas += lote.size();
            }
        }

        return totalLinhas;
    }

    private void compensar(String nuCarga) {
        try {
            cargaLoteService.reverterCarga(nuCarga);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE,
                    "FALHA CRÍTICA: compensação não concluída. Verifique o banco para nuCarga="
                            + nuCarga, ex);
        }
    }
}
