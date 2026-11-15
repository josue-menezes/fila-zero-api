-- =====================================================================
-- GLOBAL SOLUTION 2026/2 - CITIZENTECH
-- Building Relational Database - Projeto: FILA ZERO
-- Turma: 1TDSR
-- Arthur Cordão - RM 570313
-- Guilherme Novaes - RM 570444
-- Jeferson Faria - RM 573038
-- Josué Menezes - RM 569220
-- =====================================================================
-- Ordem do script (exigida pelo professor):
--   1. Criação das tabelas e sequences
--   2. INSERTs (60 registros)
--   3. 10 consultas numeradas
--   4. UPDATE e DELETE com WHERE
-- =====================================================================


-- =====================================================================
-- 0. LIMPEZA (OPCIONAL) - só descomente se precisar rodar o script de novo.
-- ATENÇÃO: apaga as tabelas com esses nomes no seu usuário Oracle.
-- =====================================================================
/*
BEGIN
  FOR t IN (SELECT table_name FROM user_tables
            WHERE table_name IN ('TB_AGENDAMENTO','TB_ATENDENTE','TB_UNIDADE_SERVICO',
                                 'TB_CIDADAO','TB_SERVICO','TB_UNIDADE')) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
  END LOOP;
  FOR s IN (SELECT sequence_name FROM user_sequences
            WHERE sequence_name IN ('SQ_TB_AGENDAMENTO','SQ_TB_ATENDENTE','SQ_TB_CIDADAO',
                                    'SQ_TB_SERVICO','SQ_TB_UNIDADE')) LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
  END LOOP;
END;
/
*/


-- =====================================================================
-- 1. CRIAÇÃO DAS TABELAS E SEQUENCES
-- Ordem respeitando as chaves estrangeiras:
-- tb_unidade -> tb_servico -> tb_cidadao -> tb_unidade_servico
-- -> tb_atendente -> tb_agendamento
-- =====================================================================

-- ---------------------------------------------------------------------
-- tb_unidade: locais físicos de atendimento (estilo Poupatempo)
-- ---------------------------------------------------------------------
CREATE TABLE tb_unidade (
    cd_unidade     NUMBER(10)     NOT NULL,
    nm_unidade     VARCHAR2(100)  NOT NULL,
    ds_endereco    VARCHAR2(150)  NOT NULL,
    nm_bairro      VARCHAR2(60)   NOT NULL,
    nm_cidade      VARCHAR2(60)   NOT NULL,
    nr_guiches     NUMBER(3)      NOT NULL,
    hr_abertura    NUMBER(2)      NOT NULL,
    hr_fechamento  NUMBER(2)      NOT NULL,
    CONSTRAINT pk_unidade PRIMARY KEY (cd_unidade),
    CONSTRAINT uk_unidade_nome UNIQUE (nm_unidade),
    CONSTRAINT ck_unidade_guiches CHECK (nr_guiches > 0),
    CONSTRAINT ck_unidade_abertura CHECK (hr_abertura BETWEEN 0 AND 23),
    CONSTRAINT ck_unidade_horario CHECK (hr_fechamento > hr_abertura AND hr_fechamento <= 23)
);
COMMENT ON TABLE tb_unidade IS 'Unidades de atendimento ao cidadão (estilo Poupatempo). nr_guiches define quantos agendamentos cabem no mesmo horário.';

CREATE SEQUENCE sq_tb_unidade START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- tb_servico: tipos de atendimento (RG, CNH, etc.)
-- ---------------------------------------------------------------------
CREATE TABLE tb_servico (
    cd_servico      NUMBER(10)     NOT NULL,
    nm_servico      VARCHAR2(80)   NOT NULL,
    ds_servico      VARCHAR2(255),
    ds_documentos   VARCHAR2(255)  NOT NULL,
    nr_duracao_min  NUMBER(3)      NOT NULL,
    CONSTRAINT pk_servico PRIMARY KEY (cd_servico),
    CONSTRAINT uk_servico_nome UNIQUE (nm_servico),
    CONSTRAINT ck_servico_duracao CHECK (nr_duracao_min > 0)
);
COMMENT ON TABLE tb_servico IS 'Serviços públicos que podem ser agendados, com documentos necessários e duração média do atendimento.';

