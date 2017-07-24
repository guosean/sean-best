package com.sean.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonMapper {

    private static ObjectMapper mapper;

    private JsonMapper(JsonInclude.Include include){
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(include);
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED,true);
        mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN,true);
    }

    public static JsonMapper buildNormal(){
        return new JsonMapper(JsonInclude.Include.ALWAYS);
    }

    public static JsonMapper buildNonNull(){
        return new JsonMapper(JsonInclude.Include.NON_NULL);
    }

    public <T> T fromJson(String json,Class<T> t) {
        T result = null;
        try {
            result =  mapper.readValue(json,t);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String toJson(Object obj){
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "{}";
    }
    

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

        AnnotationBean ab = new AnnotationBean();
        ab.setAppName("sean");
        ab.setAppCode("1111");
        result = mapper.writeValueAsString(ab);
        System.out.println(ab);
        ab = mapper.readValue(result,AnnotationBean.class);
        ab.setAppName(ab.getAppName()+"--te");
        System.out.println(ab.getAppName());
    }

    static class AnnotationBean{
        @JsonProperty("app_name")
        private String appName;

        private String appCode;

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getAppCode() {
            return appCode;
        }

        public void setAppCode(String appCode) {
            this.appCode = appCode;
        }
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
