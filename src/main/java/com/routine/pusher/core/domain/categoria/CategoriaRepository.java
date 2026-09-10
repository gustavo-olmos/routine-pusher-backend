package com.routine.pusher.core.domain.categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Toda consulta é escopada pela sessão dona — mesmo desenho do {@code LembreteRepository}. Um
 * {@code findById} solto voltaria a permitir que um visitante alcançasse a categoria de outro, que
 * é justamente o que a lista por usuário existe para impedir.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long>
{
    List<CategoriaEntity> findBySessao_Uuid( UUID sessaoUuid );

    Optional<CategoriaEntity> findByIdAndSessao_Uuid( Long id, UUID sessaoUuid );

    List<CategoriaEntity> findBySessao_Id( Long sessaoId );
}
