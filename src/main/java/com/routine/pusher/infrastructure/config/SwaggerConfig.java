package com.routine.pusher.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
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

    public static final String ESQUEMA_BEARER = "bearerAuth";
    public static final String ESQUEMA_BASIC = "basicAuth";
    public static final String ESQUEMA_OAUTH2 = "oauth2";

    /**
     * Os requisitos de segurança são declarados na <b>raiz</b> do documento, então valem para todos
     * os endpoints: nenhum controller precisa repetir {@code @SecurityRequirement}, e o header
     * {@code Authorization} passa a ser enviado em qualquer chamada feita pelo Swagger UI depois de
     * preencher o botão <i>Authorize</i>.
     *
     * <p>Cada {@code addSecurityItem} é uma <b>alternativa</b> (OU), não uma exigência somada.</p>
     */
    @Bean
    public OpenAPI routinePusherAPI( )
    {
        Info info = new Info( );
        Components components = new Components( );

        return new OpenAPI( )
                .info( info.title( TITULO ).description( DESCRICAO ).version("1.0") )
                .addSecurityItem( new SecurityRequirement( ).addList( ESQUEMA_BEARER ) )
                .addSecurityItem( new SecurityRequirement( ).addList( ESQUEMA_BASIC ) )
                .addSecurityItem( new SecurityRequirement( ).addList( ESQUEMA_OAUTH2 ) )
                .components( components.addSecuritySchemes( ESQUEMA_BEARER, esquemaBearer( ) )
                                       .addSecuritySchemes( ESQUEMA_BASIC, esquemaBasic( ) )
                                       .addSecuritySchemes( ESQUEMA_OAUTH2, esquemaOAuth2( ) ) );
    }

    /**
     * Bearer com o {@code id_token} do Google (JWT), validado pelo resource server.
     */
    private SecurityScheme esquemaBearer( )
    {
        return new SecurityScheme( )
                .type( SecurityScheme.Type.HTTP )
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Cole o id_token do Google. Enviado como: Authorization: Bearer <token>");
    }

    /**
     * Usuário local do perfil {@code dev}; não existe no perfil padrão.
     */
    private SecurityScheme esquemaBasic( )
    {
        return new SecurityScheme( )
                .type( SecurityScheme.Type.HTTP )
                .scheme("basic")
                .description("Usuário em memória do perfil dev (DEV_USER/DEV_PASSWORD).");
    }

    private SecurityScheme esquemaOAuth2( )
    {
        Scopes scopes = new Scopes( );
        scopes.addString("openid", "OpenID");
        scopes.addString("profile", "Profile");
        scopes.addString("email", "Email");

        OAuthFlow authCode = new OAuthFlow( );
        OAuthFlows oAuthFlows = new OAuthFlows( );
        oAuthFlows.authorizationCode(
                authCode.authorizationUrl( AUTH_URL ).tokenUrl( TOKEN_URL ).scopes( scopes ) );

        return new SecurityScheme( ).type( SecurityScheme.Type.OAUTH2 ).flows( oAuthFlows );
    }
}
