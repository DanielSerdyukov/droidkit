package droidkit.javac;

import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
class Utils {

    static final String AUTO_GENERATED_FILE = "AUTO-GENERATED FILE. DO NOT MODIFY.";

    static void error(ProcessingEnvironment processingEnv, String format, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(Locale.US, format, args));
    }

    static void error(ProcessingEnvironment processingEnv, Element element, String format, Object... args) {
        final String message = String.format(Locale.US, format, args);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
        throw new IllegalArgumentException(message);
    }

    static void checkArgument(boolean expression, ProcessingEnvironment processingEnv, Element element, String format,
                              Object... args) {
        if (!expression) {
            error(processingEnv, element, format, args);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T getAnnotationValue(JCTree.JCAnnotation jcAnnotation, String key, T defaultValue) {
        for (final JCTree.JCExpression arg : jcAnnotation.args) {
            if (arg instanceof JCTree.JCAssign) {
                final JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                if (key.equals(assign.lhs.toString())) {
                    if (assign.rhs instanceof JCTree.JCLiteral) {
                        return (T) ((JCTree.JCLiteral) assign.rhs).value;
                    }
                }
            }
        }
        return defaultValue;
    }

    static boolean isSubtype(ProcessingEnvironment processingEnv, Element element, String fqcn) {
        return processingEnv.getTypeUtils().isSubtype(element.asType(),
                processingEnv.getElementUtils().getTypeElement(fqcn).asType());
    }

    static boolean isSubtype(ProcessingEnvironment processingEnv, TypeMirror element, String fqcn) {
        return processingEnv.getTypeUtils().isSubtype(element,
                processingEnv.getElementUtils().getTypeElement(fqcn).asType());
    }

    static boolean isSubtype(ProcessingEnvironment processingEnv, TypeMirror element, Class<?> type) {
        return processingEnv.getTypeUtils().isSubtype(element,
                processingEnv.getElementUtils().getTypeElement(type.getName()).asType());
    }

    static <T> Collection<T> slice(Collection<T> collection, int from) {
        return new ArrayList<>(collection).subList(from, collection.size());
    }



}
