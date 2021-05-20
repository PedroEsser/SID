-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 19-Maio-2021 às 13:07
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `alterar_utilizador` (IN `id` INT, IN `nome` VARCHAR(100) CHARSET utf8, IN `email` VARCHAR(50) CHARSET utf8, IN `pass` CHAR(60))  NO SQL
BEGIN
	SET @nomeantigo = (SELECT utilizador.nome FROM utilizador WHERE idutilizador=id);

	IF nome <> '' THEN
		UPDATE utilizador
    	SET nome = nome
		WHERE idutilizador = id;
    END IF;
    
    IF email <> '' THEN
    	UPDATE utilizador
    	SET email = email
		WHERE idutilizador = id;
    END IF;
	
    IF pass <> '' THEN
    	SET @changepass = CONCAT('SET PASSWORD FOR "',@nomeantigo,'"@"localhost" = PASSWORD("',pass,'") ');
    	PREPARE changepass FROM @changepass; 
    	EXECUTE changepass; DEALLOCATE PREPARE changepass;
	END IF;
    
    IF nome <> '' AND @nomeantigo <> nome THEN
    	SET @renameuser = CONCAT('RENAME USER "',@nomeantigo,'"@"localhost" TO "',nome,'"@"localhost" ');
    	PREPARE renameuser FROM @renameuser; 
    	EXECUTE renameuser; DEALLOCATE PREPARE renameuser;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `atribuir_cultura_investigador` (IN `id_c` INT, IN `id_u` INT)  NO SQL
BEGIN
	SET @tipo = (SELECT utilizador.tipo FROM utilizador WHERE idutilizador = id_u);
    IF NOT @tipo='i' THEN
    	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The user must be an "investigador"!';
    ELSE
		UPDATE cultura SET idutilizador = id_u 
   		WHERE idcultura = id_c;
        
        UPDATE alerta SET idutilizador = id_u
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
-- Extraindo dados da tabela `alerta`
--

INSERT INTO `alerta` (`idalerta`, `zona`, `sensor`, `hora`, `leitura`, `tipo`, `cultura`, `mensagem`, `idutilizador`, `idcultura`, `horaescrita`) VALUES
(54, 'Z2', 'L2', '2021-05-18 17:31:05', '10.00', '6', 'tomates', '[Grave] Aproximação ao Limite Superior da Cultura', 4, 3, '2021-05-18 17:31:05'),
(55, 'Z2', 'T2', '2021-05-18 17:31:05', '11.80', '3', 'tomates', '[Muito Grave] Limite Superior da Cultura Excedido', 4, 3, '2021-05-18 17:31:05'),
(56, 'Z2', 'H2', '2021-05-18 17:31:05', '8.80', '7', 'tomates', '[Médio] Aproximação ao Limite Superior da Cultura', 4, 3, '2021-05-18 17:31:05'),
(57, 'Z2', 'H2', '2021-05-18 17:31:24', '11.35', '1', 'tomates', 'Sensor Estragado', 4, 3, '2021-05-18 17:31:24');

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
(1, 'batatas', 0, NULL),
(2, 'morangos', 0, NULL),
(3, 'tomates', 1, 4);

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

--
-- Extraindo dados da tabela `medicao`
--

