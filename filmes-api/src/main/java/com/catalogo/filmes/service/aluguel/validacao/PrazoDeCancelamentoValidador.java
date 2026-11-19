package com.catalogo.filmes.service.aluguel.validacao;

import com.catalogo.filmes.config.AluguelProperties;
import com.catalogo.filmes.service.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Order(3)
@RequiredArgsConstructor
public class PrazoDeCancelamentoValidador implements ValidadorCancelamento {

    private final AluguelProperties regras;

    @Override
    public void validar(PedidoCancelamento pedido) {
        if (!pedido.aluguel().podeSerCanceladoEm(pedido.agora(), regras.prazoCancelamento())) {
            throw new RegraNegocioException("O prazo para cancelar (" + descrever(regras.prazoCancelamento())
                    + " após o aluguel) já terminou.");
        }
    }

    static String descrever(Duration duracao) {
        long minutos = duracao.toMinutes();
        if (minutos % 60 == 0) {
            long horas = minutos / 60;
            return horas == 1 ? "1 hora" : horas + " horas";
        }
        return minutos == 1 ? "1 minuto" : minutos + " minutos";
    }
}
