package com.routine.pusher.infrastructure.common.shared;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class LocalDateTimeListWrapper
{
    @JsonSerialize(using = LocalDateTimeListWrapper.LocalDateTimeListSerializer.class)
    @JsonDeserialize(using = LocalDateTimeListWrapper.LocalDateTimeListDeserializer.class)
    private LocalDateTime value;


    public static class LocalDateTimeListSerializer extends JsonSerializer<List<LocalDateTime>>
    {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void serialize( List<LocalDateTime> values, JsonGenerator gen, SerializerProvider serializers ) throws IOException
        {
            gen.writeStartArray( );

            for ( LocalDateTime dateTime : values )
                gen.writeString( dateTime.format( formatter ) );

            gen.writeEndArray( );
        }
    }

    public static class LocalDateTimeListDeserializer extends JsonDeserializer<List<LocalDateTime>>
    {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public List<LocalDateTime> deserialize( JsonParser parser, DeserializationContext context ) throws IOException
        {
            List<String> dateAsString = parser.readValueAs( new TypeReference<List<String>>( ) { } );

            return dateAsString.stream( )
                    .map(date -> LocalDateTime.parse( String.valueOf( date ), formatter ) )
                    .collect( Collectors.toList( ) );
        }
    }

}
