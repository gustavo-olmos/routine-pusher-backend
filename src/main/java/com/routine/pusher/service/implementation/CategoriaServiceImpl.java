package com.routine.pusher.service.implementation;

import com.routine.pusher.model.Categoria;
import com.routine.pusher.repository.CategoriaRepository;
import com.routine.pusher.service.interfaces.CategoriaService;
import com.routine.pusher.util.SortInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService
{
    private SortInfo sortInfo;
    private CategoriaRepository repository;

    public CategoriaServiceImpl( SortInfo sortInfo, CategoriaRepository repository ) {
        this.sortInfo = sortInfo;
        this.repository = repository;
    }


    @Override
    public List<Categoria> listar( ) {
        return List.of( );
    }

    @Override
    public Categoria adicionar( String nome, String cor ) {
        return null;
    }

    @Override
    public Categoria editar( Categoria categoria, Long id ) {
        return null;
    }

    @Override
    public String excluir( Long id ) {
        return "";
    }
}
