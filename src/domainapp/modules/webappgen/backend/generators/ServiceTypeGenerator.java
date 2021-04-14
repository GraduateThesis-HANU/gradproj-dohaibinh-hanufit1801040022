package domainapp.modules.webappgen.backend.generators;

import domainapp.modules.webappgen.backend.base.services.CrudService;

public interface ServiceTypeGenerator {
    <T> Class<CrudService<T>> generateAutowiredServiceType(Class<T> type);

    static ServiceTypeGenerator getInstance(GenerationMode mode, String outputPackage, Object... args) {
        switch (mode) {
            case BYTECODE:
                return new BytecodeServiceTypeGenerator(outputPackage);
            case SOURCE_CODE:
                return new SourceCodeServiceTypeGenerator(
                        outputPackage, (String) args[0]);
            default:
                throw new IllegalArgumentException();
        }
    }
}
