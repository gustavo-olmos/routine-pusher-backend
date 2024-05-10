package com.routine.pusher.model.enums;

public enum Frequencia
{
    MINUTOS,
    HORAS,
    DIAS,
    DIAS_DA_SEMANA {
        private DiaDaSemana dia;

        public void setDia( DiaDaSemana dia ) {
            this.dia = dia;
        }

        public DiaDaSemana getDia() {
            return dia;
        }
    },
    MESES,
    ANOS;
}
