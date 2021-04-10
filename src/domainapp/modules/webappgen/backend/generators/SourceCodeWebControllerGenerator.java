package domainapp.modules.webappgen.backend.generators;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import domainapp.modules.webappgen.backend.annotations.NestedResourceController;
import domainapp.modules.webappgen.backend.annotations.bridges.AnnotationRep;
import domainapp.modules.webappgen.backend.annotations.bridges.RestAnnotationAdapter;
import domainapp.modules.webappgen.backend.annotations.bridges.TargetType;
import domainapp.modules.webappgen.backend.base.controllers.*;
import domainapp.modules.webappgen.backend.utils.GenericTypeUtils;
import domainapp.modules.webappgen.backend.utils.IdentifierUtils;
import domainapp.modules.webappgen.backend.utils.NamingUtils;
import domainapp.modules.webappgen.backend.utils.OutputPathUtils;
import examples.domainapp.modules.webappgen.backend.services.coursemodule.model.CourseModule;
import examples.domainapp.modules.webappgen.backend.services.enrolment.model.Enrolment;
import examples.domainapp.modules.webappgen.backend.services.student.model.Student;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.modeshape.common.text.Inflector;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Generated;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

final class SourceCodeWebControllerGenerator implements WebControllerGenerator {
    private static final Class<RestfulController> restCtrlClass = RestfulController.class;
    private static final Class<RestfulController> restCtrlClassImpl = (Class) DefaultRestfulController.class;
    private static final Class<RestfulController> inheritRestCtrlClass = (Class) RestfulWithInheritanceController.class;
    private static final Class<RestfulController> inheritRestCtrlClassImpl = (Class) DefaultRestfulWithInheritanceController.class;
    private static final Class nestedRestCtrlClass = NestedRestfulController.class;
    private static final Class nestedRestCtrlImplClass = DefaultNestedRestfulController.class;
    private static final InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();

    private final Map<String, Class<?>> generatedCrudClasses;
    private final RestAnnotationAdapter annotationAdapter;
    private final String outputFolder;

    SourceCodeWebControllerGenerator(String outputFolder) {
        this(TargetType.SPRING, outputFolder);
    }

    SourceCodeWebControllerGenerator(TargetType targetType, String outputFolder) {
        generatedCrudClasses = new HashMap<>();
        annotationAdapter = RestAnnotationAdapter.adaptTo(targetType);
        this.outputFolder = outputFolder;
    }

