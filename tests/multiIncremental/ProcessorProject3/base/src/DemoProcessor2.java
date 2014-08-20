import java.io.*;
import javax.tools.*;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;

@SupportedAnnotationTypes(value= {"java.lang.SuppressWarnings"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DemoProcessor2 extends AbstractProcessor { 

	private Filer filer;

	@Override
	public void init(ProcessingEnvironment env) {
		filer = env.getFiler();
	}

	@Override
	public boolean process(Set elements, RoundEnvironment env) {
System.out.println("Processor (around) running");
		// Discover anything marked with @SuppressWarnings
		for (Element element: env.getElementsAnnotatedWith(SuppressWarnings.class)) {
			if (element.getKind() == ElementKind.METHOD) {
				// For any methods we find, create an aspect:
				String methodName = element.getSimpleName().toString();
				String aspectText = 
						"public aspect AroundAdvise_"+methodName+" {\n"+
						"  void around(): execution(* "+methodName+"(..)) {\n"+
						"    System.out.println(\"Around advice on "+methodName+" running\");\n"+
						"  }\n"+
						"}\n";
				try {
					JavaFileObject file = filer.createSourceFile("AroundAdvise_"+methodName, element);
					file.openWriter().append(aspectText).close();
					System.out.println("Generated aspect with around advice to advise "+element.getSimpleName());
				} catch (IOException ioe) {
					// already creates message can appear if processor runs more than once
					if (!ioe.getMessage().contains("already created")) {
						ioe.printStackTrace();
					}
				}
			}
		}
		return false;
	}
}
