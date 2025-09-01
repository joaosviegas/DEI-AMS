package pt.ulisboa.tecnico.rnl.dei.dms.exceptions;

public enum ErrorMessage {
	// Person errors
	NO_SUCH_PERSON("Não existe nenhuma pessoa com o ID %s", 1001),
	PERSON_NAME_NOT_VALID("O nome da pessoa especificado não é válido.", 1002),
	PERSON_ALREADY_EXISTS("Já existe uma pessoa com o IST ID %s", 1003),
	EMAIL_ALREADY_EXISTS("Já existe uma pessoa com o email %s", 1004),
	PERSON_EMAIL_NOT_VALID("O email especificado não é válido.", 1005),
	PERSON_TYPE_NOT_VALID("A categoria de pessoa especificada não é válida.", 1006),
	
	// Course errors
	NO_SUCH_COURSE("Não existe nenhum curso com o ID %s", 2001),
	COURSE_CODE_NOT_VALID("O código do curso especificado não é válido.", 2002),
	COURSE_NAME_NOT_VALID("O nome do curso especificado não é válido.", 2003),
	COURSE_ALREADY_EXISTS("Já existe um curso com o código %s", 2004),
	COURSE_NAME_ALREADY_EXISTS("Já existe um curso com o nome %s", 2005);

	private final String label;
	private final int code;

	ErrorMessage(String label, int code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public int getCode() {
		return this.code;
	}
}
