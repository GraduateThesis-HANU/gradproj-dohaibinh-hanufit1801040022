package domainapp.modules.webappgen.backend.generators;

import domainapp.modules.webappgen.backend.base.services.CrudService;

public interface ServiceTypeGenerator {
    <T> Class<CrudService<T>> generateAutowiredServiceType(Class<T> type);

    static ServiceTypeGenerator getInstance(GenerationMode mode, Object... args) {
        switch (mode) {
            case BYTECODE:
                return BytecodeServiceTypeGenerator.instance();
            case SOURCE_CODE:
                return new SourceCodeServiceTypeGenerator((String) args[0]);
            default:
                throw new IllegalArgumentException();
        }
    }
}
