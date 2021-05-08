<?php
	header('Cache-Control: no cache');
	session_cache_limiter('none');
	session_start();

	if (isset($_SESSION['username']) && isset($_SESSION['password'])) {
		header('Location: main.php');
	}

	if (isset($_SESSION['login_status'])) {
		if ($_SESSION['login_status'] == 'invalid') {
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
		<title>Login</title>
      
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
			 
			.form-signin {
				max-width: 215px;
				padding: 10px;
				margin: 0 auto;
			}
			 
			.form-signin .form-control {
				padding: 10px;
				font-size: 16px;
				margin-bottom: 15px;
				font-family: Courier New;
			}
		</style>
	</head>
	
	<body>		
		<h1>Bem-Vindo</h1>

		<div>
			<form class = "form-signin" role = "form" action = "main.php" method = "post">
				<input type = "text" class = "form-control" name = "username" placeholder = "Username" required autofocus></br>
				<input type = "password" class = "form-control" name = "password" placeholder = "Password" required></br>
				<button class = "form-control" type = "submit" name = "login">Login</button>
				<h4><?php echo $msg; ?></h4>
			</form>
		</div>
	</body>
</html>