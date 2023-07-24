# NFC Storage Management

This is a Storage Management Application that helps in managing all cartons and goods stored in a Deposit room. 
The app is using NFC tags of type NXP NTAG 21x to easily identify each position by simply reading the tag with 
an NFC reader embedded in most modern Android smartphones.

There are 3 different tag types available for the NTAG 21x familiy:
- NTAG213 with 144 bytes of freely available user memory
- NTAG215 with 540 bytes of freely available user memory
- NTAG216 with 888 bytes of freely available user memory

The tags do have a build-in NDEF capability so they are read- and writable with common readers. The provide 
a 7-byte serial number UID) that was programmed by the  manufacturer and are immutable. As additional 
feature the tags can **mirror the UID and a reader counter** into user memory so the can get part of an NDEF   
message. The tags, especially the NTAG213, is available for less as a sticker so it can be easily attached 
to the carton.

As this is a complex system some parts of the work are done using the smartphone and other via Webinterface, 
so will need to available space on a webserver you own. To connect the tag with smartphone and webserver  
I'm using the UID as identifier for all datasets. 

The webserver-URL is coded in the NDEF message as a link like the following example:

https://www.example.com/storage/ident&uid=0123456789ABCDEFx112233

The workflow for the management is as follows:

**Preparation before usage*
1 The tags are written for the first usage with an NDEF message that contains the link to a webpage. The mirror 
function is enabled for the UID and the counter, after that the tag get write disabled
2 The tag is identified by the app and an empty webspace file is created (internet connection necessary)
**Usage workflow at storage place**
3 The tag is attached to a carton and read by the app. The user manually adds a carton number (usually something 
written in big letters at the carton), this information is stored in the app internal database
4 The user can make up to 3 photos of the content with the smartphone's camera
5 edit the dataset by manually type in the content (not recommended)
**Usage workflow at the office**
6 Using an internet connection the app is uploading some data to the webspace like cartons content (if collected) 
and the photos
7 Edit the content file for each carton to provide more information about the content using the webspace editor
8 download the content from the webspace to the internal database on the smartphone to have an offline source

Some minor actions can happen: 
- delete an entry because the carton is permanently removed
- mark an entry as absent because the carton is temporary removed
- add/modify/delete some photos

Enhancements:
- encrypt the data on webspace
- use two or more tags to identify the same cartoon (because the tag is attached to a carton side that it not more 
accessible due to storage place)
- add an information where the storage place is in detail (e.g. "row last on left side, 2nd from botton")
- use a multi-user/multi-app system

## Project status: not started yet

Datasheet for NTAG21x: https://www.nxp.com/docs/en/data-sheet/NTAG213_215_216.pdf

Icons: https://www.freeiconspng.com/images/nfc-icon

Nfc Simple PNG Transparent Background: https://www.freeiconspng.com/img/20581

<a href="https://www.freeiconspng.com/img/20581">Nfc Png Simple</a>

Icon / Vector editor: https://editor.method.ac/

Minimum SDK is 26 (Android 8)

