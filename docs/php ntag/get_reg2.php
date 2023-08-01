<?php

// important: use "abc" instead of 'abc', otherwise linebreak won't work !

// this tries to find if an uid file is present

$uid = $_GET['uid'];
$handle = null;
if ($uid == null) {
    $name = 'guest';
} else {
  $filename = $uid . ".txt";
  echo "$uid gets:".$filename."<br />";
  $fullFilename = dirname(__FILE__). '/data/' . $filename;
  echo "$fullFilename:".$fullFilenamefilename."<br />";
  $fileExists = file_exists($fullFilename);
  echo "fullFilenameExists: " . $fileExists ."<br />";
  if ($fileExists) {
	$fileContent = file_get_contents($fullFilename);
	echo "fileContent" 	."<br />" . $fileContent ."<br />";
  }	
}  
echo "END of SCRIPT" . "<br />";
