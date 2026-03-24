package br.com.maykon.carga.repository;

import br.com.maykon.carga.model.RegistroImportacao;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Repositório JPA responsável pelas operações de persistência de {@link RegistroImportacao}.
 *
 * <p>O controle transacional <strong>não</strong> é feito aqui — é responsabilidade do
 * {@code CargaLoteService} garantir que cada chamada ocorra dentro de uma transação
 * JTA ativa.</p>
 */
@ApplicationScoped
public class CargaRepository {

    @PersistenceContext(unitName = "cargaPU")
    private EntityManager em;

    /**
     * Persiste um único registro. O flush é feito pelo serviço ao final do lote para
     * liberar a memória do contexto de persistência de forma controlada.
     */
    public void salvar(RegistroImportacao registro) {
        em.persist(registro);
    }

    /**
     * Remove todos os registros de um determinado lote.
     * Chamado pela operação de compensação quando algum lote falha.
     *
     * @param nuCarga identificador do lote a ser removido
     * @return quantidade de registros excluídos
     */
    public int deletarPorNuCarga(String nuCarga) {
        return em.createQuery(
                        "DELETE FROM RegistroImportacao r WHERE r.nuCarga = :nuCarga")
                .setParameter("nuCarga", nuCarga)
                .executeUpdate();
    }

    /**
     * Executa flush e clear no {@link EntityManager} para liberar o cache de primeiro
     * nível após a persistência de um lote, evitando estouro de memória em importações
     * de grande volume.
     */
    public void flushELimparContexto() {
        em.flush();
        em.clear();
    }
}
