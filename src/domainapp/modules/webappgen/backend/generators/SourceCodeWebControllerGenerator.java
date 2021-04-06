package domainapp.modules.webappgen.backend.generators;

import domainapp.modules.webappgen.backend.annotations.bridges.RestAnnotationAdapter;
import domainapp.modules.webappgen.backend.annotations.bridges.TargetType;
import domainapp.modules.webappgen.backend.base.controllers.NestedRestfulController;
import domainapp.modules.webappgen.backend.base.controllers.RestfulController;

import java.util.HashMap;
import java.util.Map;

final class SourceCodeWebControllerGenerator implements WebControllerGenerator {

    private final Map<String, Class<?>> generatedCrudClasses;
    private final RestAnnotationAdapter annotationAdapter;

    private SourceCodeWebControllerGenerator() {
        generatedCrudClasses = new HashMap<>();
        annotationAdapter = RestAnnotationAdapter.adaptTo(TargetType.SPRING);
    }

    SourceCodeWebControllerGenerator(TargetType targetType) {
        generatedCrudClasses = new HashMap<>();
        annotationAdapter = RestAnnotationAdapter.adaptTo(targetType);
    }

    @Override
    public <T> Class<RestfulController<T>> getRestfulController(Class<T> type) {
        return null;
    }

    @Override
    public <T1, T2> Class<NestedRestfulController<T1, T2>> getNestedRestfulController(Class<T1> outerType, Class<T2> innerType) {
        return null;
    }
}
