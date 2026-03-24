package br.com.maykon.carga.util;

import br.com.maykon.carga.dto.CsvLinhaDTO;
import br.com.maykon.carga.exception.CsvInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private CsvParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvParser();
    }

    @Test
    void deveRetornarDtoComSeisCampos_quandoLinhaValida() {
        String linha = "valor1;valor2;valor3;valor4;valor5;valor6";
        CsvLinhaDTO dto = parser.parsear(linha, 1);

        assertEquals("valor1", dto.campo1());
        assertEquals("valor2", dto.campo2());
        assertEquals("valor3", dto.campo3());
        assertEquals("valor4", dto.campo4());
        assertEquals("valor5", dto.campo5());
        assertEquals("valor6", dto.campo6());
    }

    @Test
    void deveFazerTrimNosValores() {
        String linha = "  a  ;  b  ;  c  ;  d  ;  e  ;  f  ";
        CsvLinhaDTO dto = parser.parsear(linha, 1);

        assertEquals("a", dto.campo1());
        assertEquals("f", dto.campo6());
    }

    @Test
    void deveLancarExcecao_quandoMenosDeSeisColunasEncontradas() {
        String linha = "campo1;campo2;campo3";
        CsvInvalidoException ex = assertThrows(
                CsvInvalidoException.class,
                () -> parser.parsear(linha, 5));

        assertEquals(5, ex.getNumeroLinha());
        assertTrue(ex.getMessage().contains("esperado 6 colunas"));
        assertTrue(ex.getMessage().contains("encontrado 3"));
    }

    @Test
    void deveLancarExcecao_quandoMaisDeSeisColunasEncontradas() {
        String linha = "c1;c2;c3;c4;c5;c6;c7";
        CsvInvalidoException ex = assertThrows(
                CsvInvalidoException.class,
                () -> parser.parsear(linha, 10));

        assertEquals(10, ex.getNumeroLinha());
        assertTrue(ex.getMessage().contains("encontrado 7"));
    }

    @Test
    void deveLancarExcecao_quandoLinhaVazia() {
        CsvInvalidoException ex = assertThrows(
                CsvInvalidoException.class,
                () -> parser.parsear("", 3));

        assertEquals(3, ex.getNumeroLinha());
    }

    @Test
    void deveLancarExcecao_quandoLinhaApenasBrancos() {
        CsvInvalidoException ex = assertThrows(
                CsvInvalidoException.class,
                () -> parser.parsear("   ", 7));

        assertEquals(7, ex.getNumeroLinha());
    }

    @Test
    void deveLancarExcecao_quandoLinhaNull() {
        CsvInvalidoException ex = assertThrows(
                CsvInvalidoException.class,
                () -> parser.parsear(null, 2));

        assertEquals(2, ex.getNumeroLinha());
    }

    @Test
    void deveAceitarCamposVaziosEntreDelimitadores() {
        // Campos opcionais podem ser vazios — a aplicação pode validá-los depois
        String linha = "v1;;v3;;v5;v6";
        CsvLinhaDTO dto = parser.parsear(linha, 1);

        assertEquals("v1", dto.campo1());
        assertEquals("", dto.campo2());
        assertEquals("v3", dto.campo3());
        assertEquals("", dto.campo4());
    }

    @Test
    void deveIdentificarNumeroDaLinhaCorretamente() {
        String linha = "a;b;c";
        CsvInvalidoException ex = assertThrows(
                CsvInvalidoException.class,
                () -> parser.parsear(linha, 42));

        assertTrue(ex.getMessage().contains("42"));
    }
}
