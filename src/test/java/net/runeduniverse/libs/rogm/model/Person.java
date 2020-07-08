package net.runeduniverse.libs.rogm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.runeduniverse.libs.rogm.annotations.Property;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Person extends AEntity {

	private String firstName;
	private String lastName;
	private boolean fictional;
	
	@Property
	private Address address;
	
	public Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public Person(String firstName, String lastName, boolean fictional) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.fictional = fictional;
	}
}
