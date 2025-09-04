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
	COURSE_NAME_ALREADY_EXISTS("Já existe um curso com o nome %s", 2005),
	COURSE_DURATION_NOT_VALID("A duração do curso especificada não é válida.", 2006),

	// CurricularUnit errors
	NO_SUCH_CURRICULAR_UNIT("Não existe nenhuma unidade curricular com o ID %s", 3001),
	CURRICULAR_UNIT_CODE_NOT_VALID("O código da unidade curricular especificado não é válido.", 3002),
	CURRICULAR_UNIT_NAME_NOT_VALID("O nome da unidade curricular especificado não é válido.", 3003),
	CURRICULAR_UNIT_ALREADY_EXISTS("Já existe uma unidade curricular com o código %s", 3004),
	CURRICULAR_UNIT_ECTS_NOT_VALID("O número de ECTS especificado não é válido.", 3005),
	CURRICULAR_UNIT_SEMESTER_NOT_VALID("O semestre especificado não é válido.", 3006),
	PERSON_NOT_MAIN_TEACHER("A pessoa especificada não é um professor regente.", 3007),

	// StudentEnrollment errors
	NO_SUCH_STUDENT_ENROLLMENT("Não existe nenhuma inscrição com o ID %s", 4001),
	PERSON_NOT_STUDENT("A pessoa especificada não é um aluno.", 4002),
	STUDENT_ALREADY_ENROLLED("O aluno já está inscrito nesta unidade curricular.", 4003),
	ENROLLMENT_STATUS_NOT_VALID("O estado da inscrição especificado não é válido.", 4004),

	// Test errors
	NO_SUCH_TEST("Não existe nenhum teste com o ID %s", 5001),
	TEST_TITLE_NOT_VALID("O título do teste especificado não é válido.", 5002),
	TEST_DATE_NOT_VALID("A data do teste especificada não é válida.", 5003),
	TEST_WEIGHT_NOT_VALID("O peso do teste especificado não é válido.", 5004),
	EVALUATION_WEIGHT_EXCEEDS_LIMIT("O peso total das avaliações não pode exceder 1.0 (100%).", 5005),

	// EvaluationGrade errors
	NO_SUCH_EVALUATION_GRADE("Não existe nenhuma nota com o ID %s", 6001),
	GRADE_NOT_VALID("A nota especificada deve estar entre 0.0 e 20.0.", 6002),

	// File errors
	EMPTY_REQUIRED_FIELD("O campo %s é obrigatório.", 7001),
	RESOURCE_NOT_FOUND("Não foi possível encontrar o recurso %s.", 7002);

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
