
These tests were brought over from testing-drivers.

The test specifications are in ajcTests{Failing}.xml.

Some files are old and unused in this directory:

- The files suite.xml, suiteFails.xml, and selectionTest.xml
  show the old test specifications.  
  - Note that you can run "incremental" tests without the
    -incremental option, and it will work even when listing
    files (rather than using sourceroots), except that 
    deleting listed files causes an error.

- It's possible not all sources are used.
