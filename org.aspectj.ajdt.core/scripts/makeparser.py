# set these first four variables appropriately for your system
eclipseWorkspace = "c:/aspectj/2.1/eclipse/workspace"
workingDir = "c:/apps/jikespg/jdt/tmp"

jikespg = "c:/apps/jikespg/src/jikespg.exe"
makersc = "c:/j2sdk1.4/bin/java -classpath c:/aspectj/2.1/eclipse/workspace/org.eclipse.jdt.core/bin;c:/apps/jikespg/jdt UpdateParserFiles"

# the rest of this should never change
ajCompilerHomeRel = "org.aspectj.ajdt.core/src/"
javaCompilerHomeRel = "org.eclipse.jdt.core/compiler/"

compilerHomeRel = ajCompilerHomeRel
parserHomeRel = ajCompilerHomeRel + "org/aspectj/ajdt/internal/compiler/parser"
parserInfoFileRel = javaCompilerHomeRel + "org/eclipse/jdt/internal/compiler/parser/ParserBasicInformation.java"
symbolsHomeRel = javaCompilerHomeRel + "org/eclipse/jdt/internal/compiler/parser/TerminalTokens.java"
#    symbolsHomeRel = "org/aspectj/ajdt/compiler/IAjTerminalSymbols.java"
parserClass = "AjParser.java"
grammarFileRel = javaCompilerHomeRel + "../grammar/java_1_4.g"

import os
from os import path
import re

def readFile(name, mode=''):
    f = open(name, 'r'+mode)
    text = f.read()
    f.close()
    return text

def writeFile(name, text, mode=''):
    f = open(name, 'w'+mode)
    f.write(text)
    f.close()

compilerHome = path.join(eclipseWorkspace, compilerHomeRel)
parserHome = path.join(eclipseWorkspace, parserHomeRel)
symbolFile = path.join(eclipseWorkspace, symbolsHomeRel)
parserInfoFile = path.join(eclipseWorkspace, parserInfoFileRel)

parserFile = path.join(parserHome, parserClass)
parserText = readFile(parserFile)

if grammarFileRel == None:
    r = re.compile(r"public final static void grammar\(\){.*(--main.*\$end\n)-- need", re.DOTALL)
    match = r.search(parserText)
    grammar = match.group(1)
else:
    grammar = readFile(path.join(eclipseWorkspace, grammarFileRel), 'b')
#print grammar

grammarFile = path.join(workingDir, "java.g")
writeFile(grammarFile, grammar, 'b')
os.chdir(workingDir)
os.system("%s java.g" % jikespg)

#3.1 Copy the contents of the JavaAction.java file into the consumeRule(int) method of the org.eclipse.jdt.internal.compiler.parser.Parser class. 

newConsumeRule = readFile(path.join(workingDir, "JavaAction.java"))
#print newConsumeRule

r = re.compile(r"(// This method is part of an automatic generation : do NOT edit-modify\W+protected void consumeRule\(int act\) {.*)protected void consumeSimpleAssertStatement\(\) {", re.DOTALL)

match = r.search(parserText)
parserText = parserText.replace(match.group(1), newConsumeRule)


#3.2 The definition of the Parser needs to be updated with two tables from javadcl.java. Those are rhs[] and name[].

newTables = readFile(path.join(workingDir, "javadcl.java"))
r = re.compile(r"(public final static byte rhs\[\] = \{[^}]*\};)", re.DOTALL)
rhsTable = r.search(newTables).group(0)

parserText = parserText.replace(r.search(parserText).group(0), rhsTable)


r = re.compile(r"(public final static String name\[\] = \{[^}]*\}[^}]*\};)", re.DOTALL)
nameTable = r.search(newTables).group(0)
nameTable = nameTable.replace("\"$eof\"", "UNEXPECTED_EOF")
nameTable = nameTable.replace("$error", "Invalid Character")

parserText = parserText.replace(r.search(parserText).group(0), nameTable)


#we're done w/ Parser.java
writeFile(parserFile, parserText)



#3.3 The class org.eclipse.jdt.internal.compiler.parser.ParserBasicInformation needs to be updated with the content of the file javadef.java.

defs = readFile(path.join(workingDir, "javadef.java"))
r = re.compile(r"(public final static int[^;]*;)", re.DOTALL)
syms = r.search(defs).group(0)
#print syms

text = readFile(parserInfoFile)
text = text.replace(r.search(text).group(0), syms)

writeFile(parserInfoFile, text)

#3.4 This is the contents of the class org.eclipse.jdt.internal.compiler.parser.TerminalSymbols.

defs = readFile(path.join(workingDir, "javasym.java"))
r = re.compile(r"(int\s+TokenNameIdentifier[^;]*;)", re.DOTALL)
syms = r.search(defs).group(0)
syms = syms.replace("$eof", "EOF")
syms = syms.replace("$error", "ERROR")

print syms

text = readFile(symbolFile)
text = text.replace(r.search(text).group(0), syms)

writeFile(symbolFile, text)

#3.5 The last step is to update the resource files:

os.chdir(workingDir)
os.system("%s javadcl.java" % makersc)

for i in range(1,6):
    name = "parser%d.rsc" % i
    print "moving", name
    t = readFile(path.join(workingDir, name), 'b')
    writeFile(path.join(parserHome, name), t, 'b')
