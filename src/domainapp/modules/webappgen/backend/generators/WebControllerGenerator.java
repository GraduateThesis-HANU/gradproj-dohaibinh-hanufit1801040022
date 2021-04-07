package domainapp.modules.webappgen.backend.generators;

import domainapp.modules.webappgen.backend.annotations.bridges.TargetType;
import domainapp.modules.webappgen.backend.base.controllers.NestedRestfulController;
import domainapp.modules.webappgen.backend.base.controllers.RestfulController;

public interface WebControllerGenerator {
    <T> Class<RestfulController<T>> getRestfulController(Class<T> type);
    <T1, T2> Class<NestedRestfulController<T1, T2>> getNestedRestfulController(
        Class<T1> outerType, Class<T2> innerType);

    static WebControllerGenerator getInstance(GenerationMode mode, Object... args) {
        switch (mode) {
            case BYTECODE:
                return new BytecodeWebControllerGenerator((TargetType) args[0]);
            case SOURCE_CODE:
                return new SourceCodeWebControllerGenerator((TargetType) args[0], (String) args[1]);
            default:
                throw new IllegalArgumentException();
        }
    }
}
