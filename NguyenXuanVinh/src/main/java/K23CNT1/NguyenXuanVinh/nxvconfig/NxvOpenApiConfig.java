package K23CNT1.NguyenXuanVinh.nxvconfig;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NxvOpenApiConfig {

    @Bean
    public OpenAPI taskTaleOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("TaskTale / LifeRPG API - Äá»“ Ãn Tá»‘t Nghiá»‡p")
                        .version("2.2")
                        .description("Há»‡ thá»‘ng API Gamification Quáº£n LÃ½ Ká»· Luáº­t & NÄƒng Suáº¥t (122 Chá»©c nÄƒng - 9 Modules)"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}