Mode: VM Run
Title: empty and singular patterns on modifiers and throws

This test identifies sets of methods based on the patterns on
modifiers and throws.

If the modifier pattern is empty, then the constraint on
methods means "quantify over methods that may or may not have any
modifier".

If the modifier pattern is  a single value,
then the constraint means "quantify over methods that have that
modifier value".

The same rule applies for pattern in the  throws clause.






