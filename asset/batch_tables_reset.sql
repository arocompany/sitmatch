-- 삭제
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_INSTANCE;

-- 시퀀스 삭제 (선택적)
-- DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_SEQ;
-- DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_SEQ;

-- 테이블 생성
CREATE TABLE BATCH_JOB_INSTANCE  (
                                     JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
                                     VERSION BIGINT,
                                     JOB_NAME VARCHAR(100) NOT NULL,
                                     JOB_KEY VARCHAR(32) NOT NULL,
                                     constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION  (
                                      JOB_EXECUTION_ID BIGINT PRIMARY KEY ,
                                      VERSION BIGINT,
                                      JOB_INSTANCE_ID BIGINT NOT NULL,
                                      CREATE_TIME DATETIME NOT NULL,
                                      START_TIME DATETIME DEFAULT NULL,
                                      END_TIME DATETIME DEFAULT NULL,
                                      STATUS VARCHAR(10),
                                      EXIT_CODE VARCHAR(2500),
                                      EXIT_MESSAGE VARCHAR(2500),
                                      LAST_UPDATED DATETIME,
                                      constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
                                          references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE `batch_job_execution_params` (
                                              `JOB_EXECUTION_ID` BIGINT(19) NOT NULL,
                                              `PARAMETER_NAME` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_general_ci',
                                              `PARAMETER_TYPE` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_general_ci',
                                              `PARAMETER_VALUE` VARCHAR(2500) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
                                              `IDENTIFYING` CHAR(1) NOT NULL COLLATE 'utf8mb4_general_ci',
                                              INDEX `JOB_EXEC_PARAMS_FK` (`JOB_EXECUTION_ID`) USING BTREE,
                                              CONSTRAINT `JOB_EXEC_PARAMS_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `batch_job_execution` (`JOB_EXECUTION_ID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
    COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `batch_step_execution` (
                                        `STEP_EXECUTION_ID` BIGINT(19) NOT NULL,
                                        `VERSION` BIGINT(19) NOT NULL,
                                        `STEP_NAME` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_general_ci',
                                        `JOB_EXECUTION_ID` BIGINT(19) NOT NULL,
                                        `CREATE_TIME` DATETIME(6) NOT NULL,
                                        `START_TIME` DATETIME(6) NULL DEFAULT NULL,
                                        `END_TIME` DATETIME(6) NULL DEFAULT NULL,
                                        `STATUS` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
                                        `COMMIT_COUNT` BIGINT(19) NULL DEFAULT NULL,
                                        `READ_COUNT` BIGINT(19) NULL DEFAULT NULL,
                                        `FILTER_COUNT` BIGINT(19) NULL DEFAULT NULL,
                                        `WRITE_COUNT` BIGINT(19) NULL DEFAULT NULL,
                                        `READ_SKIP_COUNT` BIGINT(19) NULL DEFAULT NULL,
                                        `WRITE_SKIP_COUNT` BIGINT(19) NULL DEFAULT NULL,
                                        `PROCESS_SKIP_COUNT` BIGINT(19) NULL DEFAULT NULL,
                                        `ROLLBACK_COUNT` BIGINT(19) NULL DEFAULT NULL,
                                        `EXIT_CODE` VARCHAR(2500) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
                                        `EXIT_MESSAGE` VARCHAR(2500) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
                                        `LAST_UPDATED` DATETIME(6) NULL DEFAULT NULL,
                                        PRIMARY KEY (`STEP_EXECUTION_ID`) USING BTREE,
                                        INDEX `JOB_EXEC_STEP_FK` (`JOB_EXECUTION_ID`) USING BTREE,
                                        CONSTRAINT `JOB_EXEC_STEP_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `batch_job_execution` (`JOB_EXECUTION_ID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
    COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `batch_step_execution_context` (
                                                `STEP_EXECUTION_ID` BIGINT(19) NOT NULL,
                                                `SHORT_CONTEXT` VARCHAR(2500) NOT NULL COLLATE 'utf8mb4_general_ci',
                                                `SERIALIZED_CONTEXT` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
                                                PRIMARY KEY (`STEP_EXECUTION_ID`) USING BTREE,
                                                CONSTRAINT `STEP_EXEC_CTX_FK` FOREIGN KEY (`STEP_EXECUTION_ID`) REFERENCES `batch_step_execution` (`STEP_EXECUTION_ID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
    COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `batch_job_execution_context` (
                                               `JOB_EXECUTION_ID` BIGINT(19) NOT NULL,
                                               `SHORT_CONTEXT` VARCHAR(2500) NOT NULL COLLATE 'utf8mb4_general_ci',
                                               `SERIALIZED_CONTEXT` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
                                               PRIMARY KEY (`JOB_EXECUTION_ID`) USING BTREE,
                                               CONSTRAINT `JOB_EXEC_CTX_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `batch_job_execution` (`JOB_EXECUTION_ID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
    COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `batch_job_seq` (
                                 `ID` BIGINT(19) NOT NULL,
                                 `UNIQUE_KEY` CHAR(1) NOT NULL COLLATE 'utf8mb4_general_ci',
                                 UNIQUE INDEX `UNIQUE_KEY_UN` (`UNIQUE_KEY`) USING BTREE
)
    COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `batch_job_execution_seq` (
                                           `ID` BIGINT(19) NOT NULL,
                                           `UNIQUE_KEY` CHAR(1) NOT NULL COLLATE 'utf8mb4_general_ci',
                                           UNIQUE INDEX `UNIQUE_KEY_UN` (`UNIQUE_KEY`) USING BTREE
)
    COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `batch_step_execution_seq` (
                                            `ID` BIGINT(19) NOT NULL,
                                            `UNIQUE_KEY` CHAR(1) NOT NULL COLLATE 'utf8mb4_general_ci',
                                            UNIQUE INDEX `UNIQUE_KEY_UN` (`UNIQUE_KEY`) USING BTREE
)
    COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;
