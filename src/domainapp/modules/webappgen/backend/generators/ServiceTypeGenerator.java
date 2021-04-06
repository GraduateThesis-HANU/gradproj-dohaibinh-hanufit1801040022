package domainapp.modules.webappgen.backend.generators;

import domainapp.modules.webappgen.backend.base.services.CrudService;

public interface ServiceTypeGenerator {
    <T> Class<CrudService<T>> generateAutowiredServiceType(Class<T> type);

    static ServiceTypeGenerator getInstance(GenerationMode mode) {
        switch (mode) {
            case BYTECODE:
                return BytecodeServiceTypeGenerator.instance();
            case SOURCE_CODE:
                return SourceCodeServiceTypeGenerator.instance();
            default:
                throw new IllegalArgumentException();
        }
    }
}
