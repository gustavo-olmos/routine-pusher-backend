package com.routine.pusher.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig
{
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    public static final String TITULO = "Routine Pusher API";
    public static final String DESCRICAO = "Documentação dos endpoints da API Routine Pusher";

    @Bean
    public OpenAPI routinePusherAPI( )
    {
        Info info = new Info( );
//        Components components = new Components( );
//        OAuthFlow authCode = new OAuthFlow( );
//        SecurityScheme scheme = new SecurityScheme( );
//
//        Scopes scopes = new Scopes( );
//        scopes.addString("openid", "OpenID");
//        scopes.addString("profile", "Profile");
//        scopes.addString("email", "Email");
//
//        OAuthFlows oAuthFlows = new OAuthFlows( );
//        oAuthFlows.authorizationCode(
//                authCode.authorizationUrl( AUTH_URL ).tokenUrl( TOKEN_URL ).scopes( scopes ) );

        return new OpenAPI( )
                .info( info.title( TITULO ).description( DESCRICAO ).version("1.0") );
//                .components( components.addSecuritySchemes(
//                    "oauth2", scheme.type( SecurityScheme.Type.OAUTH2 ).flows( oAuthFlows ) ) );
    }
}
