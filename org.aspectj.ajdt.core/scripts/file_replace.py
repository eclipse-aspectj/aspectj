import os, string

DIR = "c:\\eclipse\\workspace\\weaver"

old_text = "declare dominates:"

new_text = "declare precedence:"


def doit(arg, dirname, filenames):
    for name in filenames:
        fullname = os.path.join(dirname, name)
        if os.path.isfile(fullname):
            do_replace(fullname)

def do_replace(filename):
    if filename.endswith(".py"): return
    
    s = open(filename, 'r')
    text = s.read()
    s.close()

    if string.find(text, old_text) == -1:
        return

    s = open(filename, 'w')
    s.write(text.replace(old_text, new_text))
    s.close()

    print "changed", filename
os.path.walk(DIR, doit, None)
