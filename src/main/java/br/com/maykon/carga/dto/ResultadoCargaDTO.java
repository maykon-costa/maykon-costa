package br.com.maykon.carga.dto;

/**
 * Resposta retornada ao cliente após uma importação bem-sucedida.
 */
public record ResultadoCargaDTO(
        String nuCarga,
        int totalRegistros,
        String status,
        String mensagem
) {
    public static ResultadoCargaDTO sucesso(String nuCarga, int totalRegistros) {
        return new ResultadoCargaDTO(
                nuCarga,
                totalRegistros,
                "SUCESSO",
                "Importação concluída com sucesso. Total de registros: " + totalRegistros
        );
    }
}
