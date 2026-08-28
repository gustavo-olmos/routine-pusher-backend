package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.application.service.LembreteService;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Trava o contrato de identidade do lembrete: a tabela tem chave sequencial própria, mas <b>nada na
 * aplicação consulta por ela</b>. Toda busca entra por UUID, que nasce no domínio antes do INSERT,
 * é o que a API expõe e é a chave do job no Quartz.
 */
@SpringBootTest
class IdentidadeLembreteTest
{
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private LembreteService lembreteService;
    @Autowired
    private LembreteRepository lembreteRepository;
    @Autowired
    private Scheduler scheduler;

    private Long categoriaId( String cor, int fatorOrdem )
    {
        CategoriaEntity categoria = new CategoriaEntity( );
        categoria.setNome( "Categoria " + fatorOrdem );
        categoria.setCor( cor );
        categoria.setFatorOrdem( fatorOrdem );

        return categoriaRepository.save( categoria ).getId( );
    }

    private LembreteInputDTO entrada( String titulo, Long categoriaId )
    {
        return new LembreteInputDTO( titulo, "de 5 em 5 minutos", categoriaId,
                new RecorrenciaInputDTO( null, null, null, 5, null, null, null, null ),
                new NotificacaoInputDTO( List.of( "pop-up" ), null, LocalDateTime.now( ), null, null ) );
    }


    @Test
    @DisplayName("o lembrete já nasce com UUID, antes de qualquer persistência")
    void lembrete_nasceIdentificado( )
    {
        Lembrete primeiro = new Lembrete( );
        Lembrete segundo = new Lembrete( );

        assertThat( primeiro.getUuid( ) ).isNotNull( );
        assertThat( segundo.getUuid( ) ).isNotNull( ).isNotEqualTo( primeiro.getUuid( ) );

        assertThat( primeiro.getId( ) )
                .as( "a chave da tabela só existe depois do INSERT" )
                .isNull( );
    }

    @Test
    @DisplayName("a tabela tem chave sequencial própria, invisível para quem consome a API")
    void tabela_temChaveInternaNaoExposta( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( entrada( "Ler", categoriaId( "#37474F", 204 ) ) );

        LembreteEntity entidade = lembreteRepository.findByUuid( criado.uuid( ) ).orElseThrow( );

        assertThat( entidade.getId( ) ).as( "chave interna existe e é sequencial" ).isNotNull( );
        assertThat( entidade.getUuid( ) ).isEqualTo( criado.uuid( ) );
    }

    @Test
    @DisplayName("a identidade devolvida pela API é a mesma chave que o Quartz usa para o job")
    void identidadeDaApi_eAChaveDoJob( ) throws Exception
    {
        LembreteOutputDTO criado = lembreteService.adicionar( entrada( "Alongar", categoriaId( "#1B5E20", 201 ) ) );

        assertThat( criado.uuid( ) ).isNotNull( );

        // Sem tabela de-para: a chave do trigger é literalmente o UUID exposto na saída, e não o id
        // sequencial — que uma recarga de base mudaria de lugar.
        assertThat( scheduler.checkExists( new TriggerKey( criado.uuid( ).toString( ) ) ) )
                .as( "o job precisa estar agendado sob o mesmo identificador que a API devolveu" )
                .isTrue( );
    }

    @Test
    @DisplayName("atualizar pelo UUID preserva a identidade e não cria um segundo lembrete")
    void atualizar_preservaIdentidadeSemDuplicar( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( entrada( "Beber água", categoriaId( "#0D47A1", 202 ) ) );

        long antes = lembreteRepository.count( );

        LembreteOutputDTO atualizado = lembreteService.atualizar( criado.uuid( ),
                entrada( "Beber mais água", criado.categoria( ).id( ) ) );

        assertThat( atualizado.uuid( ) ).isEqualTo( criado.uuid( ) );
        assertThat( atualizado.titulo( ) ).isEqualTo( "Beber mais água" );
        assertThat( lembreteRepository.count( ) )
                .as( "atualizar não pode inserir uma linha nova" )
                .isEqualTo( antes );
    }

    @Test
    @DisplayName("concluir e excluir também operam pelo UUID")
    void concluirEExcluir_operamPeloUuid( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( entrada( "Pausa", categoriaId( "#4A148C", 203 ) ) );

        lembreteService.concluir( criado.uuid( ) );
        assertThat( lembreteRepository.findByUuid( criado.uuid( ) ) ).isPresent( );

        lembreteService.excluir( criado.uuid( ) );
        assertThat( lembreteRepository.findByUuid( criado.uuid( ) ) ).isEmpty( );
    }

    @Test
    @DisplayName("UUID inexistente é 'não encontrado', não erro interno")
    void uuidInexistente_naoEncontrado( )
    {
        UUID inexistente = UUID.randomUUID( );

        assertThatThrownBy( () -> lembreteService.excluir( inexistente ) )
                .isInstanceOf( EntityNotFoundException.class );

        assertThatThrownBy( () -> lembreteService.concluir( inexistente ) )
                .isInstanceOf( EntityNotFoundException.class );
    }
}
