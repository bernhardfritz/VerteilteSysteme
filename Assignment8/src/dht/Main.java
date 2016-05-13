package dht;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

	public static void main(String[] args) {
		String[] forenames = {"Mia", "Emma", "Hannah", "Sofia", "Anna", "Emilia", "Lina", "Marie", "Lena", "Mila", 
				"Ben", "Jonas", "Leon", "Elias", "Finn", "Noah", "Paul", "Luis", "Lukas", "Luca"};
		String[] surnames = {"Gruber", "Huber", "Wagner", "Müller", "Pichler", "Moser", "Steiner", "Mayer", 
				"Berger", "Hofer", "Eder", "Bauer", "Fuchs", "Schmid", "Weber"};
		int m = 5;
		
		DHTNode n1 = new DHTNode(1, m);
		DHTNode n7 = new DHTNode(7, m);
		DHTNode n15 = new DHTNode(15, m);
		DHTNode n25 = new DHTNode(25, m);
		DHTNode n27 = new DHTNode(27, m);
		
		n1.join(null);
		n7.join(n1);
		n15.join(n1);
		n25.join(n1);
		n27.join(n1);
		
		List<Person> persons = new ArrayList<Person>();
		for (int i = 0; i < 50; i++) {
			Person p = new Person(forenames[new Random().nextInt(forenames.length)], surnames[new Random().nextInt(surnames.length)], new Random().nextInt(100)+1);
			n1.put(p.getName(), p);
			persons.add(p);
		}
		
		n1.printMap();
		n7.printMap();
		n15.printMap();
		n25.printMap();
		n27.printMap();
		System.out.println();
		
		Person p = persons.get(persons.size()-1);
		System.out.println("Contain(" + p.getName() + "): " + n1.containsKey(p.getName()) + " (" + n1.get(p.getName()) + ")");
		System.out.println();
		
		for (int i = 0; i < persons.size()/2; i++) {
			p = persons.get(new Random().nextInt(persons.size()));
			n1.remove(p.getName());
		}
		
		n1.printMap();
		n7.printMap();
		n15.printMap();
		n25.printMap();
		n27.printMap();
	}
}