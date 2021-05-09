-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 09-Maio-2021 às 03:04
-- Versão do servidor: 10.4.18-MariaDB
-- versão do PHP: 8.0.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `projetosid`
--

DELIMITER $$
--
-- Procedimentos
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `alterar_cultura` (IN `id` INT, IN `nome` VARCHAR(50) CHARSET utf8, IN `estado` INT, IN `idzona` INT, IN `min_t` DOUBLE, IN `max_t` DOUBLE, IN `min_h` DOUBLE, IN `max_h` DOUBLE, IN `min_l` DOUBLE, IN `max_l` DOUBLE)  NO SQL
BEGIN
	IF NOT(estado=0 OR estado=1) THEN
    	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid state! (only 0 or 1 are allowed)';
    ELSEIF min_t>max_t OR min_h>max_h OR min_l>max_l THEN
    	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid intervals! (min must be less than max)';
    ELSEIF NOT EXISTS(SELECT * FROM zona WHERE zona.idzona=idzona) THEN
    	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'There is no zone with that id!';
    ELSE
		UPDATE cultura
    	SET nome = nome, estado = estado
		WHERE idcultura = id;
    	
    	UPDATE parametrocultura
    	SET idzona = idzona, min_t = min_t, max_t = max_t, min_h = min_h, max_h = max_h, min_l = min_l, max_l = max_l
		WHERE idcultura = id;
	END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `alterar_utilizador` (IN `id` INT, IN `nome` VARCHAR(100) CHARSET utf8, IN `email` VARCHAR(50) CHARSET utf8)  NO SQL
BEGIN
	SET @nomeantigo = (SELECT utilizador.nome FROM utilizador WHERE idutilizador=id);

	UPDATE utilizador
    SET nome = nome, email = email
	WHERE idutilizador = id;

    SET @renameuser = CONCAT('RENAME USER "',@nomeantigo,'"@"localhost" TO "',nome,'"@"localhost" ');
    PREPARE renameuser FROM @renameuser; 
    EXECUTE renameuser; DEALLOCATE PREPARE renameuser;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `atribuir_cultura_investigador` (IN `id_c` INT, IN `id_u` INT)  NO SQL
BEGIN
	SET @tipo = (SELECT utilizador.tipo FROM utilizador WHERE idutilizador = id_u);
    IF NOT @tipo='i' THEN
    	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The user must be an "investigador"!';
    ELSE
		UPDATE cultura SET idutilizador = id_u 
   		WHERE idcultura = id_c;
	END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `criar_cultura` (IN `nome` VARCHAR(50) CHARSET utf8, IN `estado` INT, IN `idzona` INT, IN `min_t` DOUBLE, IN `max_t` DOUBLE, IN `min_h` DOUBLE, IN `max_h` DOUBLE, IN `min_l` DOUBLE, IN `max_l` DOUBLE)  NO SQL
BEGIN
	IF NOT(estado=0 OR estado=1) THEN
    	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid state! (only 0 or 1 are allowed)';
    ELSEIF min_t>max_t OR min_h>max_h OR min_l>max_l THEN
    	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid intervals! (min must be less than max)';
    ELSEIF NOT EXISTS(SELECT * FROM zona WHERE zona.idzona=idzona) THEN
    	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'There is no zone with that id!';
    ELSE
		INSERT INTO cultura(nome,estado) 
    	VALUES(nome,estado);
    	
        SET @lastid = (SELECT MAX(idcultura) FROM cultura);
    	INSERT INTO parametrocultura
    	VALUES(@lastid,idzona,min_t,max_t,
               min_h,max_h,min_l,max_l);
	END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `criar_utilizador` (IN `nome` VARCHAR(100) CHARSET utf8, IN `email` VARCHAR(50) CHARSET utf8, IN `tipo` VARCHAR(1) CHARSET utf8, IN `pass` CHAR(60) CHARSET utf8)  NO SQL
