package domainapp.modules.webappgen.frontend.examples;

import domainapp.modules.webappgen.frontend.SC1;
import domainapp.modules.webappgen.frontend.examples.model.*;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleAddress;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleCourseModule;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleMain;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleStudent;
import domainapp.modules.webappgen.frontend.generators.MainViewGenerator;
import domainapp.modules.webappgen.frontend.generators.ViewModuleGenerator;
import domainapp.modules.webappgen.frontend.generators.utils.ClassAssocUtils;
import domainapp.modules.webappgen.frontend.generators.utils.DomainTypeRegistry;
import domainapp.modules.webappgen.frontend.generators.utils.MCCUtils;
import domainapp.modules.mccl.model.MCC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked"})
/**
 * NOTE: Kindly copy the /base and /common folder under resources/react folder to your /src folder in your React App.
 */
public class ExampleViewGen {
    static final Class<?>[] models = {
            Student.class,
            Address.class,
            Gender.class,
            Enrolment.class,
            SClass.class,
            CourseModule.class,
            ElectiveModule.class,
            CompulsoryModule.class
    };

    static {
        DomainTypeRegistry.getInstance().addDomainTypes(models);
    }

    public static void main(String[] args) throws IOException {
        // OUTPUT DIR
        final String projectSrcDir = "/Users/binh_dh/vscode/courseman-examples-2/src";

        final Map<Class, Class> domainModuleClassMap = new HashMap<>();
        domainModuleClassMap.put(Student.class, ModuleStudent.class);
        domainModuleClassMap.put(CourseModule.class, ModuleCourseModule.class);
        domainModuleClassMap.put(Address.class, ModuleAddress.class);

        final Map<String, String> mainImports = new HashMap<>();
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

        Class sccClass = SC1.class;
        MCC mccMain = MCCUtils.readMCC(null, ModuleMain.class);
        String generatedMain = new MainViewGenerator(sccClass, mccMain)
                .addImports(mainImports)
                .generate();

        Path appClassFile = new File(projectSrcDir).toPath().resolve("./App.js");
        if (!Files.exists(appClassFile)) {
            Files.createFile(appClassFile);
        }
        Files.writeString(appClassFile, generatedMain);
    }
}
