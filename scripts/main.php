<?php
	header('Cache-Control: no cache');
	session_cache_limiter('none');
	session_start();
	
	mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
	if (!isset($_POST['login']) && isset($_SESSION['username']) && isset($_SESSION['password'])) {
		$conn = mysqli_connect("127.0.0.1",$_SESSION['username'],$_SESSION['password'],"projetosid");
	} else if (isset($_POST['login']) && !empty($_POST['username']) && !empty($_POST['password'])) {
		try {
			$conn = mysqli_connect("127.0.0.1",$_POST['username'],$_POST['password'],"projetosid");
			$query_select = "SELECT current_role()";
			$result_query_select = mysqli_query($conn, $query_select);
			$role = mysqli_fetch_assoc($result_query_select)['current_role()'];
			$result_query_select->close();
			if ($role != "investigador") {
				mysqli_close($conn);
				$_SESSION['login_status'] = 'invalid';
				header('Location: login.php');
				exit();
			}
			$_SESSION['username'] = $_POST['username'];
			$_SESSION['password'] = $_POST['password'];
			$_SESSION['login_status'] = 'valid';
		} catch (mysqli_sql_exception $e) {
			$_SESSION['login_status'] = 'invalid';
			header('Location: login.php');
			exit();
		}
	} else {
		header('Location: login.php');
		exit();
	}
?>

<html>
	<head>
		<title>Main</title>

		<style>
			body {
				padding-top: 10px;
				padding-bottom: 10px;
				padding-left: 10px;
				padding-right: 10px;
				font-family: Courier New;
			}
			
			h1 {
				padding-bottom: 10px;
				text-align: center;
			}
			
			.btn-logout {
				font-size: 16px;
				font-family: Courier New;
				padding: 10px;
			}
			
			.btn-table {
				font-size: 16px;
				font-family: Courier New;
				padding: 10px;
				margin-bottom: -16px;
			}
		</style>
	</head>

	<body>
		<form role = "form" action = "logout.php" method = "POST">
			<button class = "btn-logout" type = "submit">Logout</button>
		</form>
		
		<h1>Gestão de Culturas</h1>
		
		<table border = 1px solid black style = "width:100%;border-collapse: collapse;">
			<tr>
				<th>idcultura</th>
				<th>nome</th> 
				<th>estado</th>
				<th>idzona</th> 
				<th>min_t</th>
				<th>max_t</th>
				<th>min_h</th> 
				<th>max_h</th>
				<th>min_l</th>
				<th>max_l</th>
			</tr>
			<?php
				$query_select = "SELECT * FROM cultura 
								INNER JOIN parametrocultura ON cultura.idcultura=parametrocultura.idcultura 
								WHERE idutilizador = (SELECT idutilizador FROM utilizador WHERE nome = '" . $_SESSION['username'] . "')";
				$result_query_select = mysqli_query($conn, $query_select);
				while ($row = mysqli_fetch_assoc($result_query_select)) {?>
					<tr>
						<td style = "text-align:center"><?php echo $row["idcultura"] ?></td>
						<td style = "text-align:center"><?php echo $row["nome"] ?></td>
						<td style = "text-align:center"><?php echo $row["estado"] ?></td>
						<td style = "text-align:center"><?php echo $row["idzona"] ?></td>
						<td style = "text-align:center"><?php echo $row["min_t"] ?></td>
						<td style = "text-align:center"><?php echo $row["max_t"] ?></td>
						<td style = "text-align:center"><?php echo $row["min_h"] ?></td>
						<td style = "text-align:center"><?php echo $row["max_h"] ?></td>
						<td style = "text-align:center"><?php echo $row["min_l"] ?></td>
						<td style = "text-align:center"><?php echo $row["max_l"] ?></td>
						<td>
							<form role = "form" action = "edit.php" method = "get">
								<input type = "hidden" name = "idcultura" value = <?php echo $row["idcultura"] ?>>
								<button class = "btn-table" style = "width:100%" type = "submit">Editar</button>
							</form>
						</td>
					</tr>
				<?php }
				$result_query_select->close();
			?>
		</table>
	</body>

</html>
