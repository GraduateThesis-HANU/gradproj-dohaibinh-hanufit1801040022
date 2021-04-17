package examples.domainapp.modules.webappgen.frontend;

import domainapp.modules.webappgen.frontend.examples.model.*;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleCourseModule;
import domainapp.modules.webappgen.frontend.generators.ModuleIndexGenerator;
import domainapp.modules.webappgen.frontend.generators.utils.DomainTypeRegistry;
import domainapp.modules.webappgen.frontend.generators.utils.MCCUtils;
import domainapp.modules.mccl.model.MCC;

public class CourseModuleReactViewGen {
    static final Class<?>[] models = {
            Student.class,
            Gender.class,
            Enrolment.class,
            SClass.class,
            CourseModule.class,
            CompulsoryModule.class,
            ElectiveModule.class
    };

    static {
        DomainTypeRegistry.getInstance().addDomainTypes(models);
    }
    public static void main(String[] args) {
        MCC mcc = MCCUtils.readMCC(CourseModule.class, ModuleCourseModule.class);

//        String studentCreateForm = new FormGenerator(
//                mcc, FormGenerator.Type.SUBFORM)
//                .generate();
//        System.out.println(studentCreateForm);

//        System.out.println(
//                new ListViewGenerator(mcc, "Student", List.of()).generate()
//        );

        System.out.println(new ModuleIndexGenerator(CourseModule.class,
                ModuleIndexGenerator.Provider.AXIOS).generate());

//        System.out.println(
//                new FormGenerator(mcc, FormGenerator.Type.MAIN)
//                    .generate()
//        );
    }
}
