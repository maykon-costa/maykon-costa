package br.com.maykon.carga.service;

import br.com.maykon.carga.dto.CsvLinhaDTO;
import br.com.maykon.carga.model.RegistroImportacao;
import br.com.maykon.carga.repository.CargaRepository;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * EJB responsável por executar cada lote dentro de uma <strong>transação própria</strong>
 * e por realizar a <strong>compensação</strong> quando um lote falha.
 *
 * <h2>Por que {@code REQUIRES_NEW}?</h2>
 * <p>Com {@link TransactionAttributeType#REQUIRES_NEW} cada chamada a
 * {@link #processarLote} abre, comita ou reverte sua própria transação JTA,
 * independentemente de qualquer transação externa. Isso garante:</p>
 * <ul>
 *   <li>Transações curtas, sem lock prolongado no banco.</li>
 *   <li>O {@link CargaService} (orquestrador) pode operar sem transação ativa
 *       ({@code NEVER}), evitando que um erro em um lote desfaça tudo via exceção
 *       propagada — a compensação é feita de forma explícita e atômica.</li>
 * </ul>
 *
 * <h2>Compensação</h2>
 * <p>Se qualquer lote falhar, o {@link CargaService} chama {@link #reverterCarga},
 * que exclui atomicamente <em>todos</em> os registros do lote de carga identificados
 * por {@code nuCarga}, desfazendo os lotes anteriores que já foram comitados.</p>
 */
@Stateless
public class CargaLoteService {

    private static final Logger LOG = Logger.getLogger(CargaLoteService.class.getName());

    @Inject
    private CargaRepository cargaRepository;

    /**
     * Persiste um lote de registros dentro de uma transação nova e independente.
     *
     * @param lote    lista de DTOs a persistir (tamanho definido em {@link CargaService})
     * @param nuCarga identificador do lote de importação
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processarLote(List<CsvLinhaDTO> lote, String nuCarga) {
        LOG.fine(() -> "Processando lote com " + lote.size()
                + " registros. nuCarga=" + nuCarga);

        for (CsvLinhaDTO dto : lote) {
            RegistroImportacao registro = toEntity(dto, nuCarga);
            cargaRepository.salvar(registro);
        }

        cargaRepository.flushELimparContexto();

        LOG.fine(() -> "Lote concluído. nuCarga=" + nuCarga);
    }

    /**
     * Exclui todos os registros do lote identificado por {@code nuCarga}.
     *
     * <p>Executado em transação nova para garantir atomicidade da compensação mesmo
     * que o contexto chamador esteja em estado inconsistente.</p>
     *
     * @param nuCarga identificador do lote a ser revertido
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void reverterCarga(String nuCarga) {
        LOG.warning("Iniciando compensação (rollback lógico). nuCarga=" + nuCarga);
        int excluidos = cargaRepository.deletarPorNuCarga(nuCarga);
        LOG.warning("Compensação concluída. Registros excluídos: " + excluidos
                + ". nuCarga=" + nuCarga);
    }

    private RegistroImportacao toEntity(CsvLinhaDTO dto, String nuCarga) {
        RegistroImportacao entity = new RegistroImportacao();
        entity.setNuCarga(nuCarga);
        entity.setCampo1(dto.campo1());
        entity.setCampo2(dto.campo2());
        entity.setCampo3(dto.campo3());
        entity.setCampo4(dto.campo4());
        entity.setCampo5(dto.campo5());
        entity.setCampo6(dto.campo6());
        return entity;
    }
}
