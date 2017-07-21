package com.sean.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonMapper {

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Person person = new Person();
        person.age = 19;
        person.country = "china";
        Son son = new Son();
        son.name = "son";
        person.son = son;
        String result = mapper.writeValueAsString(person);
        System.out.println(result);
        Person person1 = mapper.readValue(result,Person.class);
        System.out.println(person1.getCountry());
        List<String> list = mapper.readValue("[\"a\",\"b\"]", ArrayList.class);
        List<Person> personList = Lists.newArrayList(new Person(11,"henan",new Son("s1")),new Person(12,"hebei",new Son("s2")));
        result = mapper.writeValueAsString(personList);
        System.out.println(result);
        List<Person> personList1 = mapper.readValue(result, new TypeReference<List<Person>>() {
        });
        System.out.println(personList1);
        JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class,Person.class);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        personList1 = mapper.readValue(result,javaType);
        System.out.println(personList1);
    }

    static class Person {
        private List<String> names = Lists.newArrayList("sean", "guo");
        private int age;
        private String country;
        private Son son;

        public Person(){}

        public Person(int age,String country,Son son){
            this.age = age;
            this.country = country;
            this.son = son;
        }

        public Son getSon() {
            return son;
        }

        public void setSon(Son son) {
            this.son = son;
        }

        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String toString(){
            return MoreObjects.toStringHelper(this).add("age",age).add("country",country).toString();
        }
    }

    static class Son {
        private String name;

        public Son(){}

        public Son(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
