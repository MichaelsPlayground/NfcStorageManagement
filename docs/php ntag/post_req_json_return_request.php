<?php

$requestdata = file_get_contents('php://input');
$data = json_decode($requestdata, true);

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
$newField = 'test return';

// gibt auch den ursprünglichen request zurück
$arr = array('title' => $jsonTitle, 'new field' => $newField, 'body' => $jsonBody, 'userId' => $jsonUserIdChanged, 'request' => $requestdata);

echo json_encode($arr);
