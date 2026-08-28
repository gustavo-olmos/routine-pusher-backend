package com.routine.pusher.application.service;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.application.usecase.NotificacaoUseCase;
import com.routine.pusher.application.usecase.SessaoUseCase;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.sessao.SessaoAnonima;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.dto.SessaoOutputDTO;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class SessaoService implements SessaoUseCase
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SessaoService.class );

    private final SessaoAnonimaRepository sessaoRepository;
    private final LembreteRepository lembreteRepository;
    private final SessaoAtualPort sessaoAtual;
    private final AgendadorJob agendadorJob;
    private final NotificacaoUseCase<Lembrete> notificacaoUseCase;


    @Override
    public SessaoOutputDTO atual( )
    {
        SessaoAnonimaEntity entidade = sessaoRepository.findByUuid( sessaoAtual.uuid( ) )
                .orElseThrow( ( ) -> new EntityNotFoundException( "Sessão não encontrada" ) );

        return new SessaoOutputDTO( entidade.getUuid( ), entidade.getDataCriacao( ),
                entidade.getUltimoAcesso( ).plus( SessaoAnonima.JANELA_INATIVIDADE ) );
    }

    @Override
    public void encerrar( )
    {
        sessaoRepository.findByUuid( sessaoAtual.uuid( ) ).ifPresent( this::remover );
    }

    @Override
    public int removerExpiradas( )
    {
        LocalDateTime corte = LocalDateTime.now( ).minus( SessaoAnonima.JANELA_INATIVIDADE );

        List<SessaoAnonimaEntity> expiradas = sessaoRepository.findByUltimoAcessoBefore( corte );

        int removidas = 0;
        for( SessaoAnonimaEntity sessao : expiradas ) {
            // Aba aberta esperando o disparo conta como presença, mesmo sem novas requisições —
            // o caso pomodoro: lembrete em 45 min, visitante só olhando. Varrer aqui entregaria
            // um lembrete deletado no lugar da notificação.
            if( notificacaoUseCase.possuiOuvinte( sessao.getUuid( ) ) ) continue;

            try {
                remover( sessao );
                removidas++;
            }
            catch ( Exception e ) {
                // Uma sessão emperrada não pode segurar a coleta das demais; fica para a próxima volta.
                LOGGER.error( "Falha ao remover sessão expirada {}", sessao.getUuid( ), e );
            }
        }

        if( removidas > 0 )
            LOGGER.info( "Faxina de sessões: {} removida(s)", removidas );

        return removidas;
    }

    /**
     * Desmonte na ordem inversa da criação: agendamentos primeiro (para nada disparar contra dado
     * removido), depois os lembretes (as FKs apontam para a sessão), o fluxo SSE, e a sessão por
     * último.
     */
    private void remover( SessaoAnonimaEntity sessao )
    {
        List<LembreteEntity> lembretes = lembreteRepository.findBySessao_Id( sessao.getId( ) );

        for( LembreteEntity lembrete : lembretes ) {
            try {
                agendadorJob.cancelar( lembrete.getUuid( ) );
            }
            catch ( Exception e ) {
                // Job que não existe mais no Quartz não impede a limpeza do dado — e um trigger
                // que sobreviver dispara contra lembrete inexistente, caso que o executor já
                // trata removendo o agendamento órfão.
                LOGGER.warn( "Falha ao cancelar agendamento do lembrete {}", lembrete.getUuid( ), e );
            }
        }

        lembreteRepository.deleteAll( lembretes );
        notificacaoUseCase.encerrarFluxo( sessao.getUuid( ) );
        sessaoRepository.delete( sessao );
    }
}
