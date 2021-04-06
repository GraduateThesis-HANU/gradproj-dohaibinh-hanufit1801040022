package domainapp.modules.webappgen.frontend;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.basics.util.io.ToolkitIO;
import domainapp.modules.common.model.parser.ClassAST;
import domainapp.modules.common.parser.ParserToolkit;
import domainapp.modules.webappgen.frontend.examples.services.student.model.ModuleStudent;
import domainapp.modules.webappgen.frontend.examples.services.student.model.Student;
import domainapp.modules.mccl.model.MCC;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

public class ReadMCC {
    public static void main(String[] args) {
        // change this to a file path
        Path rootSrcPath = new File("/Volumes/Data/Resources/Study/java/domainapp-reactjs/src/main/java").toPath();
        String rootSrcPathStr = rootSrcPath.toString();

        String dclsFqn = Student.class.getCanonicalName();
        String mccFqn = ModuleStudent.class.getCanonicalName();

        String[] cels = dclsFqn.split("\\.");
        String cname = cels[cels.length-1];
        String[] mels = mccFqn.split("\\.");
        String mname = mels[mels.length-1];

        Path dclsFile = ToolkitIO.getPath(rootSrcPathStr, cels);
        Path mccFile = ToolkitIO.getPath(rootSrcPathStr, mels);

        ClassAST dcls = new ClassAST(cname, dclsFile.toString() + ToolkitIO.FILE_JAVA_EXT);
        MCC mcc = new MCC(mname, mccFile.toString() + ToolkitIO.FILE_JAVA_EXT, dcls);

        System.out.printf("%s%n", mcc);

        // get view fields
        Collection<FieldDeclaration> vfields = mcc.getViewFields();
        System.out.printf("View fields: %n");
        for (FieldDeclaration vfield : vfields) {
            System.out.println(vfield);
            NormalAnnotationExpr attribDesc = ParserToolkit.getAnnotation(vfield, AttributeDesc.class);
            System.out.printf(" --> View config: %s%n", attribDesc);
        }
    }
}
