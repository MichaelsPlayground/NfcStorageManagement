<?php

// important: use "abc" instead of 'abc', otherwise linebreak won't work !

// this tries to receive an uid in uid, makes a filename like uid.txt, and writes the uid to the file in append mode

$uid = $_GET['uid'];
$handle = null;
if ($uid == null) {
    $name = 'guest';
} else {
  $filename = $uid . ".txt";
  echo "$uid gets:".$filename."<br />";
  $fullFilename = dirname(__FILE__). '/data/' . $filename;
  echo "$fullFilename:".$fullFilenamefilename."<br />";
  $handle = fopen($fullFilename, "a");
}  
  if ($handle) {
	  $line = PHP_EOL . $uid;
    $writeResult = fwrite($handle, $line);
  }
fclose($handle);

$message = $_GET['message'];

if ($message == null) {
    $message = 'hello there';
}

// final json = '{"title": "Hello", "body": "body text", "userId": 1}';

$arr = array('title' => $name, 'body' => 2, 'userId' => 3);

echo json_encode($arr)."<br />";

echo "$name says: $message"."<br />";


