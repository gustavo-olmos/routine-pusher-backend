package com.routine.pusher.core.domain.sessao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Consome uma chamada de IA da cota da sessão, atomicamente: o incremento e a checagem do teto
     * acontecem no mesmo UPDATE, então duas requisições paralelas nunca gastam a mesma vaga —
     * ler-conferir-salvar em dois passos abriria exatamente essa corrida.
     *
     * @return 1 se havia cota (e ela foi consumida); 0 se o teto já foi atingido
     */
    @Modifying
    @Transactional
    @Query("update SessaoAnonimaEntity s set s.chamadasIa = s.chamadasIa + 1 "
            + "where s.uuid = :uuid and s.chamadasIa < :limite")
    int consumirChamadaIa( UUID uuid, int limite );
}