CREATE SEQUENCE sq_tb_servico START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- tb_cidadao: pessoas que agendam atendimentos
-- ---------------------------------------------------------------------
CREATE TABLE tb_cidadao (
    cd_cidadao     NUMBER(10)     NOT NULL,
    nm_cidadao     VARCHAR2(100)  NOT NULL,
    nr_cpf         VARCHAR2(11)   NOT NULL,
    ds_email       VARCHAR2(100),
    nr_telefone    VARCHAR2(11)   NOT NULL,
    dt_nascimento  DATE           NOT NULL,
    nm_bairro      VARCHAR2(60)   NOT NULL,
    CONSTRAINT pk_cidadao PRIMARY KEY (cd_cidadao),
    CONSTRAINT uk_cidadao_cpf UNIQUE (nr_cpf),
    CONSTRAINT uk_cidadao_email UNIQUE (ds_email),
    CONSTRAINT ck_cidadao_cpf CHECK (REGEXP_LIKE(nr_cpf, '^[0-9]{11}$'))
);
COMMENT ON TABLE tb_cidadao IS 'Cidadãos que usam o Fila Zero para agendar atendimentos. CPF só com números.';

CREATE SEQUENCE sq_tb_cidadao START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- tb_unidade_servico: tabela associativa do N:N (quais serviços cada unidade oferece)
-- ---------------------------------------------------------------------
CREATE TABLE tb_unidade_servico (
    cd_unidade  NUMBER(10)  NOT NULL,
    cd_servico  NUMBER(10)  NOT NULL,
    fl_ativo    CHAR(1)     DEFAULT 'S' NOT NULL,
    CONSTRAINT pk_unidade_servico PRIMARY KEY (cd_unidade, cd_servico),
    CONSTRAINT fk_us_unidade FOREIGN KEY (cd_unidade) REFERENCES tb_unidade (cd_unidade),
    CONSTRAINT fk_us_servico FOREIGN KEY (cd_servico) REFERENCES tb_servico (cd_servico),
    CONSTRAINT ck_us_ativo CHECK (fl_ativo IN ('S', 'N'))
);
COMMENT ON TABLE tb_unidade_servico IS 'Associativa do relacionamento N:N entre unidade e serviço: indica quais serviços cada unidade oferece.';

-- ---------------------------------------------------------------------
-- tb_atendente: funcionários das unidades (1:N com tb_unidade)
-- ---------------------------------------------------------------------
CREATE TABLE tb_atendente (
    cd_atendente  NUMBER(10)     NOT NULL,
    cd_unidade    NUMBER(10)     NOT NULL,
    nm_atendente  VARCHAR2(100)  NOT NULL,
    nr_matricula  VARCHAR2(10)   NOT NULL,
    fl_ativo      CHAR(1)        DEFAULT 'S' NOT NULL,
    CONSTRAINT pk_atendente PRIMARY KEY (cd_atendente),
    CONSTRAINT uk_atendente_matricula UNIQUE (nr_matricula),
    CONSTRAINT fk_atendente_unidade FOREIGN KEY (cd_unidade) REFERENCES tb_unidade (cd_unidade),
    CONSTRAINT ck_atendente_ativo CHECK (fl_ativo IN ('S', 'N'))
);
COMMENT ON TABLE tb_atendente IS 'Atendentes que trabalham em uma unidade e registram atendimentos e faltas.';

