package jawaitasync.processor;

import jawaitasync.Async;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.html.parser.Element;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * https://today.java.net/pub/a/today/2008/04/10/source-code-analysis-using-java-6-compiler-apis.html
 * http://download.forge.objectweb.org/asm/asm4-guide.pdf
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("jawaitasync.Async")
public class AsyncProcessor extends AbstractProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends ExecutableElement> elements = (Set<? extends ExecutableElement>)roundEnv.getElementsAnnotatedWith(Async.class);


		for (ExecutableElement element : elements) {
			element.getTypeParameters()
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "AsyncProcessor.process() : " + element);
		}

		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "AsyncProcessor.process() : " + annotations.size());
		return true;
	}
}
