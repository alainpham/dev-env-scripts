package demo;

import java.util.HashMap;
import java.util.Map;

import com.demo.businessservice.Person;

import org.springframework.stereotype.Component;

@Component
public class PersonDB {
    private Map<String,Person> people;

    

    public PersonDB() {
        people = new HashMap<String,Person>();

        Person p ;
        p = new Person();
        p.setId("jdjd");
        p.setName("John Doe");
        p.setAge(25);
        people.put(p.getId(), p);

        p = new Person();
        p.setId("vxsq");
        p.setName("Jane Doe");
        p.setAge(21);
        people.put(p.getId(), p);

        p = new Person();
        p.setId("iidh");
        p.setName("Jack Lance");
        p.setAge(35);
        people.put(p.getId(), p);

        p = new Person();
        p.setId("iidh");
        p.setName("Jennie Kane");
        p.setAge(48);
        people.put(p.getId(), p);

    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public Person getPersonById(String id){
        return people.get(id);
    }

    public void setPeople(Map<String, Person> people) {
        this.people = people;
    }

    
}