BEGIN
	DECLARE role VARCHAR(30) CHARACTER SET utf8;

	IF NOT(tipo='a' OR tipo='i' OR tipo='t') THEN
    	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid type! (only "a","i","t" are allowed)';
	ELSEIF tipo='a' AND EXISTS(SELECT utilizador.tipo FROM utilizador WHERE utilizador.tipo=tipo) THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'There is already an admin!';
	ELSE    
    	INSERT INTO utilizador(nome, email, tipo)
   		VALUES(nome, email, tipo);
    
    	SET @createuser = CONCAT('CREATE USER "',nome,'"@"localhost" IDENTIFIED BY "',pass,'" ');
    	PREPARE createuser FROM @createuser; 
    	EXECUTE createuser; DEALLOCATE PREPARE createuser;
    
    	IF tipo='i' THEN
    		SET @role = 'investigador';
    	ELSEIF tipo='a' THEN
    		SET @role = 'admin';
    	ELSEIF tipo='t' THEN
    		SET @role = 'tecnico';
		END IF;
    	
    	SET @grantrole = CONCAT('GRANT ',@role,' TO "',nome,'"@"localhost" ');
    	PREPARE grantrole FROM @grantrole; 
    	EXECUTE grantrole; DEALLOCATE PREPARE grantrole;
    	
    	SET @setrole = CONCAT('SET DEFAULT ROLE ',@role,' FOR "',nome,'"@"localhost" ');
    	PREPARE setrole FROM @setrole; 
    	EXECUTE setrole; DEALLOCATE PREPARE setrole;
	END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `remover_cultura` (IN `id` INT)  NO SQL
BEGIN
	DELETE FROM cultura WHERE idcultura = id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `remover_utilizador` (IN `id` INT)  NO SQL
BEGIN
	SET @nome = (SELECT utilizador.nome FROM utilizador WHERE idutilizador = id);
    SET @dropuser = CONCAT('DROP USER "',@nome,'"@"localhost" ');
    PREPARE dropuser FROM @dropuser; 
    EXECUTE dropuser; DEALLOCATE PREPARE dropuser;
    
	DELETE FROM utilizador WHERE idutilizador = id;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `alerta`
--

