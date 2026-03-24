package br.com.maykon.carga.util;

import br.com.maykon.carga.dto.CsvLinhaDTO;
import br.com.maykon.carga.exception.CsvInvalidoException;

import javax.enterprise.context.ApplicationScoped;

/**
 * Responsável por fazer o parse de uma linha do arquivo CSV e retornar o DTO correspondente.
 *
 * <p>O delimitador padrão é ponto-e-vírgula ({@code ;}). Altere {@link #DELIMITADOR} se o
 * arquivo utilizar vírgula ou tabulação.</p>
 *
 * <p>A linha de cabeçalho <strong>não</strong> é processada por esta classe — o serviço
 * orquestrador é responsável por ignorar a primeira linha.</p>
 */
@ApplicationScoped
public class CsvParser {

    static final int NUMERO_COLUNAS = 6;
    static final String DELIMITADOR = ";";

    /**
     * Converte uma linha de texto em um {@link CsvLinhaDTO}.
     *
     * @param linha       conteúdo bruto da linha
     * @param numeroLinha número da linha no arquivo (para mensagens de erro)
     * @return DTO com os seis campos da linha
     * @throws CsvInvalidoException se a linha não contiver exatamente seis colunas ou
     *                              se algum campo obrigatório estiver vazio
     */
    public CsvLinhaDTO parsear(String linha, int numeroLinha) {
        if (linha == null || linha.isBlank()) {
            throw new CsvInvalidoException(
                    "Linha " + numeroLinha + " está vazia.", numeroLinha);
        }

        String[] campos = linha.split(DELIMITADOR, -1);

        if (campos.length != NUMERO_COLUNAS) {
            throw new CsvInvalidoException(
                    "Linha " + numeroLinha + ": esperado " + NUMERO_COLUNAS
                            + " colunas, encontrado " + campos.length + ".",
                    numeroLinha);
        }

        return new CsvLinhaDTO(
                trim(campos[0]),
                trim(campos[1]),
                trim(campos[2]),
                trim(campos[3]),
                trim(campos[4]),
                trim(campos[5])
        );
    }

    private String trim(String valor) {
        return valor == null ? null : valor.trim();
    }
}
