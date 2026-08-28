package com.routine.pusher.core.domain.lembrete;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * O parâmetro de id continua Long porque essa é a chave da tabela, mas a aplicação não procura
 * lembrete por ela: as consultas de negócio entram por {@code uuid}. Os métodos herdados que
 * recebem Long ({@code findById}, {@code deleteById}) não devem ser usados fora daqui.
 */
@Repository
public interface LembreteRepository extends JpaRepository<LembreteEntity, Long>
{
    Optional<LembreteEntity> findByUuid( UUID uuid );

    Boolean existsByCategoria_Id( Long idCategoria );
}