CREATE SEQUENCE sq_tb_atendente START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- tb_agendamento: horários marcados (1:N com cidadão, unidade, serviço e atendente)
-- ---------------------------------------------------------------------
CREATE TABLE tb_agendamento (
    cd_agendamento  NUMBER(10)    NOT NULL,
    cd_cidadao      NUMBER(10)    NOT NULL,
    cd_unidade      NUMBER(10)    NOT NULL,
    cd_servico      NUMBER(10)    NOT NULL,
    cd_atendente    NUMBER(10),
    dt_agendamento  DATE          NOT NULL,
    dt_criacao      DATE          DEFAULT SYSDATE NOT NULL,
    st_agendamento  VARCHAR2(10)  DEFAULT 'AGENDADO' NOT NULL,
    nr_espera_min   NUMBER(4),
    CONSTRAINT pk_agendamento PRIMARY KEY (cd_agendamento),
    CONSTRAINT fk_agend_cidadao FOREIGN KEY (cd_cidadao) REFERENCES tb_cidadao (cd_cidadao),
    CONSTRAINT fk_agend_unidade FOREIGN KEY (cd_unidade) REFERENCES tb_unidade (cd_unidade),
    CONSTRAINT fk_agend_servico FOREIGN KEY (cd_servico) REFERENCES tb_servico (cd_servico),
    CONSTRAINT fk_agend_atendente FOREIGN KEY (cd_atendente) REFERENCES tb_atendente (cd_atendente),
    CONSTRAINT ck_agend_status CHECK (st_agendamento IN ('AGENDADO', 'CONFIRMADO', 'CANCELADO', 'ATENDIDO', 'FALTOU')),
    CONSTRAINT ck_agend_espera CHECK (nr_espera_min >= 0)
);
COMMENT ON TABLE tb_agendamento IS 'Agendamentos feitos pelos cidadãos. cd_atendente e nr_espera_min são preenchidos quando o atendimento acontece.';

CREATE SEQUENCE sq_tb_agendamento START WITH 1 INCREMENT BY 1 NOCACHE;


-- =====================================================================
-- 2. INSERTS - 60 registros no total
-- 5 unidades + 6 serviços + 12 cidadãos + 15 ofertas + 7 atendentes + 15 agendamentos
-- (endereços, nomes e CPFs são fictícios)
-- =====================================================================

-- ---------- tb_unidade (5) ----------
INSERT INTO tb_unidade VALUES (sq_tb_unidade.NEXTVAL, 'Poupatempo Sé', 'Praça do Carmo, 100', 'Sé', 'São Paulo', 6, 8, 17);
INSERT INTO tb_unidade VALUES (sq_tb_unidade.NEXTVAL, 'Poupatempo Itaquera', 'Av. do Contorno, 60', 'Itaquera', 'São Paulo', 4, 8, 17);
INSERT INTO tb_unidade VALUES (sq_tb_unidade.NEXTVAL, 'Poupatempo Santo Amaro', 'Rua Amador Bueno, 176', 'Santo Amaro', 'São Paulo', 5, 8, 17);
INSERT INTO tb_unidade VALUES (sq_tb_unidade.NEXTVAL, 'Poupatempo Lapa', 'Rua do Curtume, 90', 'Lapa', 'São Paulo', 3, 8, 17);
INSERT INTO tb_unidade VALUES (sq_tb_unidade.NEXTVAL, 'Poupatempo Guarulhos', 'Rua José Campanella, 189', 'Macedo', 'Guarulhos', 3, 9, 16);

-- ---------- tb_servico (6) ----------
INSERT INTO tb_servico VALUES (sq_tb_servico.NEXTVAL, 'Emissão de RG', 'Primeira via ou nova via da Carteira de Identidade', 'Certidão de nascimento ou casamento e CPF', 20);
INSERT INTO tb_servico VALUES (sq_tb_servico.NEXTVAL, 'Renovação de CNH', 'Renovação da Carteira Nacional de Habilitação vencida', 'CNH atual e comprovante de residência', 30);
INSERT INTO tb_servico VALUES (sq_tb_servico.NEXTVAL, 'Segunda via de CNH', 'Nova via em caso de perda, roubo ou dano', 'RG, CPF e boletim de ocorrência (perda ou roubo)', 25);
INSERT INTO tb_servico VALUES (sq_tb_servico.NEXTVAL, 'Carteira de Trabalho', 'Orientação e emissão da Carteira de Trabalho Digital', 'RG, CPF e comprovante de residência', 15);
INSERT INTO tb_servico VALUES (sq_tb_servico.NEXTVAL, 'Atestado de Antecedentes', 'Atestado de antecedentes criminais', 'RG', 10);
INSERT INTO tb_servico VALUES (sq_tb_servico.NEXTVAL, 'Licenciamento de Veículo', 'Emissão do licenciamento anual do veículo', 'CRLV anterior e documento do proprietário', 15);

