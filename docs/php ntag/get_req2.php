<?php

// important: use "abc" instead of 'abc', otherwise linebreak won't work !

$name = $_GET['name'];

if ($name == null) {
    $name = 'guest';
}

$message = $_GET['message'];

if ($message == null) {
    $message = 'hello there';
}

// final json = '{"title": "Hello", "body": "body text", "userId": 1}';

$arr = array('title' => $name, 'body' => 2, 'userId' => 3);

echo json_encode($arr)."<br />";

echo "$name says: $message"."<br />";


