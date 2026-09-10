package com.routine.pusher.core.domain.categoria;

import lombok.Getter;

/**
 * As categorias que toda sessão nova ganha de presente.
 *
 * <p>São ponto de partida, não regra: o visitante pode renomear, recolorir ou apagar as duas. Por
 * isso elas nascem como linhas comuns da lista dele — nada no modelo as marca como especiais, e
 * nenhuma operação as trata de forma diferente.</p>
 *
 * <p>Nasceram aqui, e não numa migração como as antigas categorias do demo, porque agora dependem
 * de uma sessão que só existe em tempo de execução.</p>
 */
@Getter
public enum CategoriaPadrao
{
    IMPORTANTE( "Importante", "#E53935", 1 ),
    URGENTE( "Urgente", "#FB8C00", 2 );

    private final String nome;
    private final String cor;
    private final int fatorOrdem;

    CategoriaPadrao( String nome, String cor, int fatorOrdem )
    {
        this.nome = nome;
        this.cor = cor;
        this.fatorOrdem = fatorOrdem;
    }

    public CategoriaEntity paraSessao( com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity sessao )
    {
        CategoriaEntity entidade = new CategoriaEntity( );
        entidade.setNome( nome );
        entidade.setCor( cor );
        entidade.setFatorOrdem( fatorOrdem );
        entidade.setSessao( sessao );

        return entidade;
    }
}
