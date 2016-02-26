This is a Java application that contains several cryptography examples.

SecureRandomNumber generates random numbers that are unpredictable 
(contrary to pseudo-random number generators).
The numbers are printed as hexadecimal values.

SymCrypto generates a key and uses it to cipher and decipher data.
 
ASymCrypto generates a key pair and uses the public key to cipher and
the private key to decipher data.


Using Ant:
---------

Build steps are specified in build.xml file

To list available targets:
ant -p

To compile:
ant compile

To run:
ant run


To configure the project in Eclipse:
-----------------------------------

If Eclipse files (.project, .classpath) exist:
    'File', 'Import...', 'General'-'Existing Projects into Workspace'
    'Select root directory' and 'Browse' to the project base folder.
    Check if everything is OK and 'Finish'.

If Eclipse files do not exist:
    Create a 'New Project', 'Java Project'.
    Uncheck 'Use default location' and 'Browse' to the project base folder.
    Fill in the 'Project name'.

To run:
    Select the main class and click 'Run' (the green play button).
    Specify arguments using 'Run Configurations'

--
2012-03-24
Miguel.Pardal@tecnico.ulisboa.pt
