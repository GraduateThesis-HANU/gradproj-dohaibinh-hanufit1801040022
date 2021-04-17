package domainapp.modules.webappgen.backend.generators;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import domainapp.modules.webappgen.backend.base.services.CrudService;
import domainapp.modules.webappgen.backend.base.services.InheritedDomServiceAdapter;
import domainapp.modules.webappgen.backend.base.services.SimpleDomServiceAdapter;
import domainapp.modules.webappgen.backend.utils.InheritanceUtils;
import domainapp.modules.webappgen.backend.utils.OutputPathUtils;
import domainapp.modules.webappgen.backend.utils.PackageUtils;
import examples.domainapp.modules.webappgen.backend.services.coursemodule.model.CourseModule;
import examples.domainapp.modules.webappgen.backend.services.student.model.Student;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Generated;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * @author binh_dh
 */
final class SourceCodeServiceTypeGenerator implements ServiceTypeGenerator {
    private static final Class crudServiceClass = CrudService.class;
    private static final Class absCrudServiceClass = SimpleDomServiceAdapter.class;
    private static final Class absInheritedCrudServiceClass = InheritedDomServiceAdapter.class;

    private final String outputPackage;
    private final String outputFolder;
    private final Map<String, Class<?>> generatedServices;

    SourceCodeServiceTypeGenerator(String outputPackage, String outputFolder) {
        generatedServices = new ConcurrentHashMap<>();
        if (outputPackage.endsWith("services")) {
            this.outputPackage = outputPackage;
        } else {
            this.outputPackage = outputPackage.concat(".services");
        }
        this.outputFolder = outputFolder;
    }

    @Override
    public <T> Class<CrudService<T>> generateAutowiredServiceType(Class<T> type) {
        String genericTypeName = type.getName();

        if (generatedServices.containsKey(genericTypeName)) {
            return (Class<CrudService<T>>)
                    generatedServices.get(genericTypeName);
        }

        return generateServiceType(type, genericTypeName);
    }

    private <T> Class generateServiceType(Class<T> type, String genericTypeName) {
        final boolean hasInherit = Modifier.isAbstract(type.getModifiers());
        Class<CrudService> superClass = hasInherit ? absInheritedCrudServiceClass : absCrudServiceClass;
        final String pkg = PackageUtils.actualOutputPathOf(this.outputPackage, type);
        CompilationUnit serviceCompilationUnit =
                SourceCodeGenerators.generateDefaultGenericInherited(
                        pkg, superClass, crudServiceClass, type);

        ClassOrInterfaceDeclaration serviceClassDeclaration =
                (ClassOrInterfaceDeclaration) serviceCompilationUnit.getTypes().get(0);

        // implement constructor
        ConstructorDeclaration constructorDeclaration =
                SourceCodeGenerators.generateAutowiredConstructor(
                        serviceClassDeclaration, superClass);

        BlockStmt constructorBody = generateConstructorBody(
                type, superClass, constructorDeclaration);
        constructorDeclaration.setBody(constructorBody);

        // @Generated
        AnnotationExpr generatedAnnotation = new NormalAnnotationExpr(
                JavaParser.parseName(Generated.class.getSimpleName()),
                new NodeList<>(
                        new MemberValuePair("value",
                                new StringLiteralExpr(getClass().getCanonicalName()))));
        serviceClassDeclaration.addAnnotation(generatedAnnotation);

        String outputFQName = serviceCompilationUnit.getPackageDeclaration().get().getNameAsString()
                + "." + serviceClassDeclaration.getNameAsString();
        AnnotationExpr serviceAnnotationExpr = new NormalAnnotationExpr(
                JavaParser.parseName(Service.class.getSimpleName()),
                new NodeList<>(
                        new MemberValuePair("value",
                                new StringLiteralExpr(outputFQName))
                )
        );
        serviceCompilationUnit.addImport(BiConsumer.class);
        serviceCompilationUnit.addImport(Service.class.getCanonicalName());
        serviceClassDeclaration.addAnnotation(serviceAnnotationExpr);

        serviceCompilationUnit.addImport(Qualifier.class.getCanonicalName());
        serviceCompilationUnit.addImport(InheritanceUtils.class.getCanonicalName());

        Path outputPath = Path.of(outputFolder,
                serviceCompilationUnit.getPackageDeclaration().orElse(new PackageDeclaration()).getNameAsString().replace(".", "/"),
                serviceClassDeclaration.getNameAsString() + ".java");
        OutputPathUtils.writeToSource(serviceCompilationUnit, outputPath);

        try {
            Class generated = InMemoryJavaCompiler.newInstance()
                    .ignoreWarnings()
                    .useParentClassLoader(Thread.currentThread().getContextClassLoader())
                    .compile(outputFQName, serviceCompilationUnit.toString());
            generatedServices.put(genericTypeName, generated);
            return generated;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BlockStmt generateConstructorBody(Class type,
                                              Class superClass,
                                              ConstructorDeclaration constructorDeclaration) {
        // constructor body
        BlockStmt constructorBody = constructorDeclaration.getBody();

        constructorBody.addStatement(
                new MethodCallExpr(new ThisExpr(), "setType",
                        new NodeList<>(new NameExpr(type.getSimpleName() + ".class"))));

        constructorDeclaration.setBody(constructorBody);
        if (superClass.equals(absInheritedCrudServiceClass)) {
            constructorBody.addStatement(new MethodCallExpr(
                    new ThisExpr(),
                    "setSubtypes",
                    new NodeList<>(new NameExpr("InheritanceUtils.getSubtypeMapFor(this.type)"))));
            Parameter second = constructorDeclaration.getParameter(1);
            AnnotationExpr qualifierAnnotation = new NormalAnnotationExpr(
                    JavaParser.parseName(Qualifier.class.getSimpleName()),
                    new NodeList<>(new MemberValuePair(
                            "value",
                            new StringLiteralExpr(type.getCanonicalName()))));
            second.addAnnotation(qualifierAnnotation);
        }
        return constructorBody;
    }
}

class TestSrcServiceTypeGen {
    public static void main(String[] args) {
        ServiceTypeGenerator generator = new SourceCodeServiceTypeGenerator(
                "examples.domainapp.modules.webappgen.backend.controllers",
                "/Users/binh_dh/Documents/generated");
        System.out.println(generator.generateAutowiredServiceType(Student.class));
        System.out.println(generator.generateAutowiredServiceType(CourseModule.class));
    }
}