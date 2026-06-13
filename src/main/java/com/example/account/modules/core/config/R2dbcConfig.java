package com.example.account.modules.core.config;


import com.example.account.modules.facturation.dto.response.ExternalResponses.AccessPortalPermissionsDTO;
import com.example.account.modules.facturation.model.entity.BonAchat;
import com.example.account.modules.facturation.model.entity.BonCommande;
import com.example.account.modules.facturation.model.entity.LigneBonAchat;
import com.example.account.modules.facturation.model.entity.LigneBonLivraison;
import com.example.account.modules.facturation.domain.model.LigneDevis;
import com.example.account.modules.facturation.domain.model.LigneFacture;
import com.example.account.modules.facturation.model.entity.Lines.LineBonCommande;
import com.example.account.modules.facturation.model.entity.Lines.LineBonReception;
import com.example.account.modules.facturation.model.entity.Lines.LineFactureFournisseur;
import com.example.account.modules.facturation.service.ExternalServices.entity.JsonToClientSaleSizeConverter;
import com.example.account.modules.facturation.service.ExternalServices.entity.JsonToPromotionConverter;
import com.example.account.modules.facturation.service.ExternalServices.entity.ObjectListToJsonConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import java.util.Collections;
import java.util.Set;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * R2DBC Configuration for reactive database access.
 * Configures PostgreSQL connection factory, transaction manager, and custom converters.
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.example.account.modules")
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.url}")
    private String r2dbcUrl;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        // Extract host, port, and database from r2dbc URL
        // Format: r2dbc:postgresql://localhost:5432/billing
        String[] parts = r2dbcUrl.replace("r2dbc:postgresql://", "").split("/");
        String[] hostPort = parts[0].split(":");
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);
        String database = parts[1];

        return new PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host(host)
                .port(port)
                .database(database)
                .username(username)
                .password(password)
                .build()
        );
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }





    @WritingConverter
public static class StringToJsonConverter implements Converter<String, Json> {
    @Override
    public Json convert(String source) {
        return Json.of(source);
    }
}

@ReadingConverter
public static class JsonToStringConverter implements Converter<Json, String> {
    @Override
    public String convert(Json source) {
        return source.asString();
    }
}


    /**
     * Custom converters for handling JSONB types and other PostgreSQL-specific types.
     */
    @Override
protected List<Object> getCustomConverters() {
    List<Object> converters = new ArrayList<>();

    // 1. Generic Writing Converter (One works for all Lists)
    converters.add(new GenericListWritingConverter());

    // 2. Specific Reading Converters (Conditional - match by generic element type)
    converters.add(new GenericListReadingConverter(com.example.account.modules.facturation.model.entity.Lines.LineFactureFournisseur.class));
    converters.add(new GenericListReadingConverter(com.example.account.modules.facturation.domain.model.LigneFactureProforma.class));
    converters.add(new GenericListReadingConverter(com.example.account.modules.facturation.domain.model.LigneNoteCredit.class));

    converters.add(new GenericListReadingConverter(LigneDevis.class));
    converters.add(new GenericListReadingConverter(LigneFacture.class));
    converters.add(new GenericListReadingConverter(LigneBonAchat.class));
    converters.add(new GenericListReadingConverter(LigneBonLivraison.class));
    converters.add(new GenericListReadingConverter(LineBonCommande.class));
    converters.add(new GenericListReadingConverter(LineBonReception.class));
    
      converters.add(new GenericListReadingConverter(com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse.ClientSaleSize.class));
    converters.add(new GenericListReadingConverter(com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse.SaleSizePromotion.class));
    converters.add(new ObjectListToJsonConverter());
    converters.add(new JsonToClientSaleSizeConverter());
    converters.add(new JsonToPromotionConverter());
       converters.add(new GenericListReadingConverter(AccessPortalPermissionsDTO.class));



    return converters;
}

private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

/**
 * Truly Generic Writing Converter
 */
@WritingConverter
public static class GenericListWritingConverter implements Converter<List<?>, Json> {
    @Override
    public Json convert(List<?> source) {
        try {
            return Json.of(mapper.writeValueAsString(source));
        } catch (Exception e) {
            return Json.of("[]");
        }
    }
}

/**
 * Type-Safe Generic Reading Converter 
 * This solves the LinkedHashMap issue by passing the specific Class type.
 */
@ReadingConverter
public static class GenericListReadingConverter implements ConditionalGenericConverter {
    private final Class<?> elementType;

    public GenericListReadingConverter(Class<?> elementType) {
        this.elementType = elementType;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Json.class, List.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (sourceType == null || targetType == null) {
            return false;
        }
        if (!Json.class.isAssignableFrom(sourceType.getType())) {
            return false;
        }
        TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
        if (elementDesc == null) {
            return false;
        }
        return elementDesc.getType().equals(this.elementType);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Json json = (Json) source;
        try {
            return mapper.readValue(json.asString(),
                    mapper.getTypeFactory().constructCollectionType(List.class, this.elementType));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
}
