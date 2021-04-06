package domainapp.modules.webappgen.frontend.generators;

import domainapp.modules.webappgen.frontend.generators.utils.ClassAssocUtils;
import domainapp.modules.webappgen.frontend.generators.utils.FileUtils;
import org.modeshape.common.text.Inflector;

import java.util.*;

public class ModuleIndexGenerator implements ViewGenerator {
    private static final String TEMPLATE_FILE = "react/templates/module.js";

    private final String template;
    private final Provider provider;
    private final Collection<String> apiDeclarations;
    private final Collection<String> apiBindings;
    private final Class<?> domainClass;

    public enum Provider {
        FETCH { @Override public String toString() { return "fetch"; } },
        AXIOS { @Override public String toString() { return "axios"; } }
    }

    public ModuleIndexGenerator(Class<?> domainClass, Provider provider) {
        this.apiDeclarations = new ArrayList<>();
        this.apiBindings = new ArrayList<>();
        this.domainClass = domainClass;
        this.template = FileUtils.readWholeFile(
                getClass().getClassLoader().getResource(TEMPLATE_FILE).getFile());
        this.provider = provider;
    }

    private void generateApiDeclarations() {
        final String apiDeclarationTemplate = "const %sAPI = new BaseAPI(\"%s\", providers.%s);";
        final String apiBindingTemplate = "%s={%s}";
        Set<Class<?>> relatedClasses = new HashSet<>(ClassAssocUtils.getAssociated(domainClass));
        relatedClasses.add(domainClass);

        for (Class<?> relatedClass : relatedClasses) {
            // generate an API declaration for it
            String singularName = toLowerSingular(relatedClass.getSimpleName());
            String pluralName = toLowerPlural(relatedClass.getSimpleName());
            String provider = this.provider.toString();
            String key = singularName.concat("API");
            this.apiDeclarations.add(String.format(apiDeclarationTemplate,
                            singularName, pluralName, provider));
            this.apiBindings.add(String.format(apiBindingTemplate, key, key));
        }
    }

    private static String toLowerSingular(String original) {
        return Character.toLowerCase(original.charAt(0))
                + original.substring(1);
    }

    private static String toLowerPlural(String original) {
        final Inflector inflector = Inflector.getInstance();
        return inflector.pluralize(
                inflector.humanize(
                        inflector.underscore(original)))
                .replace(" ", "-").toLowerCase(Locale.ROOT);
    }

    @Override
    public String generate() {
        generateApiDeclarations();
        return template
                .replace("{{ view.apis.declarations }}",
                        String.join("\n", apiDeclarations))
                .replace("{{ view.api.bindings }}",
                        String.join("\n", apiBindings))
                .replace("{{ view.name.module }}",
                        domainClass.getSimpleName().concat("Module"))
                .replace("{{ view.name.main }}",
                        domainClass.getSimpleName().concat("MainForm"))
                .replace("{{ view.api.main }}",
                        lowerFirstChar(domainClass.getSimpleName()).concat("API"));
    }

    private static String lowerFirstChar(String original) {
        if (Character.isLowerCase(original.charAt(0))) return original;
        if (original.length() == 1) return "" + Character.toLowerCase(original.charAt(0));
        return Character.toLowerCase(original.charAt(0))
                + original.substring(1);
    }
}
