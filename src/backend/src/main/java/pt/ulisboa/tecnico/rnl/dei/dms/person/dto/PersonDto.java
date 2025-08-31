package pt.ulisboa.tecnico.rnl.dei.dms.person.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

// Data Transfer Object, to communicate with frontend
public record PersonDto(long id, String name, String istId, String email, String type) {
	public PersonDto(Person person) {
		this(person.getId(), person.getName(), person.getIstId(), person.getEmail(),
				person.getType().toString());
	}
}
