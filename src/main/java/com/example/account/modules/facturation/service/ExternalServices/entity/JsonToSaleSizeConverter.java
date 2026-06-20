package com.example.account.modules.facturation.service.ExternalServices.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;
import java.util.List;

@ReadingConverter
public class JsonToSaleSizeConverter implements Converter<Json, List<SaleSize>> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<SaleSize> convert(Json source) {
        try {
            return mapper.readValue(source.asString(), new TypeReference<List<SaleSize>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to List<SaleSize>", e);
        }
    }
}
