package domainapp.modules.webappgen.backend.generators;

import domainapp.modules.webappgen.backend.utils.InheritanceUtils;
import domainapp.modules.webappgen.backend.annotations.bridges.TargetType;
import domainapp.modules.webappgen.backend.utils.ClassAssocUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings({ "rawtypes" })
public class WebServiceGenerator {

    private final WebControllerGenerator webControllerGenerator;
    private final ServiceTypeGenerator serviceTypeGenerator;
    private final AnnotationGenerator annotationGenerator;
    private final List<Class> generatedControllerClasses;
    private final Map<String, Class> generatedServiceClasses;
    private Consumer<List<Class>> generateCompleteCallback;

    public WebServiceGenerator(
            TargetType targetType,
            GenerationMode generationMode,
            String outputPath) {
        this.webControllerGenerator = WebControllerGenerator.getInstance(generationMode, targetType, outputPath);
        this.serviceTypeGenerator = ServiceTypeGenerator.getInstance(generationMode, outputPath);
        this.annotationGenerator = AnnotationGenerator.instance();

        generatedControllerClasses = new LinkedList<>();
        generatedServiceClasses = new LinkedHashMap<>();
    }

    public void setGenerateCompleteCallback(Consumer<List<Class>> generateCompleteCallback) {
        this.generateCompleteCallback = generateCompleteCallback;
    }

    public Map<String, Class> getGeneratedServiceClasses() {
        return generatedServiceClasses;
    }

    public List<Class> getGeneratedControllerClasses() {
        return generatedControllerClasses;
    }

    /**
     * Generate a simple RESTful Web Service from a number of domain classes.
     * @param classes
     */
    public void generateWebService(Class... classes) {
        List<Class<?>> ignored = getIgnoredClasses(classes);
        Class<?> __;
        for (Class<?> cls : classes) {
            if (ignored.contains(cls)) continue;
            annotationGenerator.generateCircularAnnotations(cls, classes);
            try {
                annotationGenerator.generateInheritanceAnnotations(cls);
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
            __ = serviceTypeGenerator.generateAutowiredServiceType(cls);
            generatedServiceClasses.put(cls.getCanonicalName(), __);
            __ = webControllerGenerator.getRestfulController(cls);
            generatedControllerClasses.add(__);
            List<Class<?>> nestedClasses = ClassAssocUtils.getNested(cls);
            for (Class<?> nested : nestedClasses) {
                if (nested == cls) continue;
                __ = webControllerGenerator.getNestedRestfulController(cls, nested);
                generatedControllerClasses.add(__);
            }
        }
        List<Class> generatedClasses = new ArrayList<>(generatedServiceClasses.values());
        generatedClasses.addAll(generatedControllerClasses);
        onGenerateComplete(generatedClasses);
    }

    /**
     * Ignored classes are subclasses of others.
     */
    private static List<Class<?>> getIgnoredClasses(Class[] classes) {
        return Stream.of(classes).map(c -> InheritanceUtils.getSubtypesOf(c))
                .reduce((l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                }).orElse(List.of());
    }

    private void onGenerateComplete(List<Class> generatedClasses) {
        generateCompleteCallback.accept(generatedClasses);
    }
}
