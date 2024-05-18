package com.routine.pusher.service.implementation;

import com.routine.pusher.model.dto.CategoriaDTO;
import com.routine.pusher.repository.CategoriaRepository;
import com.routine.pusher.service.interfaces.CategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService
{
    private CategoriaRepository repository;

    public CategoriaServiceImpl( CategoriaRepository repository ) {
        this.repository = repository;
    }

    @Override
    public List<CategoriaDTO> listar( ) {
        return List.of( );
    }

    @Override
    public CategoriaDTO adicionar(String nome, String cor ) {
        return null;
    }

    @Override
    public CategoriaDTO editar( CategoriaDTO categoria, Long id ) {
        return null;
    }

    @Override
    public String excluir( Long id ) {
        return "";
    }
}
