package br.com.maykon.carga.dto;

/**
 * Representação imutável de uma linha do arquivo CSV.
 *
 * <p>Utiliza {@code record} do Java 17. Cada campo corresponde a uma das seis
 * colunas esperadas no arquivo. Renomeie os campos de acordo com o domínio real
 * do projeto.</p>
 */
public record CsvLinhaDTO(
        String campo1,
        String campo2,
        String campo3,
        String campo4,
        String campo5,
        String campo6
) {
}
