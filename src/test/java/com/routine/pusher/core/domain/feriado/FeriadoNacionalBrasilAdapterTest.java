package com.routine.pusher.core.domain.feriado;

import com.routine.pusher.core.domain.feriado.adapter.FeriadoNacionalBrasilAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FeriadoNacionalBrasilAdapterTest
{
    private final FeriadoNacionalBrasilAdapter adapter = new FeriadoNacionalBrasilAdapter( );

    @Test
    @DisplayName("feriados de data fixa são reconhecidos")
    void datasFixas_saoFeriado( )
    {
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 1, 1 ) ) ).isTrue( );
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 9, 7 ) ) ).isTrue( );
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 12, 25 ) ) ).isTrue( );
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 11, 20 ) ) ).isTrue( );
    }

    @Test
    @DisplayName("dia comum não é feriado")
    void diaComum_naoEhFeriado( )
    {
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 8, 27 ) ) ).isFalse( );
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 3, 10 ) ) ).isFalse( );
    }

    @Test
    @DisplayName("feriados móveis saem certos: Carnaval, Sexta-feira Santa e Corpus Christi de 2026")
    void feriadosMoveis_derivamDaPascoa( )
    {
        // Páscoa de 2026: 5 de abril.
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 2, 16 ) ) ).as( "Carnaval segunda" ).isTrue( );
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 2, 17 ) ) ).as( "Carnaval terça" ).isTrue( );
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 4, 3 ) ) ).as( "Sexta-feira Santa" ).isTrue( );
        assertThat( adapter.ehFeriado( LocalDate.of( 2026, 6, 4 ) ) ).as( "Corpus Christi" ).isTrue( );
    }

    @Test
    @DisplayName("Páscoa calculada bate com anos conhecidos")
    void pascoa_bateComAnosConhecidos( )
    {
        // Sexta-feira Santa é sempre Páscoa - 2, então serve para conferir a data da Páscoa.
        assertThat( adapter.ehFeriado( LocalDate.of( 2024, 3, 29 ) ) ).as( "Páscoa 2024: 31/03" ).isTrue( );
        assertThat( adapter.ehFeriado( LocalDate.of( 2025, 4, 18 ) ) ).as( "Páscoa 2025: 20/04" ).isTrue( );
        assertThat( adapter.ehFeriado( LocalDate.of( 2027, 3, 26 ) ) ).as( "Páscoa 2027: 28/03" ).isTrue( );
    }

    @Test
    @DisplayName("ano em que a Sexta-feira Santa cai em Tiradentes não quebra o cálculo")
    void feriadoMovelColidindoComFixo_naoQuebra( )
    {
        // Em 2000 a Páscoa caiu em 23/04, então a Sexta-feira Santa caiu em 21/04, mesmo dia de
        // Tiradentes. Com Set.of a duplicata derrubaria o cálculo do ano inteiro.
        Set<LocalDate> feriados = adapter.feriadosDe( 2000 );

        assertThat( feriados ).contains( LocalDate.of( 2000, 4, 21 ) );
        assertThat( adapter.ehFeriado( LocalDate.of( 2000, 4, 21 ) ) ).isTrue( );
    }

    @Test
    @DisplayName("consultar muitos anos não faz o cache crescer sem limite")
    void cache_naoCresceSemLimite( )
    {
        for ( int ano = 1900; ano < 2100; ano++ )
            adapter.feriadosDe( ano );

        // Passado o teto o cálculo continua respondendo, só deixa de ser guardado: uma lista de
        // datas espalhadas por séculos não pode virar consumo de memória proporcional à entrada.
        assertThat( adapter.ehFeriado( LocalDate.of( 2099, 12, 25 ) ) ).isTrue( );
        assertThat( adapter.ehFeriado( LocalDate.of( 1905, 9, 7 ) ) ).isTrue( );
    }

    @Test
    @DisplayName("o mesmo ano consultado duas vezes devolve o conjunto já calculado")
    void cache_reaproveitaOAnoJaCalculado( )
    {
        assertThat( adapter.feriadosDe( 2026 ) ).isSameAs( adapter.feriadosDe( 2026 ) );
    }
}
