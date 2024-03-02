AspectJ_JDK_Update

Always add latest AspectJ core libraries (aspectjrt.jar, aspectjweaver.jar, aspectjtools.jar) here to ensure that
relevant integration tests run with the latest versions. Having missed the update in the past has led to regression bugs
to occur, e.g. https://github.com/eclipse-aspectj/aspectj/issues/285, which was introduced during the fix for
https://github.com/eclipse-aspectj/aspectj/issues/277.
