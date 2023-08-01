<?php

// nimmt den Namen noch nicht an bei einem json input

$name = $_POST['title'];

if ($name == null) {
    $name = 'guest';
}

$message = $_POST['message'];

if ($message == null) {
    $message = 'hello there';
}

// final json = '{"title": "Hello", "body": "body text", "userId": 1}';

$arr = array('title' => $name, 'body' => 2, 'userId' => 3);

echo json_encode($arr);

//echo "$name says: $message";


