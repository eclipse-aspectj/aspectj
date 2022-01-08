#/bin/sh

JAVA_HOME="/opt/IBMJava2-13"
DOCBOOK_HOME="/usr/local/docbook"

SAXON="/home/vladimir/aspectj-external-lib/saxon"
XERCES="/usr/local/xerces-1_4_3"

saxon()  { java -cp $SAXON/saxon.jar com.icl.saxon.StyleSheet $*; }
xerces() { java -cp $XERCES/xercesSamples.jar sax.SAXCount -v $* ; }

# echo ""; echo ""
# echo "The following REMARKS still exist:"; echo ""
# egrep -n -A3 "<remark>" *.xml
# echo ""; echo ""

# echo "Checking for required RPMS..."
# for RPM in docbook-dtd docbook-xsl; do
#   rpm -q $RPM >/dev/null
#   if [ $? = 1 ]; then
#     echo "${RPM}: Required RPM not installed. Exiting..."
#     exit 1
#   fi
# done

# echo "Checking for required programs..."
# for PROG in java tex; do
#   type $PROG >/dev/null 2>/dev/null
#   if [ $? = 1 ]; then
#     echo "$prog not found in PATH. Exiting..."
#     exit 1
#   fi
# done

# echo "Checking for required files..."
# for FILE in $JAVA_HOME/jre/lib/ext/saxon.jar; do
#   if [ ! -s  $FILE ]; then
#     echo "$FILE not found. Exiting..."
#     exit 1
#   fi
# done

OPT=$1
shift 1

if [ "$OPT" == "-v" ]; then
  COMMAND="xerces -v progguide.xml"
  echo "Validating the XML source: $COMMAND"
  ${COMMAND}
fi

if [ "$OPT" == "-t" ]; then
  COMMAND='openjade -t tex -d aspectjdoc.dsl#print /usr/share/sgml/xml.dcl progguide.xml'
  echo "Creating TeX from XML: $COMMAND"
  ${COMMAND}
  COMMAND="pdfjadetex progguide.tex"
  echo "Creating PDF from TeX: $COMMAND"
  ${COMMAND}
  ${COMMAND}
  exit
fi

COMMAND="saxon -w0 progguide.xml progguide.html.xsl"
echo "Transforming XML to HTML: $COMMAND"
${COMMAND}

# echo "Transforming XML to FO..."
# saxon -w0 -o progguide.fo progguide.xml ${XSL_STYLESHEET_HOME}/fo/docbook.xsl >progguide.fo.log 2>&1

# echo -n "Transforming FO to PostScript"
# tex --interaction nonstopmode -fmt /usr/local/texmf/tex/xmltex/base/xmltex progguide.fo >|progguide.ps.1.log 2>&1
# echo "Pass 2..."
# tex --interaction nonstopmode -fmt /usr/local/texmf/tex/xmltex/base/xmltex progguide.fo >|progguide.ps.2.log 2>&1
# dvips progguide -o

# echo "Transforming FO to PDF..."
# pdflatex --interaction nonstopmode -fmt /usr/local/texmf/tex/xmltex/base/pdfxmltex progguide.fo >|progguide.pdf.log