INSERT INTO `medicao` (`idmedicao`, `zona`, `sensor`, `hora`, `leitura`) VALUES
(1885, 'Z1', 'H1', '2021-05-18 17:31:04', '8.20'),
(1886, 'Z1', 'T1', '2021-05-18 17:31:05', '10.90'),
(1887, 'Z1', 'L1', '2021-05-18 17:31:05', '9.70'),
(1888, 'Z2', 'L2', '2021-05-18 17:31:05', '10.00'),
(1889, 'Z2', 'T2', '2021-05-18 17:31:05', '11.80'),
(1890, 'Z2', 'H2', '2021-05-18 17:31:05', '8.80'),
(1891, 'Z1', 'H1', '2021-05-18 17:31:07', '7.60'),
(1892, 'Z1', 'T1', '2021-05-18 17:31:07', '10.30'),
(1893, 'Z1', 'L1', '2021-05-18 17:31:07', '9.10'),
(1894, 'Z2', 'L2', '2021-05-18 17:31:07', '9.40'),
(1895, 'Z2', 'T2', '2021-05-18 17:31:07', '11.20'),
(1896, 'Z2', 'H2', '2021-05-18 17:31:07', '8.20'),
(1897, 'Z1', 'T1', '2021-05-18 17:31:09', '9.70'),
(1898, 'Z1', 'L1', '2021-05-18 17:31:09', '8.50'),
(1899, 'Z2', 'L2', '2021-05-18 17:31:09', '8.80'),
(1900, 'Z2', 'T2', '2021-05-18 17:31:09', '10.60'),
(1901, 'Z2', 'H2', '2021-05-18 17:31:09', '7.60'),
(1902, 'Z1', 'H1', '2021-05-18 17:31:11', '7.30'),
(1903, 'Z1', 'T1', '2021-05-18 17:31:11', '9.10'),
(1904, 'Z1', 'L1', '2021-05-18 17:31:11', '7.90'),
(1905, 'Z2', 'L2', '2021-05-18 17:31:11', '8.50'),
(1906, 'Z2', 'T2', '2021-05-18 17:31:11', '10.00'),
(1907, 'Z1', 'H1', '2021-05-18 17:31:13', '7.30'),
(1908, 'Z1', 'T1', '2021-05-18 17:31:13', '8.50'),
(1909, 'Z1', 'L1', '2021-05-18 17:31:13', '7.30'),
(1910, 'Z2', 'L2', '2021-05-18 17:31:13', '7.90'),
(1911, 'Z2', 'H2', '2021-05-18 17:31:14', '7.30'),
(1912, 'Z1', 'H1', '2021-05-18 17:31:15', '7.90'),
(1913, 'Z1', 'T1', '2021-05-18 17:31:15', '8.05'),
(1914, 'Z2', 'T2', '2021-05-18 17:31:16', '8.95'),
(1915, 'Z1', 'H1', '2021-05-18 17:31:17', '8.50'),
(1916, 'Z1', 'L1', '2021-05-18 17:31:18', '7.30'),
(1917, 'Z2', 'L2', '2021-05-18 17:31:18', '7.30'),
(1918, 'Z2', 'H2', '2021-05-18 17:31:18', '7.75'),
(1919, 'Z1', 'H1', '2021-05-18 17:31:19', '9.10'),
(1920, 'Z1', 'T1', '2021-05-18 17:31:20', '7.30'),
(1921, 'Z2', 'T2', '2021-05-18 17:31:20', '7.75'),
(1922, 'Z1', 'L1', '2021-05-18 17:31:20', '7.60'),
(1923, 'Z2', 'H2', '2021-05-18 17:31:20', '8.80'),
(1924, 'Z1', 'H1', '2021-05-18 17:31:21', '9.70'),
(1925, 'Z2', 'L2', '2021-05-18 17:31:22', '7.45'),
(1926, 'Z2', 'T2', '2021-05-18 17:31:22', '7.30'),
(1927, 'Z1', 'L1', '2021-05-18 17:31:22', '8.20'),
(1928, 'Z2', 'H2', '2021-05-18 17:31:22', '9.40'),
(1929, 'Z1', 'H1', '2021-05-18 17:31:23', '13.00'),
(1930, 'Z1', 'T1', '2021-05-18 17:31:24', '7.30'),
(1931, 'Z2', 'L2', '2021-05-18 17:31:24', '8.50'),
(1932, 'Z2', 'T2', '2021-05-18 17:31:24', '7.30'),
(1933, 'Z1', 'L1', '2021-05-18 17:31:24', '8.80'),
(1934, 'Z2', 'H2', '2021-05-18 17:31:24', '11.35'),
(1935, 'Z2', 'L2', '2021-05-18 17:31:26', '9.10'),
(1936, 'Z2', 'T2', '2021-05-18 17:31:26', '7.30'),
(1937, 'Z1', 'L1', '2021-05-18 17:31:26', '9.40'),
(1938, 'Z2', 'H2', '2021-05-18 17:31:26', '11.50'),
(1939, 'Z1', 'H1', '2021-05-18 17:31:28', '10.75'),
(1940, 'Z1', 'T1', '2021-05-18 17:31:28', '8.35'),
(1941, 'Z2', 'L2', '2021-05-18 17:31:28', '9.70'),
(1942, 'Z2', 'T2', '2021-05-18 17:31:29', '7.90'),
(1943, 'Z1', 'H1', '2021-05-18 17:31:30', '11.50'),
(1944, 'Z1', 'T1', '2021-05-18 17:31:30', '9.25'),
(1945, 'Z2', 'L2', '2021-05-18 17:31:30', '13.00'),
(1946, 'Z2', 'H2', '2021-05-18 17:31:31', '10.75'),
(1947, 'Z1', 'L1', '2021-05-18 17:31:31', '11.50'),
(1948, 'Z1', 'T1', '2021-05-18 17:31:32', '11.35'),
(1949, 'Z2', 'L2', '2021-05-18 17:31:32', '10.00'),
(1950, 'Z2', 'T2', '2021-05-18 17:31:33', '8.95'),
(1951, 'Z1', 'H1', '2021-05-18 17:31:34', '12.25'),
(1952, 'Z1', 'T1', '2021-05-18 17:31:34', '11.50'),
(1953, 'Z2', 'L2', '2021-05-18 17:31:34', '10.60'),
(1954, 'Z2', 'H2', '2021-05-18 17:31:35', '11.95'),
(1955, 'Z1', 'L1', '2021-05-18 17:31:35', '11.05'),
(1956, 'Z1', 'T1', '2021-05-18 17:31:36', '10.30'),
(1957, 'Z2', 'T2', '2021-05-18 17:31:37', '13.00'),
(1958, 'Z1', 'L1', '2021-05-18 17:31:37', '11.95'),
(1959, 'Z1', 'H1', '2021-05-18 17:31:38', '13.45'),
(1960, 'Z2', 'L2', '2021-05-18 17:31:39', '11.65'),
(1961, 'Z2', 'T2', '2021-05-18 17:31:39', '10.30'),
(1962, 'Z2', 'H2', '2021-05-18 17:31:39', '13.15'),
(1963, 'Z1', 'L1', '2021-05-18 17:31:39', '12.55'),
(1964, 'Z1', 'H1', '2021-05-18 17:31:40', '14.50'),
(1965, 'Z1', 'T1', '2021-05-18 17:31:41', '11.35'),
(1966, 'Z2', 'T2', '2021-05-18 17:31:41', '10.90'),
(1967, 'Z2', 'H2', '2021-05-18 17:31:41', '14.05'),
(1968, 'Z1', 'L1', '2021-05-18 17:31:41', '13.15'),
(1969, 'Z1', 'H1', '2021-05-18 17:31:42', '15.10'),
(1970, 'Z2', 'L2', '2021-05-18 17:31:43', '12.85'),
(1971, 'Z2', 'T2', '2021-05-18 17:31:43', '11.50'),
(1972, 'Z2', 'H2', '2021-05-18 17:31:43', '14.65'),
(1973, 'Z1', 'L1', '2021-05-18 17:31:43', '13.60'),
(1974, 'Z1', 'H1', '2021-05-18 17:31:44', '15.55'),
(1975, 'Z1', 'T1', '2021-05-18 17:31:45', '12.55'),
(1976, 'Z2', 'L2', '2021-05-18 17:31:45', '13.60'),
(1977, 'Z2', 'T2', '2021-05-18 17:31:45', '12.10'),
(1978, 'Z2', 'H2', '2021-05-18 17:31:45', '15.10'),
(1979, 'Z1', 'L1', '2021-05-18 17:31:45', '14.20'),
(1980, 'Z1', 'H1', '2021-05-18 17:31:46', '16.00'),
(1981, 'Z1', 'T1', '2021-05-18 17:31:47', '13.30'),
(1982, 'Z2', 'L2', '2021-05-18 17:31:47', '14.20'),
(1983, 'Z1', 'H1', '2021-05-18 17:31:48', '16.00'),
(1984, 'Z1', 'T1', '2021-05-18 17:31:49', '13.90'),
(1985, 'Z2', 'L2', '2021-05-18 17:31:49', '14.95'),
(1986, 'Z2', 'T2', '2021-05-18 17:31:50', '13.15'),
(1987, 'Z2', 'H2', '2021-05-18 17:31:50', '16.00'),
(1988, 'Z1', 'L1', '2021-05-18 17:31:50', '15.25'),
(1989, 'Z1', 'T1', '2021-05-18 17:31:51', '14.50'),
(1990, 'Z2', 'L2', '2021-05-18 17:31:51', '15.40'),
(1991, 'Z1', 'H1', '2021-05-18 17:31:53', '15.85'),
(1992, 'Z1', 'T1', '2021-05-18 17:31:53', '15.10'),
(1993, 'Z2', 'L2', '2021-05-18 17:31:53', '16.00'),
(1994, 'Z2', 'T2', '2021-05-18 17:31:54', '14.35'),
(1995, 'Z2', 'H2', '2021-05-18 17:31:54', '16.00'),
(1996, 'Z1', 'L1', '2021-05-18 17:31:54', '16.00'),
(1997, 'Z1', 'T1', '2021-05-18 17:31:55', '15.85'),
(1998, 'Z2', 'L2', '2021-05-18 17:31:55', '16.00'),
(1999, 'Z2', 'T2', '2021-05-18 17:31:56', '15.10'),
(2000, 'Z2', 'H2', '2021-05-18 17:31:56', '15.40'),
(2001, 'Z1', 'L1', '2021-05-18 17:31:56', '16.00'),
(2002, 'Z1', 'H1', '2021-05-18 17:31:57', '14.65'),
(2003, 'Z1', 'T1', '2021-05-18 17:31:57', '16.00'),
(2004, 'Z2', 'T2', '2021-05-18 17:31:58', '15.70'),
(2005, 'Z2', 'H2', '2021-05-18 17:31:58', '14.80'),
(2006, 'Z1', 'L1', '2021-05-18 17:31:58', '15.70'),
(2007, 'Z2', 'L2', '2021-05-18 17:32:00', '15.85'),
(2008, 'Z2', 'T2', '2021-05-18 17:32:00', '16.00'),
(2009, 'Z2', 'H2', '2021-05-18 17:32:00', '14.20'),
(2010, 'Z1', 'L1', '2021-05-18 17:32:00', '14.95'),
(2011, 'Z1', 'H1', '2021-05-18 17:32:01', '13.45'),
(2012, 'Z1', 'T1', '2021-05-18 17:32:02', '16.00'),
(2013, 'Z2', 'L2', '2021-05-18 17:32:02', '15.10'),
(2014, 'Z2', 'T2', '2021-05-18 17:32:02', '16.00'),
(2015, 'Z2', 'H2', '2021-05-18 17:32:02', '13.45'),
(2016, 'Z1', 'L1', '2021-05-18 17:32:02', '14.50'),
(2017, 'Z1', 'H1', '2021-05-18 17:32:03', '12.70'),
(2018, 'Z1', 'T1', '2021-05-18 17:32:04', '15.40'),
(2019, 'Z2', 'L2', '2021-05-18 17:32:04', '14.50'),
(2020, 'Z2', 'T2', '2021-05-18 17:32:04', '16.00'),
(2021, 'Z2', 'H2', '2021-05-18 17:32:05', '12.70'),
(2022, 'Z1', 'H1', '2021-05-18 17:32:05', '12.10'),
(2023, 'Z1', 'T1', '2021-05-18 17:32:06', '14.80'),
(2024, 'Z2', 'L2', '2021-05-18 17:32:06', '13.90'),
(2025, 'Z2', 'T2', '2021-05-18 17:32:06', '15.40'),
(2026, 'Z1', 'L1', '2021-05-18 17:32:07', '13.45'),
(2027, 'Z2', 'H2', '2021-05-18 17:32:07', '12.10'),
(2028, 'Z1', 'H1', '2021-05-18 17:32:07', '11.50'),
(2029, 'Z1', 'T1', '2021-05-18 17:32:08', '14.20'),
(2030, 'Z2', 'L2', '2021-05-18 17:32:08', '13.30'),
(2031, 'Z2', 'H2', '2021-05-18 17:32:09', '11.50'),
(2032, 'Z1', 'H1', '2021-05-18 17:32:09', '10.90'),
(2033, 'Z1', 'T1', '2021-05-18 17:32:10', '13.60'),
(2034, 'Z2', 'L2', '2021-05-18 17:32:10', '12.70'),
(2035, 'Z2', 'T2', '2021-05-18 17:32:11', '14.35'),
(2036, 'Z1', 'L1', '2021-05-18 17:32:11', '12.25'),
(2037, 'Z1', 'H1', '2021-05-18 17:32:11', '10.15'),
(2038, 'Z1', 'T1', '2021-05-18 17:32:12', '13.00'),
(2039, 'Z2', 'L2', '2021-05-18 17:32:12', '12.10'),
(2040, 'Z2', 'T2', '2021-05-18 17:32:13', '13.60'),
(2041, 'Z1', 'L1', '2021-05-18 17:32:13', '11.50'),
(2042, 'Z2', 'H2', '2021-05-18 17:32:13', '10.75'),
(2043, 'Z1', 'T1', '2021-05-18 17:32:14', '12.40'),
(2044, 'Z2', 'T2', '2021-05-18 17:32:15', '13.00'),
(2045, 'Z1', 'L1', '2021-05-18 17:32:15', '10.90'),
(2046, 'Z2', 'H2', '2021-05-18 17:32:15', '10.00'),
(2047, 'Z1', 'H1', '2021-05-18 17:32:16', '9.25'),
(2048, 'Z1', 'T1', '2021-05-18 17:32:16', '11.80'),
(2049, 'Z2', 'L2', '2021-05-18 17:32:16', '11.35'),
(2050, 'Z2', 'T2', '2021-05-18 17:32:17', '12.40'),
(2051, 'Z1', 'L1', '2021-05-18 17:32:17', '10.30'),
(2052, 'Z2', 'H2', '2021-05-18 17:32:17', '9.40'),
(2053, 'Z1', 'H1', '2021-05-18 17:32:18', '8.50'),
(2054, 'Z2', 'L2', '2021-05-18 17:32:19', '10.30'),
(2055, 'Z2', 'T2', '2021-05-18 17:32:19', '11.80'),
(2056, 'Z1', 'L1', '2021-05-18 17:32:19', '9.70'),
(2057, 'Z2', 'H2', '2021-05-18 17:32:19', '8.80'),
(2058, 'Z1', 'H1', '2021-05-18 17:32:20', '11.20'),
(2059, 'Z1', 'T1', '2021-05-18 17:32:21', '10.90');

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
(1, 2, 0, 20, 0, 20, 0, 20),
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
(3, 'vascopl', 'davidrodrigues@gmail.com', 't'),
(4, 'essertgod', 'gehsser@gehmail.com', 'i');

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
  MODIFY `idalerta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=58;

--
-- AUTO_INCREMENT de tabela `cultura`
--
ALTER TABLE `cultura`
  MODIFY `idcultura` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `medicao`
--
ALTER TABLE `medicao`
  MODIFY `idmedicao` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2060;

--
-- AUTO_INCREMENT de tabela `utilizador`
--
ALTER TABLE `utilizador`
  MODIFY `idutilizador` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Restrições para despejos de tabelas
--

--
-- Limitadores para a tabela `alerta`
--
ALTER TABLE `alerta`
  ADD CONSTRAINT `alerta_ibfk_1` FOREIGN KEY (`idcultura`) REFERENCES `cultura` (`idcultura`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `alerta_ibfk_2` FOREIGN KEY (`idutilizador`) REFERENCES `utilizador` (`idutilizador`) ON DELETE SET NULL ON UPDATE CASCADE;

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
