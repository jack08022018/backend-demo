package com.jpa.utils;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.LinkedHashMap;

@Component
@RequiredArgsConstructor
public class TupleConverter {
    private final JsonMapper customJsonMapper;

    public <T> T convert(Tuple tuple, Class<T> clazz) {
        var map = new LinkedHashMap<String, Object>();
        for (TupleElement<?> e : tuple.getElements()) {
            var value = tuple.get(e);
            if(value instanceof Timestamp s) {
                value = s.toLocalDateTime();
            }
            map.put(e.getAlias(), value);
        }
        return customJsonMapper.convertValue(map, clazz);
    }
}