-- ---------- tb_cidadao (12) ----------
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Maria Souza', '12345678901', 'maria.souza@email.com', '11987654321', TO_DATE('1985-04-12', 'YYYY-MM-DD'), 'Itaquera');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'João Pereira', '23456789012', 'joao.pereira@email.com', '11976543210', TO_DATE('1992-08-25', 'YYYY-MM-DD'), 'Mooca');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Ana Lima', '34567890123', 'ana.lima@email.com', '11965432109', TO_DATE('2001-01-30', 'YYYY-MM-DD'), 'Guaianases');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Carlos Oliveira', '45678901234', 'carlos.oliveira@email.com', '11954321098', TO_DATE('1978-11-03', 'YYYY-MM-DD'), 'Itaquera');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Fernanda Costa', '56789012345', 'fernanda.costa@email.com', '11943210987', TO_DATE('1995-06-18', 'YYYY-MM-DD'), 'São Mateus');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Ricardo Alves', '67890123456', NULL, '11932109876', TO_DATE('1960-02-09', 'YYYY-MM-DD'), 'Penha');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Juliana Martins', '78901234567', 'juliana.martins@email.com', '11921098765', TO_DATE('1999-09-14', 'YYYY-MM-DD'), 'Santo Amaro');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Pedro Santos', '89012345678', 'pedro.santos@email.com', '11910987654', TO_DATE('1988-12-01', 'YYYY-MM-DD'), 'Jabaquara');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Luciana Rocha', '90123456789', 'luciana.rocha@email.com', '11998765432', TO_DATE('1972-03-27', 'YYYY-MM-DD'), 'Lapa');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Bruno Ferreira', '01234567890', 'bruno.ferreira@email.com', '11987651234', TO_DATE('2003-07-05', 'YYYY-MM-DD'), 'Bela Vista');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Patrícia Gomes', '11223344556', 'patricia.gomes@email.com', '11976512345', TO_DATE('1983-10-22', 'YYYY-MM-DD'), 'Itaquera');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Marcos Ribeiro', '22334455667', NULL, '11965123456', TO_DATE('1955-05-16', 'YYYY-MM-DD'), 'Pinheiros');

-- ---------- tb_unidade_servico (15) ----------
-- Sé (1): RG, Renovação CNH, 2ª via CNH, Carteira de Trabalho, Antecedentes
INSERT INTO tb_unidade_servico VALUES (1, 1, 'S');
INSERT INTO tb_unidade_servico VALUES (1, 2, 'S');
INSERT INTO tb_unidade_servico VALUES (1, 3, 'S');
INSERT INTO tb_unidade_servico VALUES (1, 4, 'S');
INSERT INTO tb_unidade_servico VALUES (1, 5, 'S');
-- Itaquera (2): RG, Renovação CNH, Carteira de Trabalho
INSERT INTO tb_unidade_servico VALUES (2, 1, 'S');
INSERT INTO tb_unidade_servico VALUES (2, 2, 'S');
INSERT INTO tb_unidade_servico VALUES (2, 4, 'S');
-- Santo Amaro (3): RG, Renovação CNH, 2ª via CNH, Antecedentes
INSERT INTO tb_unidade_servico VALUES (3, 1, 'S');
INSERT INTO tb_unidade_servico VALUES (3, 2, 'S');
INSERT INTO tb_unidade_servico VALUES (3, 3, 'S');
INSERT INTO tb_unidade_servico VALUES (3, 5, 'S');
-- Lapa (4): RG e Carteira de Trabalho (esta desativada)
INSERT INTO tb_unidade_servico VALUES (4, 1, 'S');
INSERT INTO tb_unidade_servico VALUES (4, 4, 'N');
-- Guarulhos (5): RG
INSERT INTO tb_unidade_servico VALUES (5, 1, 'S');
-- Licenciamento de Veículo (6) não é oferecido em nenhuma unidade (aparece na consulta 8)

