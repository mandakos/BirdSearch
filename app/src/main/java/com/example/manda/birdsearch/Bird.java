package com.example.manda.birdsearch;

import java.io.Serializable;

public class Bird implements Serializable {
    public String name_finnish; // Lajin suomen kielinen nimi
    public String name_latin; // Lajin latinan kielinen nimi
    public String desc; // Lajin kuvaus (species description)
    public String author; // Kirjoittaja

    public Bird(String name_latin, String desc, String author, String name_finnish){
        this.name_finnish = name_finnish;
        this.name_latin = name_latin;
        this.desc = desc;
        this.author = author;
    }

    public Bird(String name_latin){

        this.name_latin = name_latin;
    }

    public String getName_latin() {

        return name_latin;
    }

    public String getName_finnish() {

        return name_finnish;
    }

    public String getDesc() {
        return desc;
    }

    public String getAuthor() {
        return author;
    }

    public void setName_latin(String name_latin) {

        this.name_latin = name_latin;
    }
}
