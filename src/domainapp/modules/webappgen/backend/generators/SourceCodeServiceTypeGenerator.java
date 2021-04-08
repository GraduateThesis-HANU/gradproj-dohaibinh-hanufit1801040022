package domainapp.modules.webappgen.backend.generators;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import domainapp.modules.webappgen.backend.base.services.CrudService;
import domainapp.modules.webappgen.backend.base.services.InheritedDomServiceAdapter;
import domainapp.modules.webappgen.backend.base.services.SimpleDomServiceAdapter;
import domainapp.modules.webappgen.backend.utils.InheritanceUtils;
import domainapp.modules.webappgen.backend.utils.OutputPathUtils;
import examples.domainapp.modules.webappgen.backend.services.student.model.Student;
import examples.domainapp.modules.webappgen.backend.services.coursemodule.model.CourseModule;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Generated;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

final class SourceCodeServiceTypeGenerator implements ServiceTypeGenerator {
    private static final Class crudServiceClass = CrudService.class;
    private static final Class absCrudServiceClass = SimpleDomServiceAdapter.class;
    private static final Class absInheritedCrudServiceClass = InheritedDomServiceAdapter.class;

    private final String outputFolder;
    private final Map<String, Class<?>> generatedServices;

    SourceCodeServiceTypeGenerator(String outputFolder) {
        generatedServices = new ConcurrentHashMap<>();
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
        final boolean hasInherit = java.lang.reflect.Modifier.isAbstract(type.getModifiers());
        Class<CrudService> superClass = hasInherit ? absInheritedCrudServiceClass : absCrudServiceClass;

        CompilationUnit serviceCompilationUnit =
                SourceCodeGenerators.generateDefaultGenericInherited(
                        superClass, crudServiceClass, type);

        ClassOrInterfaceDeclaration serviceClassDeclaration =
                (ClassOrInterfaceDeclaration) serviceCompilationUnit.getTypes().get(0);

        // implement constructor
        generateAutowiredConstructor(type, serviceClassDeclaration, superClass);

        String outputFQName = serviceCompilationUnit.getPackageDeclaration().get().getNameAsString()
                + "." + serviceClassDeclaration.getNameAsString();
        AnnotationExpr serviceAnnotationExpr = new NormalAnnotationExpr(
                JavaParser.parseName(Service.class.getSimpleName()),
                new NodeList<>(
                        new MemberValuePair("value",
                                new StringLiteralExpr(outputFQName))
                )
        );
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

    private <T> void generateAutowiredConstructor(Class<T> type,
                                                  ClassOrInterfaceDeclaration serviceClassDeclaration,
                                                  Class superClass) {
        final AtomicInteger counter = new AtomicInteger(0);
        ConstructorDeclaration constructorDeclaration = serviceClassDeclaration.addConstructor(Modifier.PUBLIC);
        counter.set(0);
        for (Class parameterType : superClass.getConstructors()[0].getParameterTypes()) {
            if (parameterType == Class.class) continue;
            Parameter parameter = new Parameter(
                    JavaParser.parseType(parameterType.getCanonicalName()),
                    "arg" + counter.getAndIncrement()
            );
            constructorDeclaration.addParameter(parameter);
        }

        BlockStmt constructorBody = new BlockStmt();
        // super call
        ExplicitConstructorInvocationStmt superConstructorCall
                = new ExplicitConstructorInvocationStmt(
                false, null, new NodeList<>(
                constructorDeclaration.getParameters()
                        .stream().map(Parameter::getNameAsExpression)
                        .collect(Collectors.toList())));
        constructorBody.addStatement(superConstructorCall);
        constructorBody.addStatement(
                new MethodCallExpr(new ThisExpr(), "setType",
                        new NodeList<>(new NameExpr(type.getSimpleName() + ".class"))));

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

        constructorDeclaration.setBody(constructorBody);
        AnnotationExpr autowiredAnnotation = new NormalAnnotationExpr(
                JavaParser.parseName(Autowired.class.getSimpleName()),
                new NodeList<>());
        constructorDeclaration.addAnnotation(autowiredAnnotation);

        AnnotationExpr generatedAnnotation = new NormalAnnotationExpr(
                JavaParser.parseName(Generated.class.getSimpleName()),
                new NodeList<>(
                        new MemberValuePair("value",
                                new StringLiteralExpr(getClass().getCanonicalName()))));
        serviceClassDeclaration.addAnnotation(generatedAnnotation);
    }
}

class TestSrcServiceTypeGen {
    public static void main(String[] args) {
        ServiceTypeGenerator generator = new SourceCodeServiceTypeGenerator("/Users/binh_dh/Documents/generated");
        System.out.println(generator.generateAutowiredServiceType(Student.class));
        System.out.println(generator.generateAutowiredServiceType(CourseModule.class));
    }
}