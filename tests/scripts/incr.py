import os, sys

from org.aspectj.util import FileUtil
from java.io import File

sourcedir = "incr_test_scratch_sources"
outdir = "incr_test_scratch_classes"
errorList = []
VERBOSE = 1

def createEmpty(dir):
	if os.path.exists(dir):
		FileUtil.deleteContents(File(dir))
	else:
		os.mkdir(dir)

def makeFile(name, contents):
	fullname = os.path.join(sourcedir, name)
	dirname = os.path.dirname(fullname)
	if not os.path.exists(dirname):	
		os.makedirs(dirname)
		
	fp = open(fullname, 'w')
	fp.write(contents)
	fp.close()

def deleteFile(name):
	os.remove(os.path.join(sourcedir, name))

def snapshot(dir, map=None):
	if map is None: map = {}
	for file in os.listdir(dir):
		filename = os.path.join(dir, file)
		if os.path.isdir(filename):
			snapshot(filename, map)
		else:
			stats = os.stat(filename)
			map[filename] = stats[8]
	return map

def diffSnapshots(old, new):
	unchanged = []
	changed = []
	for name, mtime in new.items():
		if old.has_key(name):
			oldTime = old[name]
			if oldTime == mtime:
				unchanged.append(name)
			else:
				changed.append(name)
			del old[name]
		else:
			changed.append(name)
	
	deleted = old.keys()
	
	return unchanged, changed, deleted




def error(m):
	errorList.append(m)
	print m

def suffixInList(suffix, list):
	for i in list:
		if i.endswith(suffix): return 1
	return 0

def checkClasses(kind, filelist, names):
	filenames = []
	for o in filelist:
		name = os.path.basename(o)
		filenames.append(name[:-6])
	checkSets(names, filenames, kind)

"""
	#print names, repr(names)
	if repr(names).startswith("\'"): names = [names]

	for c in names:
		classname = c+".class"
		if not suffixInList(classname, filelist):
			error("%s expected %s not found in %s" % (name, classname, filelist))
"""

def findAndRemove(l, item):
	for i in range(len(l)):
		if l[i] == item:
			del l[i]
			return 1
	return 0
	
def makeList(l):
	if repr(l).startswith("\'"): return [l]
	return l
	

def checkSets(expected, found, kind="error"):
	expected = makeList(expected)
	for e in expected:
		if not findAndRemove(found, e):
			error("expected %s %s not found in %s" % (kind, e, found))
	
	for f in found:
		error("unexpected %s %s" % (kind, f))



from org.aspectj.ajdt.ajc import AjdtCommand
from org.aspectj.bridge import IMessageHandler, IMessage

def makeSet(errors):
	ret = {}
	for e in errors:
		loc = e.getISourceLocation()
		if loc is None: continue  #???
		s = "%s:%i" % (loc.sourceFile.name[:-5], loc.line)
		ret[s] = s
	return ret.keys()


class Handler (IMessageHandler):
	def __init__(self):
		self.errors = []
		
	def handleMessage(self, message):
		if message.kind == IMessage.ERROR:
			self.errors.append(message)
		if VERBOSE: print message
	def isIgnoring(self, kind):
		return 0


createEmpty(sourcedir)
createEmpty(outdir)

handler = Handler()
cmd = AjdtCommand()


TEMPLATE = """\
%(package)s
%(modifiers)s %(kind)s %(classname)s %(parents)s {
    %(body)s
    public static void main(String[] args) {
        %(stmts)s
    }
}
"""

import string, time

def splitClassName(className):
	dot = className.rfind('.')
	if dot == -1:
		return None, className, className +".java"
	else:
		packageName = className[:dot]
		className = className[dot+1:]
		l = packageName.split('.')
		l.append(className + ".java")
		path = apply(os.path.join, l)
		
		return packageName, className, path


def makeType(className, stmts="""System.out.println("hello");""", body="", kind="class", parents=""):
	packageName, className, path = splitClassName(className)
	if packageName is None: packageDecl = ""
	else: packageDecl = "package %s;" % packageName

	contents = TEMPLATE % {'package':packageDecl, 'modifiers':'public', 
							'classname':className, 'body':body, 
							'stmts':stmts, 'kind':kind, 'parents':parents}
	makeFile(path, contents)

def deleteType(className):
	packageName, className, path = splitClassName(className)
	deleteFile(path)