-- ---------- tb_atendente (7) ----------
INSERT INTO tb_atendente VALUES (sq_tb_atendente.NEXTVAL, 1, 'Sandra Nogueira', 'AT001', 'S');
INSERT INTO tb_atendente VALUES (sq_tb_atendente.NEXTVAL, 1, 'Roberto Dias', 'AT002', 'S');
INSERT INTO tb_atendente VALUES (sq_tb_atendente.NEXTVAL, 2, 'Camila Freitas', 'AT003', 'S');
INSERT INTO tb_atendente VALUES (sq_tb_atendente.NEXTVAL, 2, 'Eduardo Pires', 'AT004', 'S');
INSERT INTO tb_atendente VALUES (sq_tb_atendente.NEXTVAL, 3, 'Vanessa Moura', 'AT005', 'S');
INSERT INTO tb_atendente VALUES (sq_tb_atendente.NEXTVAL, 4, 'Thiago Barros', 'AT006', 'S');
INSERT INTO tb_atendente VALUES (sq_tb_atendente.NEXTVAL, 5, 'Renata Cardoso', 'AT007', 'N');

-- ---------- tb_agendamento (15) ----------
-- Colunas: cd, cidadao, unidade, servico, atendente, dt_agendamento, dt_criacao, status, espera
-- Passados (já aconteceram): ATENDIDO, FALTOU, CANCELADO
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 1, 1, 1, 1, TO_DATE('2026-11-03 09:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-28 10:15', 'YYYY-MM-DD HH24:MI'), 'ATENDIDO', 45);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 2, 1, 2, 2, TO_DATE('2026-11-03 10:30', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-29 18:40', 'YYYY-MM-DD HH24:MI'), 'ATENDIDO', 70);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 3, 1, 4, NULL, TO_DATE('2026-11-03 14:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-30 08:05', 'YYYY-MM-DD HH24:MI'), 'FALTOU', NULL);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 4, 2, 1, 3, TO_DATE('2026-11-04 08:30', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-31 12:20', 'YYYY-MM-DD HH24:MI'), 'ATENDIDO', 20);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 5, 2, 2, NULL, TO_DATE('2026-11-04 11:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-01 21:10', 'YYYY-MM-DD HH24:MI'), 'FALTOU', NULL);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 6, 2, 4, 4, TO_DATE('2026-11-05 15:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-02 09:45', 'YYYY-MM-DD HH24:MI'), 'ATENDIDO', 15);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 7, 3, 3, 5, TO_DATE('2026-11-05 09:30', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-01 14:30', 'YYYY-MM-DD HH24:MI'), 'ATENDIDO', 35);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 8, 3, 5, NULL, TO_DATE('2026-11-06 13:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-03 16:00', 'YYYY-MM-DD HH24:MI'), 'CANCELADO', NULL);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 3, 3, 1, NULL, TO_DATE('2026-11-06 10:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-04 07:50', 'YYYY-MM-DD HH24:MI'), 'FALTOU', NULL);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 9, 4, 1, 6, TO_DATE('2026-11-07 08:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-04 19:25', 'YYYY-MM-DD HH24:MI'), 'ATENDIDO', 25);
-- Futuros (ainda vão acontecer): AGENDADO, CONFIRMADO
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 10, 1, 5, NULL, TO_DATE('2026-11-19 09:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-10 11:00', 'YYYY-MM-DD HH24:MI'), 'AGENDADO', NULL);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 11, 2, 1, NULL, TO_DATE('2026-11-19 14:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-10 15:35', 'YYYY-MM-DD HH24:MI'), 'CONFIRMADO', NULL);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 1, 3, 2, NULL, TO_DATE('2026-11-21 10:30', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-11 08:20', 'YYYY-MM-DD HH24:MI'), 'AGENDADO', NULL);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 12, 4, 1, NULL, TO_DATE('2026-11-23 08:30', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-11 17:10', 'YYYY-MM-DD HH24:MI'), 'AGENDADO', NULL);
INSERT INTO tb_agendamento VALUES (sq_tb_agendamento.NEXTVAL, 2, 1, 3, NULL, TO_DATE('2026-11-24 16:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-12 09:00', 'YYYY-MM-DD HH24:MI'), 'AGENDADO', NULL);

