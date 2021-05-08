<?php
	header('Cache-Control: no cache');
	session_cache_limiter('none');
	session_start();
	
	mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
	if (isset($_SESSION['username']) && isset($_SESSION['password']) && isset($_GET['idcultura']) && !empty($_GET['idcultura'])) {
		$conn = mysqli_connect("127.0.0.1",$_SESSION['username'],$_SESSION['password'],"projetosid");
	} else {
		header('Location: login.php');
		exit();
	}
	
	if (isset($_SESSION['edit_status'])) {
		if ($_SESSION['edit_status'] == 'invalid') {
			$msg = 'autenticação inválida';
		} else {
			$msg = '';
		}
	} else {
		$msg = '';
	}
?>

<html>
	<head>
		<title>Edit</title>
      
		<style>
			body {
				padding-top: 40px;
				padding-bottom: 40px;
				text-align: center;
				font-family: Courier New;
			}
			
			h4 {
				margin-top: 0px;
				font-size: 13px;
				color: red;
			}
			 
			.form-edit {
				max-width: 345px;
				padding: 10px;
				margin: 0 auto;
			}
			 
			.form-edit .form-control {
				padding: 10px;
				font-size: 16px;
				margin-bottom: 15px;
				font-family: Courier New;
			}
		</style>
	</head>
	
	<body>
		<h1>Editar Cultura</h1>
		
		<?php 
			$query_select = "SELECT * FROM cultura 
							INNER JOIN parametrocultura ON cultura.idcultura=parametrocultura.idcultura 
							WHERE cultura.idcultura = " . $_GET['idcultura'];
			$result_query_select = mysqli_query($conn, $query_select);
			$cultura = mysqli_fetch_assoc($result_query_select);
			$result_query_select->close();
			mysqli_close($conn);
		?>
		
		<div>
			<form class = "form-edit" role = "form" action = "save.php" method = "post">
				<div style = "display:flex; flex-wrap:wrap; margin-left:-50px">
					<input type = "hidden" name = "username" value = <?php echo $_SESSION['username'] ?>>
					<input type = "hidden" name = "password" value = <?php echo $_SESSION['password'] ?>>
					<input type = "hidden" name = "idcultura" value = "<?php echo $cultura["idcultura"] ?>">
					<p style = "flex:1 1 33%">nome: </p><input type = "text" class = "form-control" name = "nome" value = "<?php echo $cultura["nome"] ?>" required autofocus>
					<p style = "flex:1 1 33%">estado: </p><input type = "text" class = "form-control" name = "estado" value = "<?php echo $cultura["estado"] ?>" required>
					<p style = "flex:1 1 33%">idzona: </p><input type = "text" class = "form-control" name = "idzona" value = "<?php echo $cultura["idzona"] ?>" required>
					<p style = "flex:1 1 33%">min_t: </p><input type = "text" class = "form-control" name = "min_t" value = "<?php echo $cultura["min_t"] ?>" required>
					<p style = "flex:1 1 33%">max_t: </p><input type = "text" class = "form-control" name = "max_t" value = "<?php echo $cultura["max_t"] ?>" required>
					<p style = "flex:1 1 33%">min_h: </p><input type = "text" class = "form-control" name = "min_h" value = "<?php echo $cultura["min_h"] ?>" required>
					<p style = "flex:1 1 33%">max_h: </p><input type = "text" class = "form-control" name = "max_h" value = "<?php echo $cultura["max_h"] ?>" required>
					<p style = "flex:1 1 33%">min_l: </p><input type = "text" class = "form-control" name = "min_l" value = "<?php echo $cultura["min_l"] ?>" required>
					<p style = "flex:1 1 33%">max_l: </p><input type = "text" class = "form-control" name = "max_l" value = "<?php echo $cultura["max_l"] ?>" required>
				</div>
				<button class = "form-control" type = "submit" name = "save">Guardar</button>
				<h4><?php echo $msg; ?></h4>
			</form>
		</div>
		
	</body>

</html>