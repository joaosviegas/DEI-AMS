package pt.ulisboa.tecnico.rnl.dei.dms.person.domain;


import jakarta.persistence.*;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;

// Domain class representing a person in the system
@Data
@Entity
@Getter
@Setter
@Table(name = "people")
public class Person {

	public enum PersonType {
		ADMINISTRATOR,
		MAIN_TEACHER,
		TEACHING_ASSISTANT,
		STUDENT
		// maybe add more types later?
	}

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "ist_id", nullable = false, unique = true)
	private String istId;

	@Column(name="email", nullable=false, unique=true)
	private String email;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
    private PersonType type;


	protected Person() {
	}

	public Person(String name, String istId, String email, PersonType type) {
		this.name = name;
		this.istId = istId;
		this.email= email;
		this.type = type;
	}

	public Person(PersonDto personDto) {
		this(personDto.name(), personDto.istId(), personDto.email(),
				PersonType.valueOf(personDto.type().toUpperCase()));
	}
}
