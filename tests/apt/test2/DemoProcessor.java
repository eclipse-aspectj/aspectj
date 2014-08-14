import java.io.*;
import javax.tools.*;
import javax.tools.Diagnostic.Kind;

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;

@SupportedAnnotationTypes(value= {"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DemoProcessor extends AbstractProcessor {

  private Filer filer;
  private Messager messager;

  @Override
  public void init(ProcessingEnvironment env) {
    filer = env.getFiler();
    messager = env.getMessager();
  }

  @Override
  public boolean process(Set elements, RoundEnvironment env) {
    for (Element element: env.getElementsAnnotatedWith(Marker.class)) {
      if (element.getKind() == ElementKind.METHOD) {
        // Create an aspect targeting this method!
        String methodName = element.getSimpleName().toString();
        String aspectText =
            "public aspect Advise_"+methodName+" {\n"+
                "  before(): execution(* "+methodName+"(..)) {\n"+
                "    System.out.println(\""+methodName+" running\");\n"+
                "  }\n"+
                "}\n";
        try {
          JavaFileObject file = filer.createSourceFile("Advise_"+methodName, element);
          file.openWriter().append(aspectText).close();
        } catch (IOException e) {
          e.printStackTrace();
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "Generated aspect to advise "+element.getSimpleName());
      }
    }
    return true;
  }
}