<?php

// important: use "abc" instead of 'abc', otherwise linebreak won't work !

// this tries to receive an uid in uid, makes a filename like uid.txt, and writes the uid to the file in append mode
// additionally it verifies the mac against the uid, the mac is sha256 of the uid

$uid = $_GET['uid'];
$mac = $_GET['mac'];

// verify mac
// 01020304050607 gives sha256: 783397175344fc84c0526b617652fd9b766febe64515b6a397f289168e81d830:783397175344fc84c0526b617652fd9b766febe64515b6a397f289168e81d830
// 01020304050607 gives 78339717 (first 8 chars)
// 11020304050607 gives sha256: 44a852f03a6370ca0286d8abe9150e3acd5f9cc87f2a74a1756ecae2f3e2b8e6:44a852f03a6370ca0286d8abe9150e3acd5f9cc87f2a74a1756ecae2f3e2b8e6
// 11020304050607 gives sha256: 44a852f0
$uidMac = hash('sha256', $uid, true);
$uidMacHex = bin2hex($uidMac);
echo "$uidMacHex:".$uidMacHex."<br />";
$uidMacHex8Chars = substr($uidMacHex, 0, 8);
echo "$uidMacHex8Chars:".$uidMacHex8Chars."<br />";
if ($uidMacHex8Chars == $mac) {
    echo "mac is VERIFIED"."<br />";
} else {
	echo "mac is FAILING"."<br />";
}

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


