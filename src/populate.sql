--
-- PostgreSQL database dump
--

-- Dumped from database version 14.19
-- Dumped by pg_dump version 14.18 (Ubuntu 14.18-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: courses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.courses (
    duration integer NOT NULL,
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.courses OWNER TO postgres;

--
-- Name: courses_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.courses_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.courses_seq OWNER TO postgres;

--
-- Name: curricular_unit_assistant_teachers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.curricular_unit_assistant_teachers (
    curricular_unit_id bigint NOT NULL,
    person_id bigint NOT NULL
);


ALTER TABLE public.curricular_unit_assistant_teachers OWNER TO postgres;

--
-- Name: curricular_unit_courses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.curricular_unit_courses (
    course_id bigint NOT NULL,
    curricular_unit_id bigint NOT NULL
);


ALTER TABLE public.curricular_unit_courses OWNER TO postgres;

--
-- Name: curricular_units; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.curricular_units (
    ects integer NOT NULL,
    id bigint NOT NULL,
    main_teacher_id bigint NOT NULL,
    code character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    semester character varying(255) NOT NULL,
    CONSTRAINT curricular_units_semester_check CHECK (((semester)::text = ANY ((ARRAY['FIRST'::character varying, 'SECOND'::character varying])::text[])))
);


ALTER TABLE public.curricular_units OWNER TO postgres;

--
-- Name: curricular_units_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.curricular_units_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.curricular_units_seq OWNER TO postgres;

--
-- Name: evaluation_grades; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.evaluation_grades (
    final_approved boolean,
    final_grade double precision,
    grade double precision NOT NULL,
    revision_requested boolean NOT NULL,
    teacher_suggestion double precision,
    evaluation_id bigint NOT NULL,
    final_decision_at timestamp(6) without time zone,
    graded_at timestamp(6) without time zone NOT NULL,
    id bigint NOT NULL,
    revision_requested_at timestamp(6) without time zone,
    student_enrollment_id bigint NOT NULL,
    teacher_revision_submitted_at timestamp(6) without time zone,
    final_justification character varying(255),
    revision_reason character varying(255),
    revision_status character varying(255),
    teacher_justification character varying(255),
    CONSTRAINT evaluation_grades_revision_status_check CHECK (((revision_status)::text = ANY ((ARRAY['NONE'::character varying, 'REQUESTED'::character varying, 'TEACHER_SUBMITTED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.evaluation_grades OWNER TO postgres;

--
-- Name: evaluation_grades_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.evaluation_grades_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.evaluation_grades_seq OWNER TO postgres;

--
-- Name: evaluations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.evaluations (
    weight double precision NOT NULL,
    curricular_unit_id bigint NOT NULL,
    date timestamp(6) without time zone NOT NULL,
    id bigint NOT NULL,
    revision_deadline timestamp(6) without time zone NOT NULL,
    evaluation_type character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    CONSTRAINT evaluations_evaluation_type_check CHECK (((evaluation_type)::text = ANY ((ARRAY['TEST'::character varying, 'PROJECT'::character varying])::text[])))
);


ALTER TABLE public.evaluations OWNER TO postgres;

--
-- Name: evaluations_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.evaluations_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.evaluations_seq OWNER TO postgres;

--
-- Name: people; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.people (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    ist_id character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    CONSTRAINT people_type_check CHECK (((type)::text = ANY ((ARRAY['ADMINISTRATOR'::character varying, 'MAIN_TEACHER'::character varying, 'TEACHING_ASSISTANT'::character varying, 'STUDENT'::character varying])::text[])))
);


ALTER TABLE public.people OWNER TO postgres;

--
-- Name: people_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.people_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.people_seq OWNER TO postgres;

--
-- Name: project_groups; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project_groups (
    final_grade double precision,
    created_at timestamp(6) without time zone NOT NULL,
    id bigint NOT NULL,
    project_id bigint NOT NULL,
    group_name character varying(255) NOT NULL
);


ALTER TABLE public.project_groups OWNER TO postgres;

--
-- Name: project_groups_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.project_groups_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.project_groups_seq OWNER TO postgres;

--
-- Name: project_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project_members (
    id bigint NOT NULL,
    joined_at timestamp(6) without time zone NOT NULL,
    project_group_id bigint NOT NULL,
    student_id bigint NOT NULL
);


ALTER TABLE public.project_members OWNER TO postgres;

--
-- Name: project_members_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.project_members_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.project_members_seq OWNER TO postgres;

--
-- Name: project_submissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project_submissions (
    automatic_grade double precision,
    is_latest boolean NOT NULL,
    file_size bigint NOT NULL,
    id bigint NOT NULL,
    project_group_id bigint,
    project_id bigint NOT NULL,
    submission_date timestamp(6) without time zone NOT NULL,
    submitted_by bigint NOT NULL,
    automatic_feedback text,
    comments text,
    file_extension character varying(255),
    mime_type character varying(255),
    original_filename character varying(255) NOT NULL,
    stored_filename character varying(255) NOT NULL
);


ALTER TABLE public.project_submissions OWNER TO postgres;

--
-- Name: project_submissions_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.project_submissions_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.project_submissions_seq OWNER TO postgres;

--
-- Name: projects; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.projects (
    max_group_size integer,
    id bigint NOT NULL,
    max_file_size bigint,
    submission_deadline timestamp(6) without time zone NOT NULL,
    allowed_extensions character varying(255),
    description text
);


ALTER TABLE public.projects OWNER TO postgres;

--
-- Name: resources; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resources (
    curricular_unit_id bigint NOT NULL,
    file_size bigint,
    id bigint NOT NULL,
    upload_date timestamp(6) without time zone NOT NULL,
    file_name character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    resource_type character varying(255) NOT NULL,
    CONSTRAINT resources_resource_type_check CHECK (((resource_type)::text = ANY ((ARRAY['MATERIAL'::character varying, 'SUBMISSION'::character varying])::text[])))
);


ALTER TABLE public.resources OWNER TO postgres;

--
-- Name: resources_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.resources_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resources_seq OWNER TO postgres;

--
-- Name: student_enrollments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.student_enrollments (
    completion_date date,
    enrollment_date date NOT NULL,
    final_grade double precision,
    curricular_unit_id bigint NOT NULL,
    id bigint NOT NULL,
    student_id bigint NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT student_enrollments_status_check CHECK (((status)::text = ANY ((ARRAY['ENROLLED'::character varying, 'APPROVED'::character varying, 'FAILED'::character varying])::text[])))
);


ALTER TABLE public.student_enrollments OWNER TO postgres;

--
-- Name: student_enrollments_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.student_enrollments_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.student_enrollments_seq OWNER TO postgres;

--
-- Name: tests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tests (
    id bigint NOT NULL
);


ALTER TABLE public.tests OWNER TO postgres;

--
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.courses (duration, id, code, name) FROM stdin;
3	1	LEIC-A	Licenciatura em Engenharia Informática e de Computadores (Campus Alameda)
3	2	LEIC-T	Licenciatura em Engenharia Informática e de Computadores (Campus Taguspark)
\.


--
-- Data for Name: curricular_unit_assistant_teachers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.curricular_unit_assistant_teachers (curricular_unit_id, person_id) FROM stdin;
3	14
1	3
2	3
4	3
\.


--
-- Data for Name: curricular_unit_courses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.curricular_unit_courses (course_id, curricular_unit_id) FROM stdin;
\.


--
-- Data for Name: curricular_units; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.curricular_units (ects, id, main_teacher_id, code, name, semester) FROM stdin;
6	1	1	FP	Fundamentos da Programação	FIRST
6	2	8	IAC	Introdução à Arquitetura de Computadores	SECOND
6	3	7	ASA	Análise e Síntese de Algoritmos	SECOND
3	4	13	IEI	Introdução à Engenharia Informática	FIRST
\.


--
-- Data for Name: evaluation_grades; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.evaluation_grades (final_approved, final_grade, grade, revision_requested, teacher_suggestion, evaluation_id, final_decision_at, graded_at, id, revision_requested_at, student_enrollment_id, teacher_revision_submitted_at, final_justification, revision_reason, revision_status, teacher_justification) FROM stdin;
\N	\N	15	f	\N	1	\N	2025-09-07 23:46:55.129414	2	\N	7	\N	\N	\N	NONE	\N
\N	\N	9	f	\N	1	\N	2025-09-07 23:46:55.148961	3	\N	8	\N	\N	\N	NONE	\N
\N	\N	20	f	\N	1	\N	2025-09-07 23:46:55.184987	5	\N	10	\N	\N	\N	NONE	\N
\N	\N	12	f	\N	5	\N	2025-09-07 23:50:30.034317	6	\N	11	\N	\N	\N	NONE	\N
\N	\N	10	f	\N	5	\N	2025-09-07 23:50:30.050212	7	\N	12	\N	\N	\N	NONE	\N
\N	\N	5	f	\N	5	\N	2025-09-07 23:50:30.066882	8	\N	13	\N	\N	\N	NONE	\N
\N	\N	17	f	\N	5	\N	2025-09-07 23:50:30.100905	10	\N	15	\N	\N	\N	NONE	\N
\N	\N	12	t	\N	1	\N	2025-09-07 23:46:55.101936	1	2025-09-07 23:52:33.478503	6	\N	\N	Respondi ao 2 e acho não ter sido cotada.	REQUESTED	\N
\N	\N	2	t	\N	5	\N	2025-09-07 23:50:30.084435	9	2025-09-07 23:53:16.637118	14	\N	\N	Professor, correu-me mal mas nem assim tanto, por favor avalie outravez.	REQUESTED	\N
\N	\N	8	t	9.5	1	\N	2025-09-07 23:46:55.167102	4	2025-09-07 23:53:47.331384	9	2025-09-07 23:54:20.816904	\N	Acredito que mereço mais pois respondi a todos os items.	TEACHER_SUBMITTED	Realmente tem razão, é possível subir a nota.
\.


--
-- Data for Name: evaluations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.evaluations (weight, curricular_unit_id, date, id, revision_deadline, evaluation_type, title) FROM stdin;
0.5	1	2025-09-19 11:00:00	1	2025-09-26 11:00:00	TEST	MAP
0.5	1	2025-09-30 23:59:00	2	2025-10-10 12:00:00	PROJECT	Projeto Inicial em Python
1	4	2025-10-30 13:00:00	3	2025-11-07 12:00:00	TEST	Exame
0.25	2	2025-09-08 10:00:00	4	2025-09-15 12:00:00	TEST	MAP 1
0.25	2	2025-09-22 11:00:00	5	2025-09-29 12:00:00	TEST	MAP 2
0.5	2	2025-09-30 23:59:00	6	2025-10-10 12:00:00	PROJECT	Projeto Inicial de IAC
1	3	2025-11-03 09:00:00	7	2025-11-10 12:00:00	TEST	Exame
\.


--
-- Data for Name: people; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.people (id, email, ist_id, name, type) FROM stdin;
1	arld@tecnico.ulisboa.pt	ist5346	Arlindo Oliveira	MAIN_TEACHER
2	joao.s.viegas@tecnico.ulisboa.pt	ist109685	João Sérgio Viegas	STUDENT
3	vicente@tecnico.ulisboa.pt	ist106787	Vicente Duarte	TEACHING_ASSISTANT
4	andre.v@tecnico.ulisboa.pt	ist102345	André Viveiros	STUDENT
5	alex.h@tecnico.ulisboa.pt	ist109675	Alexandre Henriques	STUDENT
6	dm@tecnico.ulisboa.pt	ist5003	David Matos	ADMINISTRATOR
7	jose.f@tecnico.ulisboa.pt	ist5490	José Fragoso	MAIN_TEACHER
8	joao.r@tecnico.ulisboa.pt	ist5000	João Rodrigues	MAIN_TEACHER
9	robie.m@tecnico.ulisboa.pt	ist107654	Roberto Mauser	STUDENT
10	danilo.p@tecnico.ulisboa.pt	ist103456	Danilo Pereira	STUDENT
11	lili.gar@tecnico.ulisboa.pt	ist109954	Liliana Garcês	STUDENT
12	sara.f@tecnico.ulisboa.pt	ist109167	Sara Freitas	STUDENT
13	nuno.m@tecnico.ulisboa.pt	ist3045	Nuno Mamede	MAIN_TEACHER
14	josemarques@tecnico.ulisboa.pt	ist103045	José Marques	TEACHING_ASSISTANT
\.


--
-- Data for Name: project_groups; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project_groups (final_grade, created_at, id, project_id, group_name) FROM stdin;
\N	2025-09-07 23:47:52.03743	1	2	Grupo 1
\N	2025-09-07 23:47:52.040754	2	2	Grupo 2
\N	2025-09-07 23:47:52.041535	3	2	Grupo 3
\N	2025-09-07 23:51:31.809409	4	6	Grupo 1
\N	2025-09-07 23:51:31.809786	5	6	Grupo 2
\N	2025-09-07 23:51:31.809993	6	6	Grupo 3
\.


--
-- Data for Name: project_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project_members (id, joined_at, project_group_id, student_id) FROM stdin;
1	2025-09-07 23:47:52.038599	1	9
2	2025-09-07 23:47:52.039626	1	5
3	2025-09-07 23:47:52.04134	2	12
4	2025-09-07 23:47:52.041406	2	11
5	2025-09-07 23:47:52.041669	3	10
6	2025-09-07 23:51:31.809595	4	10
7	2025-09-07 23:51:31.809645	4	5
8	2025-09-07 23:51:31.809868	5	9
9	2025-09-07 23:51:31.809907	5	2
10	2025-09-07 23:51:31.810062	6	4
\.


--
-- Data for Name: project_submissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project_submissions (automatic_grade, is_latest, file_size, id, project_group_id, project_id, submission_date, submitted_by, automatic_feedback, comments, file_extension, mime_type, original_filename, stored_filename) FROM stdin;
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.projects (max_group_size, id, max_file_size, submission_deadline, allowed_extensions, description) FROM stdin;
2	2	10485760	2025-09-30 23:59:00	py,c,java,zip	Cria um programa em Python que peça ao utilizador o nome e a idade, e depois mostre uma mensagem personalizada a indicar em que ano terá 100 anos.
2	6	10485760	2025-09-30 23:59:00	py,c,java,zip	Implemente em Assembly MIPS um programa que leia dois inteiros, calcule a soma e mostre o resultado no ecrã.
\.


--
-- Data for Name: resources; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.resources (curricular_unit_id, file_size, id, upload_date, file_name, name, resource_type) FROM stdin;
1	1875	1	2025-09-07 23:47:02.09727	4ff373cf-9a8f-4d06-8d05-f6ee4a6ba31a_MAP1_correcao.pdf	MAP1_correcao.pdf	MATERIAL
1	1846	2	2025-09-07 23:47:04.783764	0f250fc3-6633-4350-8026-764951418bbc_MAP1_enunciado.pdf	MAP1_enunciado.pdf	MATERIAL
2	1875	3	2025-09-07 23:50:03.733154	61c5297c-45c7-40f1-b602-88af5887d32d_MAP1_correcao.pdf	MAP1_correcao.pdf	MATERIAL
2	1846	4	2025-09-07 23:50:06.327031	a90f0b69-ba1c-4c4d-91ba-00e6b65d6669_MAP1_enunciado.pdf	MAP1_enunciado.pdf	MATERIAL
2	1875	5	2025-09-07 23:50:12.571861	a95f4732-3d12-4266-b160-6ced82632159_MAP2_correcao.pdf	MAP2_correcao.pdf	MATERIAL
2	1846	6	2025-09-07 23:50:19.807939	d6ebfce0-b0a6-4203-8514-36ffd5dcaa67_MAP2_enunciado.pdf	MAP2_enunciado.pdf	MATERIAL
\.


--
-- Data for Name: student_enrollments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.student_enrollments (completion_date, enrollment_date, final_grade, curricular_unit_id, id, student_id, status) FROM stdin;
\N	2025-09-07	\N	3	1	2	ENROLLED
\N	2025-09-07	\N	3	2	4	ENROLLED
\N	2025-09-07	\N	3	3	5	ENROLLED
\N	2025-09-07	\N	3	4	9	ENROLLED
\N	2025-09-07	\N	3	5	10	ENROLLED
\N	2025-09-07	\N	1	6	11	ENROLLED
\N	2025-09-07	\N	1	7	10	ENROLLED
\N	2025-09-07	\N	1	8	12	ENROLLED
\N	2025-09-07	\N	1	9	9	ENROLLED
\N	2025-09-07	\N	1	10	5	ENROLLED
\N	2025-09-07	\N	2	11	5	ENROLLED
\N	2025-09-07	\N	2	12	2	ENROLLED
\N	2025-09-07	\N	2	13	4	ENROLLED
\N	2025-09-07	\N	2	14	9	ENROLLED
\N	2025-09-07	\N	2	15	10	ENROLLED
\N	2025-09-07	\N	4	16	11	ENROLLED
\N	2025-09-07	\N	4	17	12	ENROLLED
\N	2025-09-07	\N	4	18	10	ENROLLED
\N	2025-09-07	\N	4	19	9	ENROLLED
\N	2025-09-07	\N	4	20	5	ENROLLED
\.


--
-- Data for Name: tests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tests (id) FROM stdin;
1
3
4
5
7
\.


--
-- Name: courses_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.courses_seq', 1, false);


--
-- Name: curricular_units_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.curricular_units_seq', 1, false);


--
-- Name: evaluation_grades_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.evaluation_grades_seq', 51, true);


--
-- Name: evaluations_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.evaluations_seq', 51, true);


--
-- Name: people_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.people_seq', 1, false);


--
-- Name: project_groups_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.project_groups_seq', 51, true);


--
-- Name: project_members_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.project_members_seq', 51, true);


--
-- Name: project_submissions_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.project_submissions_seq', 1, false);


--
-- Name: resources_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.resources_seq', 51, true);


--
-- Name: student_enrollments_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.student_enrollments_seq', 51, true);


--
-- Name: courses courses_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_code_key UNIQUE (code);


--
-- Name: courses courses_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_name_key UNIQUE (name);


--
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (id);


--
-- Name: curricular_unit_assistant_teachers curricular_unit_assistant_teachers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.curricular_unit_assistant_teachers
    ADD CONSTRAINT curricular_unit_assistant_teachers_pkey PRIMARY KEY (curricular_unit_id, person_id);


--
-- Name: curricular_unit_courses curricular_unit_courses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.curricular_unit_courses
    ADD CONSTRAINT curricular_unit_courses_pkey PRIMARY KEY (course_id, curricular_unit_id);


--
-- Name: curricular_units curricular_units_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.curricular_units
    ADD CONSTRAINT curricular_units_code_key UNIQUE (code);


--
-- Name: curricular_units curricular_units_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.curricular_units
    ADD CONSTRAINT curricular_units_pkey PRIMARY KEY (id);


--
-- Name: evaluation_grades evaluation_grades_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.evaluation_grades
    ADD CONSTRAINT evaluation_grades_pkey PRIMARY KEY (id);


--
-- Name: evaluations evaluations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.evaluations
    ADD CONSTRAINT evaluations_pkey PRIMARY KEY (id);


--
-- Name: people people_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.people
    ADD CONSTRAINT people_email_key UNIQUE (email);


--
-- Name: people people_ist_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.people
    ADD CONSTRAINT people_ist_id_key UNIQUE (ist_id);


--
-- Name: people people_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.people
    ADD CONSTRAINT people_pkey PRIMARY KEY (id);


--
-- Name: project_groups project_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_groups
    ADD CONSTRAINT project_groups_pkey PRIMARY KEY (id);


--
-- Name: project_members project_members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_members
    ADD CONSTRAINT project_members_pkey PRIMARY KEY (id);


--
-- Name: project_submissions project_submissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_submissions
    ADD CONSTRAINT project_submissions_pkey PRIMARY KEY (id);


--
-- Name: projects projects_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_pkey PRIMARY KEY (id);


--
-- Name: resources resources_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resources
    ADD CONSTRAINT resources_pkey PRIMARY KEY (id);


--
-- Name: student_enrollments student_enrollments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.student_enrollments
    ADD CONSTRAINT student_enrollments_pkey PRIMARY KEY (id);


--
-- Name: tests tests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tests
    ADD CONSTRAINT tests_pkey PRIMARY KEY (id);


--
-- Name: project_submissions fk34r3v4wlh63x2urc9rmm68sul; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_submissions
    ADD CONSTRAINT fk34r3v4wlh63x2urc9rmm68sul FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: project_groups fk5qcfmaoj7jnpjt4d3e1nq2cew; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_groups
    ADD CONSTRAINT fk5qcfmaoj7jnpjt4d3e1nq2cew FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: resources fk6moxmold99dwgu16wnnb5gkhg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.resources
    ADD CONSTRAINT fk6moxmold99dwgu16wnnb5gkhg FOREIGN KEY (curricular_unit_id) REFERENCES public.curricular_units(id);


--
-- Name: curricular_unit_assistant_teachers fkayqwsxyxporr49m1jb28pued; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.curricular_unit_assistant_teachers
    ADD CONSTRAINT fkayqwsxyxporr49m1jb28pued FOREIGN KEY (person_id) REFERENCES public.people(id);


--
-- Name: project_members fkce31jltmiuna2ekm74w8vjogt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_members
    ADD CONSTRAINT fkce31jltmiuna2ekm74w8vjogt FOREIGN KEY (student_id) REFERENCES public.people(id);


--
-- Name: projects fkdktdum1378yrno9waiodk1nj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT fkdktdum1378yrno9waiodk1nj FOREIGN KEY (id) REFERENCES public.evaluations(id);


--
-- Name: curricular_unit_courses fkfejgd60i3w76y86g4em8frqs9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.curricular_unit_courses
    ADD CONSTRAINT fkfejgd60i3w76y86g4em8frqs9 FOREIGN KEY (curricular_unit_id) REFERENCES public.curricular_units(id);


--
-- Name: evaluation_grades fkfgo5qq5glp1bl37a3ppknw7re; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.evaluation_grades
    ADD CONSTRAINT fkfgo5qq5glp1bl37a3ppknw7re FOREIGN KEY (student_enrollment_id) REFERENCES public.student_enrollments(id);


--
-- Name: curricular_unit_courses fkg0rcv8a5uqk3l3s8myutpx01u; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.curricular_unit_courses
    ADD CONSTRAINT fkg0rcv8a5uqk3l3s8myutpx01u FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: project_submissions fkhc7pe1kdwhh1nlkrcab17vrrr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_submissions
    ADD CONSTRAINT fkhc7pe1kdwhh1nlkrcab17vrrr FOREIGN KEY (project_group_id) REFERENCES public.project_groups(id);


--
-- Name: curricular_units fkho54205pro22kj4hv694739u1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.curricular_units
    ADD CONSTRAINT fkho54205pro22kj4hv694739u1 FOREIGN KEY (main_teacher_id) REFERENCES public.people(id);


--
-- Name: evaluation_grades fkne91wpmre8fvlrvyk8dpxi65k; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.evaluation_grades
    ADD CONSTRAINT fkne91wpmre8fvlrvyk8dpxi65k FOREIGN KEY (evaluation_id) REFERENCES public.evaluations(id);


--
-- Name: curricular_unit_assistant_teachers fkorhi2yfjcubu57y19mmjpokwv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.curricular_unit_assistant_teachers
    ADD CONSTRAINT fkorhi2yfjcubu57y19mmjpokwv FOREIGN KEY (curricular_unit_id) REFERENCES public.curricular_units(id);


--
-- Name: student_enrollments fkprlhjj7160k6o5tmrwnuohwrf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.student_enrollments
    ADD CONSTRAINT fkprlhjj7160k6o5tmrwnuohwrf FOREIGN KEY (curricular_unit_id) REFERENCES public.curricular_units(id);


--
-- Name: student_enrollments fkq51fq3pxima6fe3keqvc9ubrb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.student_enrollments
    ADD CONSTRAINT fkq51fq3pxima6fe3keqvc9ubrb FOREIGN KEY (student_id) REFERENCES public.people(id);


--
-- Name: evaluations fkqbvryax47qxf8ld8ptket9dvi; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.evaluations
    ADD CONSTRAINT fkqbvryax47qxf8ld8ptket9dvi FOREIGN KEY (curricular_unit_id) REFERENCES public.curricular_units(id);


--
-- Name: project_members fkqdpgauymexk10d85rhtr26tfx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_members
    ADD CONSTRAINT fkqdpgauymexk10d85rhtr26tfx FOREIGN KEY (project_group_id) REFERENCES public.project_groups(id);


--
-- Name: project_submissions fkt1alp64pi0u7dsega9gbx550l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_submissions
    ADD CONSTRAINT fkt1alp64pi0u7dsega9gbx550l FOREIGN KEY (submitted_by) REFERENCES public.people(id);


--
-- Name: tests fktf1swlga3lcn3d6ylj3lsx2ux; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tests
    ADD CONSTRAINT fktf1swlga3lcn3d6ylj3lsx2ux FOREIGN KEY (id) REFERENCES public.evaluations(id);


--
-- PostgreSQL database dump complete
--