def test(batch=0, couldChange=[], changed=[], deleted=[], errors=[]):
	print ">>>>test changed=%s, couldChange=%s, deleted=%s, errors=%s<<<<" % (changed, couldChange, deleted, errors)
	
	start = snapshot(outdir)
	#print start
	handler.errors = []
	
	time.sleep(0.1)
	
	if batch: cmd.runCommand(["-d", outdir, "-sourceroots", sourcedir], handler)
	else: cmd.repeatCommand(handler)

	checkSets(errors, makeSet(handler.errors))
	if len(handler.errors) > 0: return

	end = snapshot(outdir)
	#print "end", end
	u, c, d = diffSnapshots(start, end)
	checkClasses("changed", c, makeList(changed) + makeList(couldChange)) 
	checkClasses("deleted", d, deleted) 


"""
Pure Java tests
"""

makeType("p1.Hello")
test(batch=1, changed="Hello")

test()

makeType("p1.Hello", stmts="Target.staticM();")
test(errors="Hello:5")

test(errors="Hello:5")

makeType("p1.Target", body="static void staticM() {}")
test(changed=["Hello", "Target"])

deleteType("p1.Target")
test(errors="Hello:5")

makeType("p1.Target", body="static void staticM() { int x = 2; }")
test(changed=["Target", "Hello"])

makeType("p1.Target", body="""static void staticM() { System.out.println("foo"); }""")
test(changed=["Target"])

makeType("p1.Target", body="static int staticM() { return 2; }")
test(changed=["Hello", "Target"])

makeType("p1.Hello", body="static class Inner {}")
test(changed=["Hello", "Hello$Inner"])


deleteType("p1.Hello")
test(deleted=["Hello", "Hello$Inner"])

makeType("p1.Hello", body="static class NewInner {}")
test(changed=["Hello", "Hello$NewInner"])

makeType("p1.Hello", body="")
test(changed=["Hello"], deleted=["Hello$NewInner"])

print "done", errorList
sys.exit(0)



"""
Simple tests with aspects
"""

makeType("p1.Hello")
test(batch=1, changed="Hello")

makeType("p1.A", kind="aspect", body="before(): within(String) { }")
test(changed=["A"], couldChange=["Hello"])

makeType("p1.Hello")
makeType("p1.A", kind="aspect", body="before(): execution(* main(..)) { }")
test(changed=["A", "Hello"])

makeType("p1.A", kind="aspect", body="before(): within(Hello) { }")
test(changed=["A", "Hello"])

makeType("p1.Target")
test(changed="Target")

makeType("p1.Hello", stmts="new Target().m();")
test(errors=["Hello:5"])

makeType("p1.ATypes", kind="aspect", body="int Target.m() { return 10; }")
test(changed=["Hello", "ATypes", "Target"], couldChange=["A"])

makeType("p1.ATypes", kind="aspect", body="int Target.m(int x) { return x + 10; }")
test(errors=["Hello:5"])

makeType("p1.Hello", stmts="new Target().m(2);")
test(changed="Hello")

makeType("p1.Hello", stmts="new Target().m(5);")
test(changed="Hello")

makeType("p1.Hello", stmts="new Target().m(42);")
test(changed="Hello")



print "done", errorList
sys.exit(0)











"""
Bugzilla Bug 29684  
   Incremental: Commenting out conflict yeilds NullPointerException 
   
public class SomeClass {

    public String toString() {
        return "from SomeClass";
    }
}

public aspect Conflicter {

    public String SomeClass.toString() {
        return "from Conflicter";
    }

    public static void main(String[] args) {
       int i = 0;
    }
}

However, modifying Conflicter so that it reads:

public aspect Conflicter {

//    public String SomeClass.toString() {
//        return "from Conflicter";
//    }

    public static void main(String[] args) {
       int i = 0;
    }
}
   
   
"""
makeType("conflict.SomeClass", 
	body="""public String toString() { return "from SomeClass"; }""")
makeType("conflict.Conflicter", kind="aspect",
	body="""public String SomeClass.toString() { return "from Conflicter"; }""")
test(batch=1, errors=["Conflicter:3"])

makeType("conflict.Conflicter", kind="aspect",
	body="")
test(changed=["SomeClass", "Conflicter"])

makeType("conflict.Conflicter", kind="aspect",
	body="""public String SomeClass.toString() { return "from Conflicter"; }""")
test(errors=["Conflicter:3"])

makeType("conflict.SomeClass", 
	body="")
test(changed=["SomeClass"])



print "done", errorList
sys.exit(0)



"""
Bugzilla Bug 28807  
   incremental compilation always fails with NullPointerException in 1.1 beta 2 
"""
makeType("incremental.BasicAspect")
makeType("incremental.Basic")
test(batch=1, changed=["Basic", "BasicAspect"])

makeType("incremental.BasicAspect")
test()

print "done", errorList
sys.exit(0)


"""
Stress testing
"""
N = 2000
l = []
for i in range(N):
	name = "p1.Hello" + str(i)
	makeType(name)
	l.append("Hello" + str(i))

test(batch=1, changed=l)

print "done", errorList
sys.exit(0)


