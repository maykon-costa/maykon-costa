package br.com.maykon.carga.exception;

/**
 * Exceção lançada quando o arquivo CSV possui formato inválido
 * (número incorreto de colunas, delimitador ausente, etc.).
 */
public class CsvInvalidoException extends RuntimeException {

    private final int numeroLinha;

    public CsvInvalidoException(String mensagem, int numeroLinha) {
        super(mensagem);
        this.numeroLinha = numeroLinha;
    }

    public int getNumeroLinha() {
        return numeroLinha;
    }
}
