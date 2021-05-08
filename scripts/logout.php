<?php
	header('Cache-Control: no cache');
	session_cache_limiter('none');
	session_start();
	
	unset($_SESSION['username']);
	unset($_SESSION['password']);
	header('Location: login.php');
?>