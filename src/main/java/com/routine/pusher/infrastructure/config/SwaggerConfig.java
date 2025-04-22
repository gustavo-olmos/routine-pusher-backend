package com.routine.pusher.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig
{
    @Bean
    public OpenAPI myOpenAPI( )
    {
        return new OpenAPI( ).info(
                new Info( ).title( "Routine Pusher API" )
                         .description( "Documentação dos endpoints da API Routine Pusher" )
                         .version( "1.0" ) );
    }
}
