package dht;

import java.io.Serializable;

public class Person implements Serializable {
	
	private static final long serialVersionUID = 5056220435601201673L;

	private String forename;
	
	private String surname;
	
	private int age;

	public Person(String forename, String surname, int age) {
		this.forename = forename;
		this.surname = surname;
		this.age = age;
	}

	public String getForename() {
		return forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	
	public String getName() {
		return forename + " " + surname;
	}
	
	@Override
	public String toString() {
		return getName() + "(" + age + ")";
	}
}