inputFile = "c:/eclipse/workspace/org.eclipse.jdt.core/aspectj/org/aspectj/workbench/resources/FileAdapter.java"

text = open(inputFile, 'r').read()

#print text

import re, string

methodPat = re.compile(r"public [^{]*\([^{]*{[^}]*}") #^[{]\)^[{]{", re.DOTALL) #{ .* }", re.DOTALL)

throwException = """throw new RuntimeException("unimplemented");"""

for method in methodPat.findall(text):
    print method

    newMethod = method[:len(method)-1]

    startBody = newMethod.find("{")
    newMethod = newMethod[:startBody+1]

    
    newMethod = newMethod + "\n\t" + throwException + "\n\t}"
    text = text.replace(method, newMethod)

print text
    
open(inputFile, 'w').write(text)

