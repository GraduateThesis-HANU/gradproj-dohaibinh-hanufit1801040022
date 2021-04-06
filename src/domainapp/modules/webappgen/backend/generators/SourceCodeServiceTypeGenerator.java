package domainapp.modules.webappgen.backend.generators;

import domainapp.modules.webappgen.backend.base.services.CrudService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class SourceCodeServiceTypeGenerator implements ServiceTypeGenerator {

    private static ServiceTypeGenerator INSTANCE;

    public static ServiceTypeGenerator instance() {
        if (INSTANCE == null) {
            INSTANCE = new SourceCodeServiceTypeGenerator();
        }
        return INSTANCE;
    }

    private final Map<String, Class<?>> generatedServices;

    private SourceCodeServiceTypeGenerator() {
        generatedServices = new ConcurrentHashMap<>();
    }

    @Override
    public <T> Class<CrudService<T>> generateAutowiredServiceType(Class<T> type) {
        return null;
    }
}
