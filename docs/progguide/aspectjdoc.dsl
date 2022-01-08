<!DOCTYPE style-sheet PUBLIC "-//James Clark//DTD DSSSL Style Sheet//EN" [
  <!ENTITY html-ss PUBLIC "-//Norman Walsh//DOCUMENT DocBook HTML Stylesheet//EN" CDATA DSSSL>
  <!ENTITY print-ss PUBLIC "-//Norman Walsh//DOCUMENT DocBook Print Stylesheet//EN" CDATA DSSSL>
]>

<style-sheet>

<!-- Customizations for the HTML version -->

<style-specification id="html" use="html-stylesheet">
<style-specification-body>

;; Specify the CSS stylesheet to use
(define %stylesheet% "../../style.css")

;; Suppress Lists of Tables, Examples, ...
(define ($generate-book-lot-list$)
  '())

;; Display only the first two section levels in the table of contents
(define (toc-depth nd)
  (if (string=? (gi nd) (normalize "book"))
      2
      1))

;; Make references be appendices (or chapters), not parts.
(define (en-label-number-format-list)
  (list
   (list (normalize "set")		"1")
   (list (normalize "book")		"1")
   (list (normalize "prefix")		"1")
   (list (normalize "part")		"I")
   (list (normalize "chapter")		"1")
   (list (normalize "appendix")		"A")
   ;;(list (normalize "reference")	"1") ; references-as-chapters
   (list (normalize "reference")	"A") ; references-as-appendices
   (list (normalize "example")		"1")
   (list (normalize "figure")		"1")
   (list (normalize "table")		"1")
   (list (normalize "procedure")	"1")
   (list (normalize "step")		"1")
   (list (normalize "refsect1")		"1")
   (list (normalize "refsect2")		"1")
   (list (normalize "refsect3")		"1")
   (list (normalize "sect1")		"1")
   (list (normalize "sect2")		"1")
   (list (normalize "sect3")		"1")
   (list (normalize "sect4")		"1")
   (list (normalize "sect5")		"1")
   (list (normalize "section")		"1")
   ))
  ;;; for references-as-appendices
  (define (reference-number-sibling-list cmp) (list (normalize "appendix")))
  (define (appendix-number-sibling-list cmp)  (list (normalize "reference")))
  ;;; for references-as-chapters
  ;;(define (reference-number-sibling-list cmp) (list (normalize "chapter")))
  ;;(define (chapter-number-sibling-list cmp)  (list (normalize "reference")))


</style-specification-body>
</style-specification>
<external-specification id="html-stylesheet"  document="html-ss">


<!-- Customizations for the print version -->

<style-specification id="print" use="print-stylesheet">
<style-specification-body>

;; Suppress Lists of Tables, Examples, ...
(define ($generate-book-lot-list$)
  '())

;; Display only the first two section levels in the table of contents
(define (toc-depth nd)
  (if (string=? (gi nd) (normalize "book"))
      2
      1))

(define %two-side% #t)
(define bop-footnotes #t)		; doesn't seem to work

;; Make references be appendices (or chapters), not parts.
(define (en-label-number-format-list)
  (list
   (list (normalize "set")		"1")
   (list (normalize "book")		"1")
   (list (normalize "prefix")		"1")
   (list (normalize "part")		"I")
   (list (normalize "chapter")		"1")
   (list (normalize "appendix")		"A")
   ;;(list (normalize "reference")	"1") ; references-as-chapters
   (list (normalize "reference")	"A") ; references-as-appendices
   (list (normalize "example")		"1")
   (list (normalize "figure")		"1")
   (list (normalize "table")		"1")
   (list (normalize "procedure")	"1")
   (list (normalize "step")		"1")
   (list (normalize "refsect1")		"1")
   (list (normalize "refsect2")		"1")
   (list (normalize "refsect3")		"1")
   (list (normalize "sect1")		"1")
   (list (normalize "sect2")		"1")
   (list (normalize "sect3")		"1")
   (list (normalize "sect4")		"1")
   (list (normalize "sect5")		"1")
   (list (normalize "section")		"1")
   ))
  ;;; for references-as-appendices
  (define (reference-number-sibling-list cmp) (list (normalize "appendix")))
  (define (appendix-number-sibling-list cmp)  (list (normalize "reference")))
  ;;; for references-as-chapters
  ;;(define (reference-number-sibling-list cmp) (list (normalize "chapter")))
  ;;(define (chapter-number-sibling-list cmp)  (list (normalize "reference")))

</style-specification-body>
</style-specification>
<external-specification id="print-stylesheet" document="print-ss">

</style-sheet>

<!-- Local Variables -->
<!-- mode: scheme -->
<!-- End -->
