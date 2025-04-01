package com.routine.pusher.infrastructure.common.shared;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
public class LocalDateTimeWrapper
{
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime value;


    public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime>
    {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void serialize( LocalDateTime value, JsonGenerator generator, SerializerProvider serializers ) throws IOException
        {
            generator.writeString( value.format( formatter ) );
        }
    }

    public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
    {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public LocalDateTime deserialize( JsonParser parser, DeserializationContext context ) throws IOException
        {
            return LocalDateTime.parse( parser.getText( ), formatter );
        }
    }
}
