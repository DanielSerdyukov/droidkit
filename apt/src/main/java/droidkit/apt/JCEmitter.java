package droidkit.apt;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.Element;

/**
 * @author Daniel Serdyukov
 */
abstract class JCEmitter {

    public void emitTo(Element element) {
        emitTo(JavacEnv.get().<JCTree.JCClassDecl>getTree(element));
    }

    public void emitTo(JCTree.JCClassDecl classDecl) {
        final ListBuffer<JCTree> defs = new ListBuffer<>();
        defs.addAll(classDecl.defs);
        defs.add(tree());
        classDecl.defs = defs.toList();
    }

    public void emitTo(JCTree.JCMethodDecl methodDecl) {

    }

    public void overrideMethod(JCTree.JCMethodDecl methodDecl) {
        methodDecl.body = JavacEnv.get().maker().Block(0, List.<JCTree.JCStatement>of(this.<JCTree.JCTry>tree()));
    }

    public abstract <T extends JCTree> T tree();

}
