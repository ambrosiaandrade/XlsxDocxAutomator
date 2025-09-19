package com.ambrosiaandrade.exceldocxautomator.model;

import java.io.Serializable;
import java.util.List;

/**
 * Representa a Unidade Concedente (UC), cujos dados são lidos do Excel.
 */
public record UnidadeConcedente(
        String nome,
        String cnpj,
        String telefone,
        String localEstagio,
        String endereco,
        String pontoReferencia,
        String numero,
        String complemento,
        String cep,
        String bairro,
        String cidade,
        String estado,
        String representanteLegal,
        String cargoRepresentante
) implements Serializable {
    public UnidadeConcedente {
        // Normaliza o CNPJ no construtor
        if (cnpj != null) {
            cnpj = cnpj.replaceAll("[^a-zA-Z0-9]", "");
        }
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof UnidadeConcedente uc) &&
                this.cnpj != null && this.cnpj.equals(uc.cnpj);
    }

    @Override
    public int hashCode() {
        return cnpj == null ? 0 : cnpj.hashCode();
    }
}

