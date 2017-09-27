package cz.matousek.zoo.entity.jackson;

/**
 * Created by matousekl on 9/21/2017.
 */

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import cz.matousek.zoo.entity.Animal;


public class AnimalDeserializer extends StdDeserializer<Animal> {

    public AnimalDeserializer() {
        this(null);
    }

    public AnimalDeserializer(Class<?> vc) {
        super(vc);
    }


    @Override
    public Animal deserialize(JsonParser arg0, DeserializationContext arg1)
            throws IOException, JsonProcessingException {
        JsonNode node = arg0.getCodec().readTree(arg0);
        Animal animal = new Animal();
        int id = node.get("id").asInt();
        String name = node.get("name").asText();
        animal.setName(name);
        animal.setId(id);
        return animal;
    }

}
