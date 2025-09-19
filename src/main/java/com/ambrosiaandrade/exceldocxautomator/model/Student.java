package com.ambrosiaandrade.exceldocxautomator.model;

import java.io.Serializable;

public record Student (String nome, String matricula, String curso, String ucCnpj) implements Serializable {
}
