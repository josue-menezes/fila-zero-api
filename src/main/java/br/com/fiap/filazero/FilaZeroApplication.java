package br.com.fiap.filazero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da API do Fila Zero.
 * O Fila Zero mostra a espera prevista nas unidades de atendimento público
 * e permite ao cidadão agendar o melhor horário.
 */
@SpringBootApplication
public class FilaZeroApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilaZeroApplication.class, args);
    }
}