CREATE TABLE `alerta` (
  `idalerta` int(11) NOT NULL,
  `zona` varchar(10) NOT NULL,
  `sensor` varchar(10) NOT NULL,
  `hora` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `leitura` decimal(5,2) NOT NULL,
  `tipo` varchar(1) NOT NULL,
  `cultura` varchar(50) NOT NULL,
  `mensagem` varchar(150) NOT NULL,
  `idutilizador` int(11) DEFAULT NULL,
  `idcultura` int(11) NOT NULL,
  `horaescrita` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Acionadores `alerta`
--
DELIMITER $$
CREATE TRIGGER `complete_alert` BEFORE INSERT ON `alerta` FOR EACH ROW BEGIN
	SET new.idutilizador = (
        	SELECT idutilizador 
   			FROM cultura
			WHERE cultura.idcultura = new.idcultura),
		new.cultura = (
        	SELECT nome
            FROM cultura
			WHERE cultura.idcultura = new.idcultura);
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `cultura`
--

CREATE TABLE `cultura` (
  `idcultura` int(11) NOT NULL,
  `nome` varchar(50) NOT NULL,
  `estado` int(11) NOT NULL,
  `idutilizador` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Extraindo dados da tabela `cultura`
--

INSERT INTO `cultura` (`idcultura`, `nome`, `estado`, `idutilizador`) VALUES
(1, 'batatas', 1, 2),
(2, 'morangos', 0, NULL),
(3, 'tomates', 1, 2);

-- --------------------------------------------------------

--
-- Estrutura da tabela `medicao`
--

CREATE TABLE `medicao` (
  `idmedicao` int(11) NOT NULL,
  `zona` varchar(10) NOT NULL,
  `sensor` varchar(10) NOT NULL,
  `hora` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `leitura` decimal(5,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Estrutura da tabela `parametrocultura`
--

CREATE TABLE `parametrocultura` (
  `idcultura` int(11) NOT NULL,
  `idzona` int(11) NOT NULL,
  `min_t` double NOT NULL,
  `max_t` double NOT NULL,
  `min_h` double NOT NULL,
  `max_h` double NOT NULL,
  `min_l` double NOT NULL,
  `max_l` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Extraindo dados da tabela `parametrocultura`
--

INSERT INTO `parametrocultura` (`idcultura`, `idzona`, `min_t`, `max_t`, `min_h`, `max_h`, `min_l`, `max_l`) VALUES
(1, 1, 0, 10, 0, 10, 0, 10),
(2, 2, 0, 10, 0, 10, 0, 10),
(3, 2, 0, 10, 0, 10, 0, 10);

-- --------------------------------------------------------

--
-- Estrutura da tabela `sensor`
--

CREATE TABLE `sensor` (
  `idsensor` int(11) NOT NULL,
  `tipo` varchar(1) NOT NULL,
  `limiteinferior` decimal(5,2) NOT NULL,
  `limitesuperior` decimal(5,2) NOT NULL,
  `idzona` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Extraindo dados da tabela `sensor`
--

INSERT INTO `sensor` (`idsensor`, `tipo`, `limiteinferior`, `limitesuperior`, `idzona`) VALUES
(1, 'H', '0.00', '100.00', 1),
(1, 'L', '0.00', '300.00', 1),
(1, 'T', '2.00', '50.00', 1),
(2, 'H', '0.00', '10.00', 2),
(2, 'L', '0.00', '300.00', 2),
(2, 'T', '2.00', '50.00', 2);

-- --------------------------------------------------------

--
-- Estrutura da tabela `utilizador`
--

CREATE TABLE `utilizador` (
  `idutilizador` int(11) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `email` varchar(50) NOT NULL,
  `tipo` varchar(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Extraindo dados da tabela `utilizador`
--

INSERT INTO `utilizador` (`idutilizador`, `nome`, `email`, `tipo`) VALUES
(1, 'faizan', 'simaosaojose@gmail.com', 'a'),
(2, 'essertgod', 'pedroesser@gmail.com', 'i'),
(3, 'vascopl', 'davidrodrigues@gmail.com', 't');

-- --------------------------------------------------------

--
-- Estrutura da tabela `zona`
--

CREATE TABLE `zona` (
  `idzona` int(11) NOT NULL,
  `temperatura` decimal(5,2) NOT NULL,
  `humidade` decimal(5,2) NOT NULL,
  `luz` decimal(5,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Extraindo dados da tabela `zona`
--

INSERT INTO `zona` (`idzona`, `temperatura`, `humidade`, `luz`) VALUES
(1, '12.00', '20.00', '10.00'),
(2, '13.00', '30.00', '20.00');

--
-- Índices para tabelas despejadas
--

--
-- Índices para tabela `alerta`
--
ALTER TABLE `alerta`
  ADD PRIMARY KEY (`idalerta`),
  ADD KEY `alerta_ibfk_1` (`idcultura`),
  ADD KEY `alerta_ibfk_2` (`idutilizador`);

--
-- Índices para tabela `cultura`
--
ALTER TABLE `cultura`
  ADD PRIMARY KEY (`idcultura`),
  ADD KEY `cultura_ibfk_1` (`idutilizador`);

--
-- Índices para tabela `medicao`
--
ALTER TABLE `medicao`
  ADD PRIMARY KEY (`idmedicao`);

--
-- Índices para tabela `parametrocultura`
--
ALTER TABLE `parametrocultura`
  ADD PRIMARY KEY (`idcultura`),
  ADD KEY `idzona` (`idzona`);

--
-- Índices para tabela `sensor`
--
ALTER TABLE `sensor`
  ADD PRIMARY KEY (`idsensor`,`tipo`);

--
-- Índices para tabela `utilizador`
--
ALTER TABLE `utilizador`
  ADD PRIMARY KEY (`idutilizador`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `nome` (`nome`);

--
-- Índices para tabela `zona`
--
ALTER TABLE `zona`
  ADD PRIMARY KEY (`idzona`);

--
-- AUTO_INCREMENT de tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `alerta`
--
ALTER TABLE `alerta`
  MODIFY `idalerta` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `cultura`
--
ALTER TABLE `cultura`
  MODIFY `idcultura` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `medicao`
--
ALTER TABLE `medicao`
  MODIFY `idmedicao` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `utilizador`
--
ALTER TABLE `utilizador`
  MODIFY `idutilizador` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Restrições para despejos de tabelas
--

--
-- Limitadores para a tabela `alerta`
--
ALTER TABLE `alerta`
  ADD CONSTRAINT `alerta_ibfk_1` FOREIGN KEY (`idcultura`) REFERENCES `cultura` (`idcultura`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `alerta_ibfk_2` FOREIGN KEY (`idutilizador`) REFERENCES `utilizador` (`idutilizador`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limitadores para a tabela `cultura`
--
ALTER TABLE `cultura`
  ADD CONSTRAINT `cultura_ibfk_1` FOREIGN KEY (`idutilizador`) REFERENCES `utilizador` (`idutilizador`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Limitadores para a tabela `parametrocultura`
--
ALTER TABLE `parametrocultura`
  ADD CONSTRAINT `parametrocultura_ibfk_1` FOREIGN KEY (`idcultura`) REFERENCES `cultura` (`idcultura`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `parametrocultura_ibfk_2` FOREIGN KEY (`idzona`) REFERENCES `zona` (`idzona`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
