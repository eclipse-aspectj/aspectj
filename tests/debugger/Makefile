SHELL = bash

### Fill these in #######################
aspectj = C:/aspectj
root = C:/aspectj/tests
your_classpath = C:/aspectj/tests/lib#C:/classes
tools_jar = C:/apps/jdk1.3/lib/tools.jar
aspectj_lib = C:/aspectj/lib
##########################################

aspectj_src = $(aspectj)/src
jar_path = C:/apps/aspectj0.7/lib
aspectj_jars = $(jar_path)/aspectjrt.jar;$(jar_path)/aspectjtools.jar
path = debugger
tests = $(aspectj)/tests
d = $(tests)/lib
lib = $(d)/$(path)
srcpath = $(tests)
classpath = "$(d);$(your_classpath);$(aspectj_lib);$(tools_jar)"
javac_opts = -d $(d) $(g) -sourcepath $(srcpath) -classpath $(classpath)
javac_compile = javac $(javac_opts)
java_ex = Main
java_main = debugger.$(java_ex)
java_main_class = $(java_ex).class
java_opts = -classpath $(classpath)
java_args =
g = -g
ajc_compile = ajc $(g) -d $(your_classpath) -workingdir $(root)/ajworkingdir
javac_names = \
	Main\
	Tester\
	BreakpointTester\
	ThreadTester
javac_srcs = $(foreach name, $(javac_names), $(name).java)
javac_classes = $(foreach name, $(javac_names), $(d)/$(path)/$(name).class)

ajc_names = \
	TestClass\
	AJDBThreads
ajc_srcs = $(foreach name, $(ajc_names), $(name).java)
ajc_classes = $(foreach name, $(ajc_names), $(d)/$(path)/$(name).class)
test_args = -classpath "$(your_classpath);$(aspectj_jars)" -root $(root)

.SUFFIXES: .java .class

all: classes aspects

classes: 
	$(javac_compile) $(javac_srcs)

aspects: 
	$(ajc_compile) $(ajc_srcs)

run:
	java $(java_opts) $(java_main) $(java_args)

clean:
	rm -Rf *~* $(lib)/*.class

srcclean:
	rm -Rf *~* *.ajsym #*

db:
	make -C ../../debugger

test:	
	java $(java_opts) $(java_main) $(test_args)

.java.class:
	$(javac_compile) $?
