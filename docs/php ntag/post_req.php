<?php
$name = $_POST['name'];

if ($name == null) {
    $name = 'guest';
}

$message = $_POST['message'];

if ($message == null) {
    $message = 'hello there';
}


echo "$name says: $message";


