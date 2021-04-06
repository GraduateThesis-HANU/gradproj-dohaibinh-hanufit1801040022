package domainapp.modules.webappgen.frontend.examples.services.coursemodule.model;

import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;

@ModuleDescriptor(name = "ModuleCourseModule",
        modelDesc = @ModelDesc(model = CourseModule.class),
        viewDesc = @ViewDesc(
                formTitle = "Form: CourseModule",
                imageIcon = "CourseModule.png",
                domainClassLabel = "CourseModule",
                view = domainapp.basics.core.View.class),
        controllerDesc = @ControllerDesc())
public class ModuleCourseModule {

    @AttributeDesc(label = "title")
    private String title;

    @AttributeDesc(label = "id")
    private int id;

    @AttributeDesc(label = "code")
    private String code;

    @AttributeDesc(label = "name")
    private String name;

    @AttributeDesc(label = "semester")
    private int semester;

    @AttributeDesc(label = "credits")
    private int credits;

//    @AttributeDesc(label = "enrolments")
//    private Collection<Enrolment> enrolments;
}