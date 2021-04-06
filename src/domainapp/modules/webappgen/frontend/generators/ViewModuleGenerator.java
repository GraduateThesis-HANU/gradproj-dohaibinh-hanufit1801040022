package domainapp.modules.webappgen.frontend.generators;

import domainapp.modules.webappgen.frontend.generators.utils.ClassAssocUtils;
import domainapp.modules.webappgen.frontend.generators.utils.DomainTypeRegistry;
import domainapp.modules.mccl.model.MCC;
import org.modeshape.common.text.Inflector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ViewModuleGenerator {
    private final MCC mcc;
    private final String name;
    private final Path moduleSrcDir;
    private final Collection<String> dependentModules;
    private final Collection<String> subModules;
    private final String domainTypeName;
    private BiConsumer<String, String> onWriteCompleted;

    public ViewModuleGenerator(String name,
                               String projectSrcDir,
                               final MCC mcc) {
        this.mcc = mcc;
        this.name = name;

        this.moduleSrcDir = new File(projectSrcDir).toPath()
                .resolve("./" + toHrefString(name));
        System.out.println(moduleSrcDir);

        domainTypeName = mcc.getDomainClass().getName();
        Class<?> domainClass = DomainTypeRegistry.getInstance()
                .getDomainTypeByName(domainTypeName);
        this.subModules = ClassAssocUtils.getNested(domainClass)
                .stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        Collection<String> dependentModules = ClassAssocUtils.getAssociated(domainClass)
                .stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        dependentModules.removeAll(subModules);
        this.dependentModules = dependentModules;
    }

    public ViewModuleGenerator(String name,
                               String projectSrcDir,
                               String domainTypeName) {
        this.name = name;
        this.mcc = null;
        this.moduleSrcDir = new File(projectSrcDir).toPath()
                .resolve("./" + toHrefString(name));
        System.out.println(moduleSrcDir);

        this.domainTypeName = domainTypeName;
        Class<?> domainClass = DomainTypeRegistry.getInstance()
                .getDomainTypeByName(domainTypeName);
        this.subModules = ClassAssocUtils.getNested(domainClass)
                .stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        Collection<String> dependentModules = ClassAssocUtils.getAssociated(domainClass)
                .stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        dependentModules.removeAll(subModules);
        this.dependentModules = dependentModules;
    }

    // href string: /a-simple-string
    private static String toHrefString(String original) {
        final Inflector inflector = Inflector.getInstance();
        return "/" + inflector.humanize(inflector.underscore(original))
                .replace(" ", "-").toLowerCase(Locale.ROOT);
    }

    private static String lowerFirstChar(String original) {
        if (Character.isLowerCase(original.charAt(0))) return original;
        if (original.length() == 1) return "" + Character.toLowerCase(original.charAt(0));
        return Character.toLowerCase(original.charAt(0))
                + original.substring(1);
    }

    private Map<String, ViewGenerator> getOutputViewGenMap() {
        final Map<String, ViewGenerator> outputViewGenMap = new HashMap<>();
        if (mcc != null) {
            // form
            ViewGenerator formGen = new FormGenerator(mcc, FormGenerator.Type.FORM_ONLY);
            outputViewGenMap.put(domainTypeName.concat("Form.js"), formGen);

            // main form
            ViewGenerator mainFormGen = new FormGenerator(mcc, FormGenerator.Type.MAIN);
            outputViewGenMap.put(domainTypeName.concat("MainForm.js"), mainFormGen);

            // list
            ViewGenerator listGen = new ListViewGenerator(mcc, domainTypeName, dependentModules);
            outputViewGenMap.put(domainTypeName.concat("ListView.js"), listGen);

            // list item
            ViewGenerator listItemGen = new ListViewGenerator.ListItemViewGenerator(mcc);
            outputViewGenMap.put(domainTypeName.concat("ListItemView.js"), listItemGen);

            // submodules
            for (String subModuleName : subModules) {
                ViewGenerator submoduleGen = new FormGenerator(
                        subModuleName,
                        FormGenerator.Type.SUBFORM,
                        lowerFirstChar(mcc.getDomainClass().getName()));
                outputViewGenMap.put(subModuleName.concat("Submodule.js"), submoduleGen);
            }

            // index
            ViewGenerator indexGen = new ModuleIndexGenerator(
                    DomainTypeRegistry.getInstance().getDomainTypeByName(this.domainTypeName),
                    ModuleIndexGenerator.Provider.AXIOS);

            outputViewGenMap.put("index.js", indexGen);
        } else {
            // form
            ViewGenerator formGen = new FormGenerator(this.domainTypeName, FormGenerator.Type.FORM_ONLY);
            outputViewGenMap.put(domainTypeName.concat("Form.js"), formGen);

            // main form
            ViewGenerator mainFormGen = new FormGenerator(this.domainTypeName, FormGenerator.Type.MAIN);
            outputViewGenMap.put(domainTypeName.concat("MainForm.js"), mainFormGen);

            // list
            ViewGenerator listGen = new ListViewGenerator(null, domainTypeName, dependentModules);
            outputViewGenMap.put(domainTypeName.concat("ListView.js"), listGen);

            // list item
            ViewGenerator listItemGen = new ListViewGenerator.ListItemViewGenerator(this.domainTypeName);
            outputViewGenMap.put(domainTypeName.concat("ListItemView.js"), listItemGen);

            // submodules
            for (String subModuleName : subModules) {
                ViewGenerator submoduleGen = new FormGenerator(
                        subModuleName,
                        FormGenerator.Type.SUBFORM,
                        lowerFirstChar(domainTypeName));
                outputViewGenMap.put(subModuleName.concat("Submodule.js"), submoduleGen);
            }

            // index
            ViewGenerator indexGen = new ModuleIndexGenerator(
                    DomainTypeRegistry.getInstance().getDomainTypeByName(this.domainTypeName),
                    ModuleIndexGenerator.Provider.AXIOS);
            outputViewGenMap.put("index.js", indexGen);
        }

        return outputViewGenMap;
    }

    private static void createDirIfNotExists(Path path) throws IOException {
        if (Files.exists(path)) return;
        Files.createDirectories(path);
    }

    private static void createFileIfNotExists(Path path) throws IOException {
        if (Files.exists(path)) return;
        Files.createFile(path);
    }

    public void setOnWriteCompleted(BiConsumer<String, String> callback) {
        this.onWriteCompleted = callback;
    }

    public void writeToSourceCode() throws IOException {
        createDirIfNotExists(moduleSrcDir);
        Map<String, ViewGenerator> outputViewGenMap = getOutputViewGenMap();
        for (String fileName : outputViewGenMap.keySet()) {
            Path outputPath = moduleSrcDir.resolve(fileName);
            createFileIfNotExists(outputPath);
            Files.writeString(outputPath,
                    outputViewGenMap.get(fileName)
                            .generate());
        }
        String moduleName = "Module" + name;
        String relativeModulePath = "." + moduleSrcDir.toString().split("\\.")[1];
        if (onWriteCompleted != null) {
            onWriteCompleted.accept(moduleName, relativeModulePath);
        }
    }
}