COMMIT;


-- =====================================================================
-- 3. CONSULTAS (10, numeradas - cada uma responde uma pergunta do Fila Zero)
-- =====================================================================

-- CONSULTA 1 - Quais agendamentos o Poupatempo Sé teve no dia 03/11/2026, em ordem de horário?
-- Recursos: WHERE, ORDER BY
SELECT cd_agendamento,
       TO_CHAR(dt_agendamento, 'DD/MM/YYYY HH24:MI') AS horario,
       st_agendamento
  FROM tb_agendamento
 WHERE cd_unidade = 1
   AND TRUNC(dt_agendamento) = TO_DATE('2026-11-03', 'YYYY-MM-DD')
 ORDER BY dt_agendamento;

-- CONSULTA 2 - Quantos agendamentos existem em cada status?
-- Recursos: COUNT, GROUP BY, ORDER BY
SELECT st_agendamento,
       COUNT(*) AS quantidade
  FROM tb_agendamento
 GROUP BY st_agendamento
 ORDER BY quantidade DESC;

-- CONSULTA 3 - Qual a espera média, máxima e mínima (em minutos) em cada unidade?
-- Recursos: AVG, MAX, MIN, GROUP BY, INNER JOIN
SELECT u.nm_unidade,
       ROUND(AVG(a.nr_espera_min), 1) AS espera_media,
       MAX(a.nr_espera_min)           AS espera_maxima,
       MIN(a.nr_espera_min)           AS espera_minima
  FROM tb_agendamento a
 INNER JOIN tb_unidade u ON u.cd_unidade = a.cd_unidade
 WHERE a.nr_espera_min IS NOT NULL
 GROUP BY u.nm_unidade
 ORDER BY espera_media DESC;

-- CONSULTA 4 - Quais unidades têm taxa de falta acima de 20%?
-- Recursos: SUM, COUNT, cálculo no SELECT, GROUP BY, HAVING
SELECT u.nm_unidade,
       COUNT(*) AS total_atendimentos_previstos,
       SUM(CASE WHEN a.st_agendamento = 'FALTOU' THEN 1 ELSE 0 END) AS total_faltas,
       ROUND(SUM(CASE WHEN a.st_agendamento = 'FALTOU' THEN 1 ELSE 0 END) * 100 / COUNT(*), 1) AS taxa_falta_pct
  FROM tb_agendamento a
 INNER JOIN tb_unidade u ON u.cd_unidade = a.cd_unidade
 WHERE a.st_agendamento IN ('ATENDIDO', 'FALTOU')
 GROUP BY u.nm_unidade
HAVING SUM(CASE WHEN a.st_agendamento = 'FALTOU' THEN 1 ELSE 0 END) * 100 / COUNT(*) > 20
 ORDER BY taxa_falta_pct DESC;

-- CONSULTA 5 - Quais cidadãos fizeram mais agendamentos que a média geral?
-- Recursos: SUBCONSULTA (SUBSELECT), COUNT, AVG, GROUP BY, HAVING
SELECT c.nm_cidadao,
       COUNT(a.cd_agendamento) AS qtd_agendamentos
  FROM tb_cidadao c
 INNER JOIN tb_agendamento a ON a.cd_cidadao = c.cd_cidadao
 GROUP BY c.cd_cidadao, c.nm_cidadao
HAVING COUNT(a.cd_agendamento) > (SELECT AVG(qtd)
                                    FROM (SELECT COUNT(*) AS qtd
                                            FROM tb_agendamento
                                           GROUP BY cd_cidadao))
 ORDER BY qtd_agendamentos DESC;

