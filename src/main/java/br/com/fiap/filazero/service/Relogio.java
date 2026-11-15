package br.com.fiap.filazero.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Hora oficial do sistema: sempre no fuso de São Paulo,
 * mesmo quando a API roda num servidor em outro fuso (como o Render).
 */
final class Relogio {

    static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private Relogio() {
    }

    static LocalDateTime agora() {
        return LocalDateTime.now(FUSO);
    }
}
