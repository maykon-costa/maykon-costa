package br.com.maykon.carga.exception;

/**
 * Exceção de negócio genérica do processo de carga.
 * Usada para erros que impedem a conclusão da importação e acionam a compensação.
 */
public class CargaException extends RuntimeException {

    public CargaException(String mensagem) {
        super(mensagem);
    }

    public CargaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
