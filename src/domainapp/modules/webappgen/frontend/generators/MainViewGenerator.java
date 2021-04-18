package domainapp.modules.webappgen.frontend.generators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import domainapp.basics.model.config.Configuration;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.util.ApplicationToolKit;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleMain;
import domainapp.modules.mccl.model.MCC;
import org.modeshape.common.text.Inflector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static domainapp.modules.webappgen.frontend.generators.utils.FileUtils.readWholeFile;

/**
 * Generates App.js file with:
 * +- App name
 * +- Welcome text
 * +- Navigation buttons with links based on referred modules
 */
public class MainViewGenerator implements ViewGenerator {
    private static final String templateFile = "react/templates/app.js";

    private final Map<String, String> moduleFileMap = new HashMap<>();
    private final String appName;
    private final String welcomeText;
    private final String template;
    private final Collection<FrontendModuleDescriptor> frontendModuleDescriptors;

    public MainViewGenerator(Class<?> sysConfigClass, Class<?> moduleMainClass, MCC mainMCC) {
        Configuration initConfig = ApplicationToolKit.parseInitApplicationConfiguration(sysConfigClass);
        this.appName = initConfig.getAppName();
        this.welcomeText = "Welcome to " + mainMCC.getPropertyVal("viewDesc", "formTitle").asLiteralStringValueExpr().getValue();
        this.frontendModuleDescriptors = getFrontendModuleDescriptors(
                ApplicationToolKit.parseApplicationModules(sysConfigClass), moduleMainClass);
        this.template = readWholeFile(getClass().getClassLoader().getResource(templateFile).getFile());
    }

    private static Collection<FrontendModuleDescriptor> getFrontendModuleDescriptors(Class[] modules, Class moduleMainClass) {
        return Stream.of(modules)
                .filter(module -> !module.equals(moduleMainClass))
                .map(module -> (ModuleDescriptor)module.getAnnotation(ModuleDescriptor.class))
                .map(ModuleDescriptor::modelDesc)
                .map(ModelDesc::model)
                .map(module -> module.getSimpleName())
                .map(FrontendModuleDescriptor::new)
                .collect(Collectors.toList());
    }

    private Collection<String> generateRouters() {
        return frontendModuleDescriptors.stream()
                .map(moduleDesc ->
                        String.format("<Route path='%s'>" +
                                "<Module%s title='%s' />" +
                                "</Route>",
                                moduleDesc.endpoint,
                                moduleDesc.simpleDClassName,
                                moduleDesc.name))
                .collect(Collectors.toList());
    }

    public ViewGenerator addImports(Map<String, String> moduleFileMap) {
        this.moduleFileMap.putAll(moduleFileMap);
        return this;
    }

    @Override
    public String generate() {
        String imports = this.moduleFileMap.entrySet().stream()
                .map(entry -> String.format(
                        "import %s from '%s';",
                        entry.getKey(),
                        entry.getValue()
                )).reduce("", (s1, s2) -> s1 + "\n" + s2);
        try {
            return imports + this.template
                    .replace("{{ view.main.appName }}", this.appName)
                    .replace("{{ view.main.modules }}", new ObjectMapper().writeValueAsString(this.frontendModuleDescriptors))
                    .replace("{{ view.main.routers }}", String.join("\n", generateRouters()))
                    .replace("{{ view.main.welcome }}", welcomeText);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class FrontendModuleDescriptor {
        private static final Inflector inflector = Inflector.getInstance();
        private final String endpoint;
        private final String name;
        @JsonIgnore
        private final String simpleDClassName;

        public FrontendModuleDescriptor(String simpleDClassName) {
            this.simpleDClassName = simpleDClassName;
            this.endpoint = toHrefString(simpleDClassName);
            this.name = "Manage " + toPluralHumanString(simpleDClassName);
        }

        // href string: /a-simple-string
        private static String toHrefString(String original) {
            return "/" + inflector.pluralize(
                    inflector.humanize(
                        inflector.underscore(original)))
                    .replace(" ", "-").toLowerCase(Locale.ROOT);
        }

        private static String toPluralHumanString(String original) {
            return inflector.capitalize(inflector.pluralize(inflector.humanize(original)));
        }

        public String getName() {
            return name;
        }

        public String getEndpoint() {
            return endpoint;
        }

        @Override
        public String toString() {
            return "FrontendModuleDescriptor{" +
                    "href='" + endpoint + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
