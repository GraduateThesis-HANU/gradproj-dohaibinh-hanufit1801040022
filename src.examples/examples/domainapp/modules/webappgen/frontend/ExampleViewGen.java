package examples.domainapp.modules.webappgen.frontend;

import domainapp.modules.webappgen.frontend.SC1;
import domainapp.modules.webappgen.frontend.examples.model.*;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleAddress;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleCourseModule;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleStudent;
import domainapp.modules.webappgen.frontend.generators.ViewAppGenerator;
import domainapp.modules.webappgen.frontend.generators.utils.DomainTypeRegistry;

import java.io.IOException;

@SuppressWarnings({"unchecked"})
/**
 * NOTE: Kindly copy the /base and /common folder under resources/react folder to your /src folder in your React App.
 * @author binh_dh
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
        Class sccClass = SC1.class;

        ViewAppGenerator generator = new ViewAppGenerator(projectSrcDir, sccClass);
        generator.addModule(Student.class, ModuleStudent.class);
        generator.addModule(CourseModule.class, ModuleCourseModule.class);
        generator.addModule(Address.class, ModuleAddress.class);

        generator.generate();
    }
}
