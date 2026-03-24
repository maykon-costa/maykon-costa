package br.com.maykon.carga.dto;

/**
 * Payload de erro retornado ao cliente nas respostas HTTP de falha.
 */
public record ErroDTO(
        int codigo,
        String mensagem
) {
}
