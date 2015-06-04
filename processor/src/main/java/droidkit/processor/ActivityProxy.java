package droidkit.processor;

import com.squareup.javapoet.*;
import com.sun.tools.javac.tree.JCTree;
import droidkit.annotation.*;

import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
class ActivityProxy implements IProcessor, JavaClassMaker {

    private final TypeElement mOriginElement;

    private final ClassName mOriginClass;

    private final ViewInjector mViewInjector;

    private final OnClickInjector mOnClickInjector;

    private final OnActionClickInjector mOnActionClickInjector;

    private final LoaderCallbacks mLoaderCallbacks;

    private boolean mDone;

    public ActivityProxy(TypeElement originElement) {
        mOriginElement = originElement;
        mOriginClass = ClassName.get(originElement);
        mViewInjector = new ViewInjector(originElement, mOriginClass);
        mOnClickInjector = new OnClickInjector(mOriginClass, mOriginClass);
        mOnActionClickInjector = new OnActionClickInjector(mOriginClass);
        mLoaderCallbacks = new LoaderCallbacks(originElement);
    }

    @Override
    public void process() {
        if (!mDone) {
            final List<? extends Element> elements = mOriginElement.getEnclosedElements();
            for (final Element element : elements) {
                if (ElementKind.FIELD == element.getKind()) {
                    mViewInjector.tryInject((VariableElement) element, element.getAnnotation(InjectView.class));
                } else if (ElementKind.METHOD == element.getKind()) {
                    final ExecutableElement method = (ExecutableElement) element;
                    mOnClickInjector.tryInject(method, element.getAnnotation(OnClick.class));
                    mOnActionClickInjector.tryInject(method, element.getAnnotation(OnActionClick.class));
                    mLoaderCallbacks.tryInject(method, element.getAnnotation(OnCreateLoader.class));
                    mLoaderCallbacks.tryInject(method, element.getAnnotation(OnLoadFinished.class));
                    mLoaderCallbacks.tryInject(method, element.getAnnotation(OnResetLoader.class));
                }
            }
        }
        mDone = true;
    }

    @Override
    public boolean finishProcessing() {
        try {
            mViewInjector.makeJavaFile();
            mLoaderCallbacks.makeJavaFile();
            makeJavaFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void makeJavaFile() throws IOException {
        final TypeSpec spec = TypeSpec.classBuilder(mOriginElement.getSimpleName() + "$Proxy")
                .superclass(TypeName.get(mOriginElement.getSuperclass()))
                .addFields(mOnClickInjector.fields())
                .addFields(mOnActionClickInjector.fields())
                .addMethod(makeSetContentView1())
                .addMethod(makeSetContentView2())
                .addMethod(makeOnResume())
                .addMethod(makeOnPause())
                .addMethod(makeOnDestroy())
                .addMethod(makeOnOptionsItemSelected())
                .addMethods(mOnClickInjector.setupMethods())
                .addMethods(mOnActionClickInjector.setupMethods())
                .build();
        final JavaFile javaFile = JavaFile.builder(mOriginElement.getEnclosingElement().toString(), spec)
                .addFileComment(AUTO_GENERATED)
                .build();
        final JavaFileObject sourceFile = JCUtils.ENV.getFiler()
                .createSourceFile(javaFile.packageName + "." + spec.name);
        try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
            javaFile.writeTo(writer);
        }
        JCUtils.<JCTree.JCClassDecl>getTree(mOriginElement).extending = JCUtils.ident(spec.name);
    }

    private MethodSpec makeSetContentView1() {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "layoutResId")
                .addStatement("super.setContentView(layoutResId)");
        if (!mViewInjector.isEmpty()) {
            method.addStatement("$T$$ViewInjector.inject(($T) this, ($T) this)",
                    mOriginClass, mOriginClass, mOriginClass);
        }
        for (final MethodSpec setupMethod : mOnClickInjector.setupMethods()) {
            method.addStatement("$N(($T) this, ($T) this)", setupMethod, mOriginClass, mOriginClass);
        }
        for (final MethodSpec setupMethod : mOnActionClickInjector.setupMethods()) {
            method.addStatement("$N(($T) this)", setupMethod, mOriginClass);
        }
        return method.build();
    }

    private MethodSpec makeSetContentView2() {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addStatement("super.setContentView(view)");
        if (!mViewInjector.isEmpty()) {
            method.addStatement("$T$$ViewInjector.inject(($T) this, ($T) this)",
                    mOriginClass, mOriginClass, mOriginClass);
        }
        for (final MethodSpec setupMethod : mOnClickInjector.setupMethods()) {
            method.addStatement("$N(($T) this, ($T) this)", setupMethod, mOriginClass, mOriginClass);
        }
        for (final MethodSpec setupMethod : mOnActionClickInjector.setupMethods()) {
            method.addStatement("$N(($T) this)", setupMethod, mOriginClass);
        }
        return method.build();
    }

    private MethodSpec makeOnResume() {
        return MethodSpec.methodBuilder("onResume")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super.onResume()")
                .addCode(mOnClickInjector.resumeBlock())
                .addCode(mOnActionClickInjector.resumeBlock())
                .build();
    }

    private MethodSpec makeOnPause() {
        return MethodSpec.methodBuilder("onPause")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addCode(mOnClickInjector.pauseBlock())
                .addCode(mOnActionClickInjector.pauseBlock())
                .addStatement("super.onPause()")
                .build();
    }

    private MethodSpec makeOnDestroy() {
        return MethodSpec.methodBuilder("onDestroy")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addCode(mOnClickInjector.destroyBlock())
                .addCode(mOnActionClickInjector.destroyBlock())
                .addStatement("super.onDestroy()")
                .build();
    }

    private MethodSpec makeOnOptionsItemSelected() {
        return MethodSpec.methodBuilder("onOptionsItemSelected")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(ClassName.get("android.view", "MenuItem"), "item")
                .addCode(mOnActionClickInjector.handleClick("item"))
                .addStatement("return super.onOptionsItemSelected(item)")
                .build();
    }

}
