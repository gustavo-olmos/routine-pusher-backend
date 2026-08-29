package com.routine.pusher.application.external.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Cartão de visita da API na raiz do domínio.
 *
 * <p>Existe por três motivos práticos, nenhum deles decorativo: plataformas de container checam a
 * saúde em {@code /} por padrão, e um 404 ali faz a plataforma concluir que a instância está doente
 * e reiniciá-la em laço; o {@code defaultSuccessUrl("/")} do login do Google precisava de um destino;
 * e quem abrir o domínio da API no navegador merece saber o que é aquilo em vez de ver um erro.</p>
 *
 * <p>O health check de verdade continua sendo {@code /actuator/health}, que consulta banco e demais
 * dependências — este endpoint só prova que a aplicação responde.</p>
 */
@RestController
@Tag(name = "Raiz", description = "Identificação da API")
public class RaizController
{
    private final String nome;

    public RaizController( @Value("${spring.application.name:Routine Pusher}") String nome )
    {
        this.nome = nome;
    }

    @GetMapping("/")
    @Operation(summary = "Identifica a API e aponta o caminho da documentação")
    public ResponseEntity<Map<String, String>> raiz( )
    {
        return ResponseEntity.ok( Map.of(
                "aplicacao", nome,
                "status", "no ar",
                "documentacao", "/swagger-ui.html",
                "saude", "/actuator/health" ) );
    }
}
