-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema elastest-etm
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `elastest-etm` ;

-- -----------------------------------------------------
-- Schema elastest-etm
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `elastest-etm` DEFAULT CHARACTER SET utf8 ;
USE `elastest-etm` ;

-- -----------------------------------------------------
-- Table `elastest-etm`.`ELAS_ETM_TJOB`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `elastest-etm`.`ELAS_ETM_TJOB` (
  `ELAS_ETM_TJOB_ID` BIGINT(20) NOT NULL,
  `ELAS_ETM_TJOB_NAME` VARCHAR(45) NULL,
  `ELAS_ETM_TJOB_TSERV` INT NULL,
  `ELAS_ETM_TJOB_IMNAME` VARCHAR(45) NULL,
  `ELAS_ETM_TJOB_SUT` INT NULL,
  PRIMARY KEY (`ELAS_ETM_TJOB_ID`),
  UNIQUE INDEX `ELAS_ETM_TJOB_ID_UNIQUE` (`ELAS_ETM_TJOB_ID` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `elastest-etm`.`ELAS_ETM_TJOBEXEC`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `elastest-etm`.`ELAS_ETM_TJOBEXEC` (
  `ELAS_ETM_TJOBEXEC_ID` BIGINT(20) NOT NULL,
  `ELAS_ETM_TJOBEXEC_RESULT` VARCHAR(45) NULL,
  `ELAS_ETM_TJOBEXEC_DURATION` FLOAT NULL,
  `ELAS_ETM_TJOBEXEC_SUT_EXEC` INT NULL,
  `ELAS_ETM_TJOBEXEC_ERROR_EXEC` VARCHAR(150) NULL,
  `ELAS_ETM_TJOBEXEC_LOGS` INT NULL,
  `ELAS_ETM_TJOBEXEC_TJOB` BIGINT(20) NULL,
  PRIMARY KEY (`ELAS_ETM_TJOBEXEC_ID`),
  INDEX `FK_TJOB_idx` (`ELAS_ETM_TJOBEXEC_TJOB` ASC),
  UNIQUE INDEX `ELAS_ETM_TJOBEXEC_ID_UNIQUE` (`ELAS_ETM_TJOBEXEC_ID` ASC),
  CONSTRAINT `FK_TJOB`
    FOREIGN KEY (`ELAS_ETM_TJOBEXEC_TJOB`)
    REFERENCES `elastest-etm`.`ELAS_ETM_TJOB` (`ELAS_ETM_TJOB_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
