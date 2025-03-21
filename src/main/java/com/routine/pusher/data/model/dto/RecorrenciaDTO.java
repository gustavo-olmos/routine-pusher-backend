package com.routine.pusher.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class RecorrenciaDTO {
    private List<String> diasDaSemana;
    private String intervaloCronExp;
    private LocalDateTime validade;
    private int quantidade;
}
