Mode: VM run
Title: Local declarations in advise bodies

This tests local declarations in the body of advises.  Local
declarations help the weaves inside advises to share variables between
them.  These variables should be resolved in the context of the
methods being advised.  

The syntax supports local variable as well as class declrations.  But
the weaver can't handle inner classes yet, so only the local variable
declaration case is tested.

