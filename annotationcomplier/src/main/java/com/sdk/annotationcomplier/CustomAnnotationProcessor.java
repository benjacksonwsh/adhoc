package com.sdk.annotationcomplier;

import com.google.auto.service.AutoService;
import com.sdk.annotation.Hello;
import com.sdk.annotation.ModuleService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;


@AutoService(Processor.class)
public class CustomAnnotationProcessor extends AbstractProcessor {
    private Types mTypeUtils;
    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mTypeUtils = processingEnv.getTypeUtils();
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(ModuleService.class.getCanonicalName());
        annotations.add(Hello.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return true;
        }

        analysisAnnotated(roundEnvironment, ModuleService.class);
        analysisAnnotated(roundEnvironment, Hello.class);
        return true;
    }

    private <A extends Annotation> void analysisAnnotated(RoundEnvironment roundEnvironment, Class<A> cls) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(cls);
        if (elements == null || elements.isEmpty()) {
            return;
        }

        for (Element annotatedElement : elements) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s",
                        ModuleService.class.getSimpleName());
                return ;
            }

            analysisAnnotatedElement(annotatedElement, cls);
        }
    }

    private <A extends Annotation> void analysisAnnotatedElement(Element classElement, Class<A> cls) {
        //A annotation = classElement.getAnnotation(cls);

        String name = classElement.getSimpleName().toString() +"$$Test";
        String text = classElement.getSimpleName().toString();

        StringBuilder builder = new StringBuilder()
                .append("package com.sdk.adhocsdk;\n\n")
                .append("public class ")
                .append(name)
                .append(" {\n\n") // open class
                .append("\tpublic String getMessage() {\n") // open method
                .append("\t\treturn \"");

        // this is appending to the return statement
        builder.append(text).append(name).append(" !\\n");


        builder.append("\";\n") // end return
                .append("\t}\n") // close method
                .append("}\n"); // close class


        try { // write the file
            JavaFileObject source = mFiler.createSourceFile("com.sdk.adhocsdk." + name);
            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
        }
    }

    private void error(Element e, String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    private void info(String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.NOTE,
                String.format(msg, args));
    }


}
