package br.com.maykon.carga.service;

import br.com.maykon.carga.dto.CsvLinhaDTO;
import br.com.maykon.carga.dto.ResultadoCargaDTO;
import br.com.maykon.carga.exception.CargaException;
import br.com.maykon.carga.exception.CsvInvalidoException;
import br.com.maykon.carga.util.CsvParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CargaServiceTest {

    @Mock
    private CargaLoteService cargaLoteService;

    @Mock
    private CsvParser csvParser;

    @InjectMocks
    private CargaService cargaService;

    private static final CsvLinhaDTO DTO_VALIDO =
            new CsvLinhaDTO("v1", "v2", "v3", "v4", "v5", "v6");

    @BeforeEach
    void setUp() {
        // Por padrão, o parser retorna um DTO válido para qualquer linha
        when(csvParser.parsear(anyString(), anyInt())).thenReturn(DTO_VALIDO);
    }

    // -----------------------------------------------------------------------
    // Casos de sucesso
    // -----------------------------------------------------------------------

    @Test
    void deveRetornarResultadoComTotalCorreto_quandoCsvComUmaLinha() {
        InputStream csv = toStream("cabecalho\nlinha1");

        ResultadoCargaDTO resultado = cargaService.importar(csv);

        assertEquals(1, resultado.totalRegistros());
        assertEquals("SUCESSO", resultado.status());
        assertNotNull(resultado.nuCarga());
        verify(cargaLoteService, times(1)).processarLote(anyList(), anyString());
        verify(cargaLoteService, never()).reverterCarga(anyString());
    }

    @Test
    void deveProcessarEmLotesCorretos_quandoCsvComMaisLinhasQueOTamanhoDoLote() {
        // Gera um CSV com TAMANHO_LOTE + 1 linhas de dados (mais o cabeçalho)
        String conteudo = gerarCsv(CargaService.TAMANHO_LOTE + 1);
        InputStream csv = toStream(conteudo);

        ResultadoCargaDTO resultado = cargaService.importar(csv);

        assertEquals(CargaService.TAMANHO_LOTE + 1, resultado.totalRegistros());
        // Deve ter sido chamado 2 vezes: um lote cheio + um lote com 1 registro
        verify(cargaLoteService, times(2)).processarLote(anyList(), anyString());
        verify(cargaLoteService, never()).reverterCarga(anyString());
    }

    @Test
    void devePropagaOMesmoNuCargaParaTodosOsLotes() {
        String conteudo = gerarCsv(CargaService.TAMANHO_LOTE + 1);
        InputStream csv = toStream(conteudo);

        cargaService.importar(csv);

        ArgumentCaptor<String> nuCargaCaptor = ArgumentCaptor.forClass(String.class);
        verify(cargaLoteService, times(2))
                .processarLote(anyList(), nuCargaCaptor.capture());

        List<String> valores = nuCargaCaptor.getAllValues();
        // Os dois lotes devem ter o mesmo nuCarga
        assertEquals(valores.get(0), valores.get(1));
    }

    @Test
    void deveIgnorarLinhasBrancas_quandoCsvContemLinhasVazias() {
        InputStream csv = toStream("cabecalho\nlinha1\n\n\nlinha2");

        ResultadoCargaDTO resultado = cargaService.importar(csv);

        // Linhas em branco são ignoradas pelo serviço
        assertEquals(2, resultado.totalRegistros());
    }

    // -----------------------------------------------------------------------
    // Casos de falha — compensação
    // -----------------------------------------------------------------------

    @Test
    void deveChamarCompensacao_quandoErroNoPrimeiroLote() {
        InputStream csv = toStream("cabecalho\nlinha1");
        doThrow(new RuntimeException("Erro de BD")).when(cargaLoteService)
                .processarLote(anyList(), anyString());

        assertThrows(CargaException.class, () -> cargaService.importar(csv));

        verify(cargaLoteService, times(1)).reverterCarga(anyString());
    }

    @Test
    void deveChamarCompensacaoUmaVez_quandoErroNoSegundoLote() {
        String conteudo = gerarCsv(CargaService.TAMANHO_LOTE + 1);
        InputStream csv = toStream(conteudo);

        // O primeiro lote passa, o segundo falha
        doNothing().doThrow(new RuntimeException("timeout"))
                .when(cargaLoteService).processarLote(anyList(), anyString());

        assertThrows(CargaException.class, () -> cargaService.importar(csv));

        verify(cargaLoteService, times(1)).reverterCarga(anyString());
    }

    @Test
    void deveLancarCsvInvalidoException_quandoParserFalha() {
        InputStream csv = toStream("cabecalho\nlinhaInvalida");
        when(csvParser.parsear(anyString(), anyInt()))
                .thenThrow(new CsvInvalidoException("Linha inválida", 2));

        assertThrows(CsvInvalidoException.class, () -> cargaService.importar(csv));

        // Compensação ainda deve ser chamada (pode haver lotes anteriores inseridos)
        verify(cargaLoteService, times(1)).reverterCarga(anyString());
    }

    @Test
    void deveLancarCargaException_quandoArquivoVazio() {
        InputStream csv = toStream("");

        assertThrows(CargaException.class, () -> cargaService.importar(csv));
        verify(cargaLoteService, never()).processarLote(anyList(), anyString());
    }

    @Test
    void deveContinuarMesmoSeCompensacaoFalhar() {
        InputStream csv = toStream("cabecalho\nlinha1");
        doThrow(new RuntimeException("Erro de BD")).when(cargaLoteService)
                .processarLote(anyList(), anyString());
        doThrow(new RuntimeException("Erro na compensação")).when(cargaLoteService)
                .reverterCarga(anyString());

        // Mesmo que a compensação falhe, o serviço deve lançar CargaException original
        assertThrows(CargaException.class, () -> cargaService.importar(csv));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private InputStream toStream(String conteudo) {
        return new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8));
    }

    private String gerarCsv(int quantidadeLinhasDados) {
        StringBuilder sb = new StringBuilder("col1;col2;col3;col4;col5;col6\n");
        for (int i = 0; i < quantidadeLinhasDados; i++) {
            sb.append("v1;v2;v3;v4;v5;v6\n");
        }
        return sb.toString();
    }
}
