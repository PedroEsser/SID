<?php
	header('Cache-Control: no cache');
	session_cache_limiter('none');
	session_start();

	mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
	if (isset($_SESSION['username']) && isset($_SESSION['password']) && isset($_POST['save']) && !empty($_POST['idcultura'])) {
		$conn = mysqli_connect("127.0.0.1",$_SESSION['username'],$_SESSION['password'],"projetosid");
		$procedure = "CALL alterar_cultura(" . $_POST['idcultura'] . ",'" . $_POST['nome'] . "'," .
					$_POST['estado'] . "," . $_POST['idzona'] . "," . $_POST['min_t'] . "," . $_POST['max_t'] . "," .
					$_POST['min_h'] . "," . $_POST['max_h'] . "," . $_POST['min_l'] . "," . $_POST['max_h'] . ")";
		try {
			$result = mysqli_query($conn, $procedure);
			$_SESSION['edit_status'] = 'valid';
			header('Location: main.php');
		} catch (mysqli_sql_exception $e) {
			$_SESSION['edit_status'] = 'invalid';
			header('Location: edit.php?idcultura=' . $_POST['idcultura']);
		}
		mysqli_close($conn);
		exit();
	} else {
		header('Location: login.php');
		exit();
	}
?>