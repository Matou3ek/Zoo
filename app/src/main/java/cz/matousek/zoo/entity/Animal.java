package cz.matousek.zoo.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cz.matousek.zoo.entity.jackson.AnimalDeserializer;

/**
 * Created by matousekl on 9/21/2017.
 */
@JsonDeserialize(using = AnimalDeserializer.class)
public class Animal {
    @JsonIgnore
    private int id;
    private String name;

    @JsonCreator
    public Animal() {
    }

    @JsonCreator
    public Animal(String name) {
    }

    @JsonCreator
    public Animal(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonSetter("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonGetter("id")
    public int getId() {
        return id;
    }

    @JsonGetter("name")
    public String getName() {
        return name;
    }
}
