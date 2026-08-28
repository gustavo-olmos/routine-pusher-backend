package com.routine.pusher.core.domain.sessao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Como em {@code LembreteRepository}: a chave da tabela é Long, mas as consultas de negócio entram
 * por {@code uuid} — o Long não atravessa a camada de persistência.
 */
@Repository
public interface SessaoAnonimaRepository extends JpaRepository<SessaoAnonimaEntity, Long>
{
    Optional<SessaoAnonimaEntity> findByUuid( UUID uuid );

    /** Candidatas à faxina: paradas desde antes do corte. */
    List<SessaoAnonimaEntity> findByUltimoAcessoBefore( LocalDateTime corte );
}
