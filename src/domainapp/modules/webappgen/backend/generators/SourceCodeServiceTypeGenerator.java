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
            return (Class) generatedServices.get(genericTypeName);
        }
        Class generated = generateServiceType(type, genericTypeName);
        generatedServices.put(genericTypeName, generated);
        return generated;
    }

    private <T> Class generateServiceType(Class<T> type, String genericTypeName) {
        Class<CrudService> superClass = getSuperClass(type);
        final String pkg = PackageUtils.basePackageFrom(this.outputPackage, type);
        CompilationUnit serviceCompilationUnit =
                SourceCodeGenerators.generateDefaultGenericInherited(
                        pkg, superClass, crudServiceClass, type);

        ClassOrInterfaceDeclaration serviceClassDeclaration =
                (ClassOrInterfaceDeclaration) serviceCompilationUnit.getTypes().get(0);

        // implement constructor
        implementConstructor(type, superClass, serviceClassDeclaration);

        // @Generated annotation
        addGeneratedAnnotation(serviceClassDeclaration);
        String outputFQName = getOutputFQNameFrom(serviceCompilationUnit, serviceClassDeclaration);
        addServiceAnnotation(serviceClassDeclaration, outputFQName);
        addImportsOn(serviceCompilationUnit);

        writeToSource(serviceCompilationUnit, serviceClassDeclaration);

        return compileAndReturn(serviceCompilationUnit, outputFQName);
    }

    private String getOutputFQNameFrom(CompilationUnit serviceCompilationUnit,
                                       ClassOrInterfaceDeclaration serviceClassDeclaration) {
        String outputFQName = serviceCompilationUnit.getPackageDeclaration().get().getNameAsString()
                + "." + serviceClassDeclaration.getNameAsString();
        return outputFQName;
    }

    private <T> Class<CrudService> getSuperClass(Class<T> type) {
        final boolean hasInherit = Modifier.isAbstract(type.getModifiers());
        Class<CrudService> superClass = hasInherit ? absInheritedCrudServiceClass : absCrudServiceClass;
        return superClass;
    }

    private <T> void implementConstructor(Class<T> type, Class<CrudService> superClass, ClassOrInterfaceDeclaration serviceClassDeclaration) {
        ConstructorDeclaration constructorDeclaration =
                SourceCodeGenerators.generateAutowiredConstructor(
                        serviceClassDeclaration, superClass);

        BlockStmt constructorBody = generateConstructorBody(
                type, superClass, constructorDeclaration);
        constructorDeclaration.setBody(constructorBody);
    }

    private Class compileAndReturn(CompilationUnit serviceCompilationUnit, String outputFQName) {
        try {
            Class generated = InMemoryJavaCompiler.newInstance()
                    .ignoreWarnings()
                    .useParentClassLoader(Thread.currentThread().getContextClassLoader())
                    .compile(outputFQName, serviceCompilationUnit.toString());
            return generated;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToSource(CompilationUnit serviceCompilationUnit, ClassOrInterfaceDeclaration serviceClassDeclaration) {
        Path outputPath = Path.of(outputFolder,
                serviceCompilationUnit.getPackageDeclaration().orElse(new PackageDeclaration()).getNameAsString().replace(".", "/"),
                serviceClassDeclaration.getNameAsString() + ".java");
        OutputPathUtils.writeToSource(serviceCompilationUnit, outputPath);
    }

    private void addServiceAnnotation(ClassOrInterfaceDeclaration serviceClassDeclaration, String outputFQName) {
        AnnotationExpr serviceAnnotationExpr = new NormalAnnotationExpr(
                JavaParser.parseName(Service.class.getSimpleName()),
                new NodeList<>(
                        new MemberValuePair("value",
                                new StringLiteralExpr(outputFQName))
                )
        );
        serviceClassDeclaration.addAnnotation(serviceAnnotationExpr);
    }

    private void addGeneratedAnnotation(ClassOrInterfaceDeclaration serviceClassDeclaration) {
        AnnotationExpr generatedAnnotation = new NormalAnnotationExpr(
                JavaParser.parseName(Generated.class.getSimpleName()),
                new NodeList<>(
                        new MemberValuePair("value",
                                new StringLiteralExpr(getClass().getCanonicalName()))));
        serviceClassDeclaration.addAnnotation(generatedAnnotation);
    }

    private void addImportsOn(CompilationUnit serviceCompilationUnit) {
        serviceCompilationUnit.addImport(BiConsumer.class);
        serviceCompilationUnit.addImport(Service.class);

        serviceCompilationUnit.addImport(Qualifier.class);
        serviceCompilationUnit.addImport(InheritanceUtils.class);
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