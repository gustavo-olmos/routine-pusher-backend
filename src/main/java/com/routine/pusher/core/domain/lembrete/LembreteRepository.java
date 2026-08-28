package com.routine.pusher.core.domain.lembrete;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * O parâmetro de id continua Long porque essa é a chave da tabela, mas a aplicação não procura
 * lembrete por ela: as consultas de negócio entram por {@code uuid}. Os métodos herdados que
 * recebem Long ({@code findById}, {@code deleteById}) não devem ser usados fora daqui.
 *
 * <p>As variantes com sessão são o caminho dos endpoints: o visitante só enxerga (e só altera) o
 * que a sessão dele criou. {@link #findByUuid} sem sessão fica para uso interno — o executor de
 * jobs, que age em nome do sistema.</p>
 */
@Repository
public interface LembreteRepository extends JpaRepository<LembreteEntity, Long>
{
    Optional<LembreteEntity> findByUuid( UUID uuid );

    Optional<LembreteEntity> findByUuidAndSessao_Uuid( UUID uuid, UUID sessaoUuid );

    List<LembreteEntity> findBySessao_Uuid( UUID sessaoUuid );

    List<LembreteEntity> findBySessao_Id( Long sessaoId );

    long countBySessao_Uuid( UUID sessaoUuid );

    Boolean existsByCategoria_Id( Long idCategoria );
}
