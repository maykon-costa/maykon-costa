package br.com.maykon.carga.model;

import javax.persistence.*;

/**
 * Entidade que representa um registro importado via CSV.
 *
 * <p>A tabela {@code TB_REGISTRO_IMPORTACAO} já existe no banco — nenhuma DDL é
 * executada pela aplicação. A coluna {@code NU_CARGA} identifica o lote de importação
 * e é usada pela operação de compensação (rollback lógico) caso algum lote falhe.</p>
 *
 * <p>Adapte os nomes de colunas ({@code CD_CAMPO_1} … {@code CD_CAMPO_6}) aos nomes
 * reais da tabela do projeto destino.</p>
 */
@Entity
@Table(name = "TB_REGISTRO_IMPORTACAO")
public class RegistroImportacao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_registro_importacao")
    @SequenceGenerator(
            name = "seq_registro_importacao",
            sequenceName = "SQ_REGISTRO_IMPORTACAO",
            allocationSize = 50
    )
    @Column(name = "ID_REGISTRO")
    private Long id;

    /** Identificador único do lote de carga. Gerado na requisição e compartilhado por
     *  todas as linhas do mesmo arquivo. Permite compensação atômica em caso de falha. */
    @Column(name = "NU_CARGA", nullable = false, length = 36)
    private String nuCarga;

    @Column(name = "CD_CAMPO_1", length = 100)
    private String campo1;

    @Column(name = "CD_CAMPO_2", length = 100)
    private String campo2;

    @Column(name = "CD_CAMPO_3", length = 100)
    private String campo3;

    @Column(name = "CD_CAMPO_4", length = 100)
    private String campo4;

    @Column(name = "CD_CAMPO_5", length = 100)
    private String campo5;

    @Column(name = "CD_CAMPO_6", length = 100)
    private String campo6;

    public RegistroImportacao() {
    }

    public Long getId() {
        return id;
    }

    public String getNuCarga() {
        return nuCarga;
    }

    public void setNuCarga(String nuCarga) {
        this.nuCarga = nuCarga;
    }

    public String getCampo1() {
        return campo1;
    }

    public void setCampo1(String campo1) {
        this.campo1 = campo1;
    }

    public String getCampo2() {
        return campo2;
    }

    public void setCampo2(String campo2) {
        this.campo2 = campo2;
    }

    public String getCampo3() {
        return campo3;
    }

    public void setCampo3(String campo3) {
        this.campo3 = campo3;
    }

    public String getCampo4() {
        return campo4;
    }

    public void setCampo4(String campo4) {
        this.campo4 = campo4;
    }

    public String getCampo5() {
        return campo5;
    }

    public void setCampo5(String campo5) {
        this.campo5 = campo5;
    }

    public String getCampo6() {
        return campo6;
    }

    public void setCampo6(String campo6) {
        this.campo6 = campo6;
    }
}
