package domainapp.modules.webappgen.frontend.examples.modules;

import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.modules.webappgen.frontend.examples.model.CourseModule;
import domainapp.modules.webappgen.frontend.examples.model.Enrolment;

import java.util.Collection;

@ModuleDescriptor(name = "ModuleCourseModule",
        modelDesc = @ModelDesc(model = CourseModule.class),
        viewDesc = @ViewDesc(formTitle = "Form: CourseModule", imageIcon = "CourseModule.png", domainClassLabel = "CourseModule", view = domainapp.basics.core.View.class), controllerDesc = @domainapp.basics.model.meta.module.controller.ControllerDesc())
public class ModuleCourseModule {

    @AttributeDesc(label = "Form: CourseModule")
    private String title;

    @AttributeDesc(label = "ID")
    private int id;

    @AttributeDesc(label = "Code")
    private String code;

    @AttributeDesc(label = "Course Name")
    private String name;

    @AttributeDesc(label = "Semester")
    private int semester;

    @AttributeDesc(label = "Credit count")
    private int credits;

    @AttributeDesc(label = "Enrolments")
    private Collection<Enrolment> enrolments;
}
