#!/bin/sh
# awkward script to generate test case for filecompare of 2 different dirs

catExpected() {
  cat<<EOF
1a2
> \a1\a1
12d12
< \b1\b1
23,24d22
< \c1\added
< \c1\c1
424,425d421
< dir.zip!dir/a1/
< dir.zip!dir/a1/a1
468a465
> dir.zip!dir/c1/added
EOF
}

abc="a b c d e f g h i j k l m n o p q r s"
n123="0 1 2 3 4 5 6 7 8 9"
mkdir dir
cd dir
for a in ${abc};  do
  mkdir "$a"
  for n in ${n123} ; do
    echo "$abc $n "
    mkdir "${a}${n}"
    echo "$a$n " > "${a}${n}/${a}${n}"
  done > "$a/$a"
done
cd ..
cp -r dir subdir
mv subdir dir/
cp -r dir dir2
zip -rq one.zip dir/*
# create some differences
rm -rf dir/a1
rm dir2/b1/b1
rm dir2/c1/c1
echo added > dir/c1/added
zip -rq two.zip dir/*
mv one.zip dir/c2/dir.zip
mv two.zip dir2/c2/dir.zip

# save expected and wrap up for checkin
catExpected > expected
zip -qr fileCompareTestDirs.zip expected dir/* dir2/*