    @Override
    public <T> Class<RestfulController<T>> getRestfulController(Class<T> type) {
        try {
            String typeName = type.getName();
            if (!generatedCrudClasses.containsKey(typeName)) {
                generatedCrudClasses.put(typeName, generateRestfulController(type));
            }
            return (Class<RestfulController<T>>) generatedCrudClasses.get(typeName);
        } catch (IllegalAccessException | IOException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <T1, T2> Class<NestedRestfulController<T1, T2>> getNestedRestfulController(Class<T1> outerType, Class<T2> innerType) {
        try {
            return generateNestedRestfulController(outerType, innerType);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T1, T2> Class<NestedRestfulController<T1,T2>> generateNestedRestfulController(
            Class<T1> outerType, Class<T2> innerType) {
        //
        final Inflector inflector = Inflector.getInstance();
        final String endpoint =
                "/" + inflector.underscore(inflector.pluralize(outerType.getSimpleName())).replace("_", "-")
                        + "/{id}/" + inflector.underscore(inflector.pluralize(innerType.getSimpleName())).replace("_", "-");
        final String pkg = outerType.getPackage().getName().replace(".model", "");
        final String name = NamingUtils.classNameFrom(pkg, nestedRestCtrlClass, "Controller", outerType, innerType)
                .replace("NestedRestfulController$", "");

        final Class<NestedRestfulController> baseClass = nestedRestCtrlClass;
        final Class<NestedRestfulController> baseImplClass = nestedRestCtrlImplClass;

        final CompilationUnit compilationUnit = SourceCodeGenerators.generateDefaultGenericInherited(
                baseImplClass, baseClass, outerType, innerType
        );

        final ClassOrInterfaceDeclaration classDeclaration =
                compilationUnit.getType(0).asClassOrInterfaceDeclaration();
        classDeclaration.setName(name.substring(name.lastIndexOf(".") + 1)
            .replace("NestedRestfulController$", ""));

        AnnotationRep ann = new AnnotationRep(NestedResourceController.class);
        ann.setValueOf("innerType", inflector.underscore(inflector.pluralize(innerType.getSimpleName())).replace("_", "-"));
        ann.setValueOf("outerType", inflector.underscore(inflector.pluralize(outerType.getSimpleName())).replace("_", "-"));
        annotationAdapter.addSourceAnnotation(ann);
        SourceCodeGenerators.generateAutowiredConstructor(classDeclaration, baseImplClass);

        addAnnotations(baseClass, endpoint, name, compilationUnit, classDeclaration);

        return saveAndReturnClass(pkg, name, compilationUnit, classDeclaration);
    }

    private Class saveAndReturnClass(String pkg, String name, CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classDeclaration) {
        Path outputPath = Path.of(outputFolder,
                compilationUnit.getPackageDeclaration().orElse(new PackageDeclaration()).getNameAsString().replace(".", "/"),
                classDeclaration.getNameAsString() + ".java");
        OutputPathUtils.writeToSource(compilationUnit, outputPath);
        try {
            String className = name.contains(pkg) ? name : pkg.concat(".").concat(name);
            return compiler.ignoreWarnings()
                    .useParentClassLoader(Thread.currentThread().getContextClassLoader())
                    .compile(className, compilationUnit.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Class<RestfulController<T>> generateRestfulController(Class<T> type)
            throws IllegalAccessException, IOException, NoSuchMethodException, SecurityException {
        final boolean hasInheritance = Modifier.isAbstract(type.getModifiers());
        final Class<RestfulController> baseImplClass = hasInheritance ? inheritRestCtrlClassImpl : restCtrlClassImpl;
        final Class<RestfulController> baseClass = hasInheritance ? inheritRestCtrlClass : restCtrlClass;
        final Inflector inflector = Inflector.getInstance();
        final String endpoint = "/" + inflector.underscore(inflector.pluralize(type.getSimpleName())).replace("_", "-");
        final String pkg = type.getPackage().getName().replace(".model", "");
        final String name = NamingUtils.classNameFrom("", restCtrlClass, "Controller", type)
                .replace("RestfulController$", "");

        final CompilationUnit compilationUnit = SourceCodeGenerators.generateDefaultGenericInherited(
                baseImplClass, baseClass, type
        );

        final ClassOrInterfaceDeclaration classDeclaration =
                compilationUnit.getType(0).asClassOrInterfaceDeclaration();
        classDeclaration.setName(classDeclaration.getNameAsString()
                .replace("Service", "Controller"));

        SourceCodeGenerators.generateAutowiredConstructor(classDeclaration, baseImplClass);

        addAnnotations(baseClass, endpoint, name, compilationUnit, classDeclaration);

        return saveAndReturnClass(pkg, name, compilationUnit, classDeclaration);
    }

    private void addAnnotations(Class baseClass, String endpoint, String name, CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classDeclaration) {
        adaptAnnotationsOnClassDeclaration(classDeclaration, baseClass, name);

        AnnotationExpr restCtrlAnnotation = new NormalAnnotationExpr(
                JavaParser.parseName(RestController.class.getCanonicalName()).removeQualifier(),
                new NodeList<>());
        AnnotationExpr requestMappingAnnotation = new NormalAnnotationExpr(
                JavaParser.parseName(RequestMapping.class.getCanonicalName()).removeQualifier(),
                new NodeList<>(
                        new MemberValuePair("value",
                                new StringLiteralExpr(endpoint))
                ));
        compilationUnit.addImport(RestController.class);
        classDeclaration.addAnnotation(restCtrlAnnotation);
        compilationUnit.addImport(RequestMapping.class);
        classDeclaration.addAnnotation(requestMappingAnnotation);

        AnnotationExpr generatedAnnotation = new NormalAnnotationExpr(
                JavaParser.parseName(Generated.class.getSimpleName()),
                new NodeList<>(
                        new MemberValuePair("value",
                                new StringLiteralExpr(getClass().getCanonicalName()))));
        classDeclaration.addAnnotation(generatedAnnotation);
    }

    private static AnnotationExpr from(AnnotationRep annRep) {
        Class<? extends Annotation> annType = (Class) annRep.getAnnotationClass();
        NormalAnnotationExpr builder = new NormalAnnotationExpr(
                JavaParser.parseName(annType.getCanonicalName()),
                new NodeList<>()
        );
        for (Map.Entry<String, Object> entry : annRep.getValues().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Class type = value.getClass();
            if (type.isAssignableFrom(String.class)) {
                builder.addPair(key, new StringLiteralExpr((String) value));
            } else if (type == Boolean.class || type == Boolean.TYPE
                    || type == Short.class || type == Short.TYPE
                    || type == Long.class || type == Long.TYPE
                    || type == Integer.class || type == Integer.TYPE
                    || type == Float.class || type == Float.TYPE
                    || type == Double.class || type == Double.TYPE
                    || type == Byte.class || type == Byte.TYPE) {
                builder.addPair(key, value.toString());
            } else if (type == String[].class) {
                String[] array = (String[]) value;
                if (array.length == 1)
                    builder.addPair(key, new StringLiteralExpr(array[0]));
            } else {
                // not working yet
            }
        }
        return builder;
    }

    private ClassOrInterfaceDeclaration adaptAnnotationsOnClassDeclaration(
            ClassOrInterfaceDeclaration classDeclaration,
            Class baseInterface,
            String currentName) {

        for (Method baseMethod : baseInterface.getMethods()) {
            MethodDeclaration method = classDeclaration.getMethodsByName(baseMethod.getName()).get(0);

            List<AnnotationExpr> adaptedAnnotations =
                    adaptAnnotations(baseMethod.getAnnotations(), currentName)
                            .stream()
                            .map(SourceCodeWebControllerGenerator::from)
                            .distinct()
                            .collect(Collectors.toList());
            for (AnnotationExpr adaptedAnnotation : adaptedAnnotations) {
                method.addAnnotation(adaptedAnnotation);
            }

            Parameter[] parameters = baseMethod.getParameters();
            final AtomicInteger counter = new AtomicInteger(0);
            for (Parameter p : parameters) {
                com.github.javaparser.ast.body.Parameter currentParam =
                        method.getParameter(counter.getAndIncrement());
                Set<AnnotationExpr> parameterAnnotations =
                        adaptAnnotations(p.getAnnotations(), currentName)
                                .stream().map(SourceCodeWebControllerGenerator::from)
                                .collect(Collectors.toSet());
                parameterAnnotations.forEach(currentParam::addAnnotation);
            }
        }
        return classDeclaration;
    }

    private List<AnnotationRep> adaptAnnotations(Annotation[] annotations, String className) {
        List<AnnotationRep> adaptedAnnotations = new LinkedList<>();
        for (Annotation ann : annotations) {
            List<AnnotationRep> annReps = adaptAnnotation(ann, className);
            if (annReps == null)
                continue;
            adaptedAnnotations.addAll(annReps);
        }
        return adaptedAnnotations;
    }

    private List<AnnotationRep> adaptAnnotation(Annotation ann, String className) {
        Class<Annotation> annType = (Class) ann.annotationType();
        AnnotationRep annRep = new AnnotationRep(annType);
        for (Method m : annType.getDeclaredMethods()) {
            try {
                annRep.setValueOf(m.getName(), m.invoke(ann));
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        annRep.setValueOf("declaredOn", className);
        this.annotationAdapter.addSourceAnnotation(annRep);
        return annotationAdapter.getTargetAnnotations(annType);
    }

}

class TestSrcWebCtrlGen {
    public static void main(String[] args) {
            WebControllerGenerator generator = new SourceCodeWebControllerGenerator("/Users/binh_dh/Documents/generated");
        System.out.println(generator.getRestfulController(Student.class));
        System.out.println(generator.getRestfulController(CourseModule.class));
        System.out.println(generator.getNestedRestfulController(Student.class, Enrolment.class));
    }
}
