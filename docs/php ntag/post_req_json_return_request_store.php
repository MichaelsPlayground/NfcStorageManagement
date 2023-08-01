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
$newField = 'key saved';

/*
// store title in file
$titleFile = 'key.txt';
//file_force_contents($titleFile, $jsonTitle);

$file = 'keys/people.txt';
// Öffnet die Datei, um den vorhandenen Inhalt zu laden
//$current = file_get_contents($file);
// Fügt eine neue Person zur Datei hinzu
$current .= "John Smith and Nicole Wesson\n";
// Schreibt den Inhalt in die Datei zurück
file_put_contents($file, $current);

//mkdir ("keys2", 0666);
// Gewünschte Verzeichnisstruktur
$structure = './stufe1/stufe2/stufe3/';
// Zur Erstellung der verschachtelten Struktur muss der Parameter $recursive
// von mkdir() angegeben werden
if (!mkdir($structure, 0777, true)) {
    //die('Erstellung der Verzeichnisse schlug fehl...');
    echo 'Erstellung der Verzeichnisse schlug fehl...' . PHP_EOL;
}
$file2 = './stufe1/stufe2/stufe3/people2.txt';
file_put_contents($file2, $current); // sollte scheitern, folder existiert nicht

$structure3 = 'stufex/stufe2/stufe3/key.txt';
file_force_contents($structure3, $current);
*/

echo PHP_EOL . 'try number 4' . PHP_EOL;
$pathToKeys = './keyFolder/';
$keyFilename = './keyFolder/key1.txt';
if (!mkdir($pathToKeys, 0750, true)) {
    //die('Erstellung der Verzeichnisse schlug fehl...');
    echo 'try 4 Erstellung der Verzeichnisse schlug fehl...' . PHP_EOL;
}
$current = 'AAA12345BBB';
file_put_contents($keyFilename, $current);

/*
// first check that directory exist
$pathToKeysExist = is_dir($pathToKeys . "/.");
if ($pathToKeys == false) {
    mkdir($pathToKeys, 0777, false);
}
// second write file
file_put_contents($pathToKeys.$keyFilename, $current);
*/


// gibt auch den ursprünglichen request zurück
$arr = array('title' => $jsonTitle, 'new field' => $newField, 'body' => $jsonBody, 'userId' => $jsonUserIdChanged, 'request' => $requestdata);

echo json_encode($arr);

// function forces to create a directory if it does not exist
function file_force_contents($dir, $contents){
    echo 'file_force_contents dir: ' . $dir . ' contents: ' . $contents . PHP_EOL;
    $parts = explode('/', $dir);
    $file = array_pop($parts);
    $dir = '';
    foreach($parts as $part)
        if(!is_dir($dir .= "/$part")) mkdir($dir);
    file_put_contents("$dir/$file", $contents);
}
