package domainapp.modules.webappgen.frontend.generators;

import domainapp.modules.mccl.model.MCC;
import domainapp.modules.webappgen.frontend.generators.utils.ClassAssocUtils;
import domainapp.modules.webappgen.frontend.generators.utils.MCCUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAppGenerator {
    private final String projectSrcDir;
    private final Class sccClass;
    private final Map<Class, Class> domainModuleClassMap;
    private final Map<String, String> mainImports;
    private final Class moduleMainClass;

    public ViewAppGenerator(String projectSrcDir, Class sccClass, Class moduleMainClass) {
        this.projectSrcDir = projectSrcDir;
        this.sccClass = sccClass;
        this.domainModuleClassMap = new HashMap<>();
        this.mainImports = new HashMap<>();
        this.moduleMainClass = moduleMainClass;
    }

    public void addModule(Class domain, Class module) {
        this.domainModuleClassMap.put(domain, module);
    }

    private void generateViewModules() throws IOException {
        for (Map.Entry<Class, Class> entry : domainModuleClassMap.entrySet()) {
            Class domainClass = entry.getKey();
            Class moduleClass = entry.getValue();
            MCC mcc = MCCUtils.readMCC(domainClass, moduleClass);

            List<Class<?>> relatedClasses = ClassAssocUtils.getAssociated(domainClass);
            // generate related module
            for (Class<?> cls : relatedClasses) {
                if (mainImports.containsKey("Module" + cls.getSimpleName())) {
                    continue;
                }
                ViewModuleGenerator submoduleGenerator = new ViewModuleGenerator(
                        cls.getSimpleName(), projectSrcDir, cls.getSimpleName());
                submoduleGenerator.writeToSourceCode();
            }

            ViewModuleGenerator generator = new ViewModuleGenerator(
                    domainClass.getSimpleName(), projectSrcDir, mcc);

            generator.setOnWriteCompleted(mainImports::put);
            generator.writeToSourceCode();
        }
    }

    private void generateAndSave() throws IOException {
        MCC mccMain = MCCUtils.readMCC(null, moduleMainClass);
        String generatedMain = new MainViewGenerator(sccClass, moduleMainClass, mccMain)
                .addImports(mainImports)
                .generate();

        Path appClassFile = new File(projectSrcDir).toPath().resolve("./App.js");
        if (!Files.exists(appClassFile)) {
            Files.createFile(appClassFile);
        }
        Files.writeString(appClassFile, generatedMain);
    }

    public void generate() {
        try {
            generateViewModules();
            generateAndSave();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
