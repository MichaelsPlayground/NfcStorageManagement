<?php

$data = json_decode(file_get_contents('php://input'), true);

$jsonTitle = $data["title"];
$jsonBody = $data["body"];
$jsonUserId = $data["userId"];

if ($jsonTitle == null) {
    $jsonTitle = 'guest';
}

if ($jsonBody == null) {
    $jsonBody = 'hello there';
}
/*
if ($jsonUserId == null) {
    $jsonUserId = 'neu vergeben';
}
*/

$jsonUserIdChanged = 'new UserId';

// final json = '{"title": "Hello", "body": "body text", "userId": 1}';

$arr = array('title' => $jsonTitle, 'body' => $jsonBody, 'userId' => $jsonUserIdChanged);

echo json_encode($arr);
