package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.SessaoUseCase;
import com.routine.pusher.core.domain.sessao.dto.SessaoOutputDTO;
import com.routine.pusher.infrastructure.web.SessaoAnonimaFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/sessao")
@Tag(name = "Sessão", description = "Sessão anônima do visitante: consulta e encerramento")
public class SessaoController
{
    private final SessaoUseCase useCase;


    @GetMapping
    @Operation(summary = "Consulta a sessão atual (cria uma se ainda não houver)")
    public ResponseEntity<SessaoOutputDTO> atual( )
    {
        return ResponseEntity.ok( useCase.atual( ) );
    }

    /**
     * O "esquecer-me": remove a sessão, os lembretes e os agendamentos dela, e expira o cookie no
     * navegador. A requisição seguinte do mesmo visitante começa do zero, com identidade nova.
     */
    @DeleteMapping
    @Operation(summary = "Encerra a sessão atual e remove tudo que pertence a ela")
    public ResponseEntity<Void> encerrar( )
    {
        useCase.encerrar( );

        ResponseCookie expirado = ResponseCookie.from( SessaoAnonimaFilter.COOKIE_SESSAO, "" )
                .httpOnly( true )
                .path( "/" )
                .maxAge( 0 )
                .sameSite( "Lax" )
                .build( );

        return ResponseEntity.noContent( )
                .header( HttpHeaders.SET_COOKIE, expirado.toString( ) )
                .build( );
    }
}