-- CONSULTA 6 - Lista completa de agendamentos com o nome do cidadão, da unidade e do serviço
-- Recursos: INNER JOIN (4 tabelas), ORDER BY
SELECT a.cd_agendamento,
       c.nm_cidadao,
       u.nm_unidade,
       s.nm_servico,
       TO_CHAR(a.dt_agendamento, 'DD/MM/YYYY HH24:MI') AS horario,
       a.st_agendamento
  FROM tb_agendamento a
 INNER JOIN tb_cidadao c ON c.cd_cidadao = a.cd_cidadao
 INNER JOIN tb_unidade u ON u.cd_unidade = a.cd_unidade
 INNER JOIN tb_servico s ON s.cd_servico = a.cd_servico
 ORDER BY a.dt_agendamento;

-- CONSULTA 7 - Quantos agendamentos cada unidade recebeu, incluindo unidades sem nenhum?
-- Recursos: LEFT JOIN, COUNT, GROUP BY
SELECT u.nm_unidade,
       COUNT(a.cd_agendamento) AS total_agendamentos
  FROM tb_unidade u
  LEFT JOIN tb_agendamento a ON a.cd_unidade = u.cd_unidade
 GROUP BY u.nm_unidade
 ORDER BY total_agendamentos DESC;

-- CONSULTA 8 - Quais unidades oferecem cada serviço, incluindo serviços que nenhuma unidade oferece?
-- Recursos: RIGHT JOIN, INNER JOIN, ORDER BY
SELECT s.nm_servico,
       u.nm_unidade,
       us.fl_ativo
  FROM tb_unidade_servico us
 INNER JOIN tb_unidade u ON u.cd_unidade = us.cd_unidade
 RIGHT JOIN tb_servico s ON s.cd_servico = us.cd_servico
 ORDER BY s.nm_servico, u.nm_unidade;

-- CONSULTA 9 - Qual a idade de cada cidadão e quantas faltas ele tem?
-- Recursos: cálculo no SELECT (idade), SUM com CASE, INNER JOIN, GROUP BY
SELECT c.nm_cidadao,
       TRUNC(MONTHS_BETWEEN(SYSDATE, c.dt_nascimento) / 12) AS idade,
       SUM(CASE WHEN a.st_agendamento = 'FALTOU' THEN 1 ELSE 0 END) AS total_faltas
  FROM tb_cidadao c
 INNER JOIN tb_agendamento a ON a.cd_cidadao = c.cd_cidadao
 GROUP BY c.cd_cidadao, c.nm_cidadao, c.dt_nascimento
 ORDER BY total_faltas DESC, c.nm_cidadao;

-- CONSULTA 10 - Quanto tempo de atendimento (em minutos) cada unidade tem previsto para 19/11/2026?
-- Recursos: SUM, INNER JOIN, WHERE, GROUP BY
SELECT u.nm_unidade,
       COUNT(*)              AS agendamentos_no_dia,
       SUM(s.nr_duracao_min) AS minutos_previstos
  FROM tb_agendamento a
 INNER JOIN tb_unidade u ON u.cd_unidade = a.cd_unidade
 INNER JOIN tb_servico s ON s.cd_servico = a.cd_servico
 WHERE TRUNC(a.dt_agendamento) = TO_DATE('2026-11-19', 'YYYY-MM-DD')
   AND a.st_agendamento IN ('AGENDADO', 'CONFIRMADO')
 GROUP BY u.nm_unidade
 ORDER BY minutos_previstos DESC;


-- =====================================================================
-- 4. UPDATE E DELETE (ambos com WHERE)
-- =====================================================================

-- UPDATE - A cidadã Maria Souza confirmou o agendamento 13 na véspera
UPDATE tb_agendamento
   SET st_agendamento = 'CONFIRMADO'
 WHERE cd_agendamento = 13;

-- Conferindo o UPDATE
SELECT cd_agendamento, st_agendamento FROM tb_agendamento WHERE cd_agendamento = 13;

-- DELETE - Remove agendamentos cancelados que já passaram (limpeza da agenda)
DELETE FROM tb_agendamento
 WHERE st_agendamento = 'CANCELADO'
   AND dt_agendamento < SYSDATE;

-- Conferindo o DELETE
SELECT COUNT(*) AS cancelados_restantes FROM tb_agendamento WHERE st_agendamento = 'CANCELADO';

COMMIT;
