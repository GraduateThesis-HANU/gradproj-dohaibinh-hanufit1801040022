package domainapp.modules.webappgen.backend.utils;

import java.util.stream.Stream;

public final class PackageUtils {
    public static String basePackageOf(Class<?> cls) {
        return Stream.of(cls.getClassLoader().getDefinedPackages())
            .map(p -> p.getName())
            .filter(cls.getName()::contains)
            .sorted()
            .findFirst().orElse("");
    }
}
