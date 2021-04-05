package vn.com.courseman.modules.simple.coursemodule;

import java.util.Collection;

import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import vn.com.courseman.modules.simple.enrolment.model.Enrolment;

@ModuleDescriptor(name = "ModuleCourseModule", modelDesc = @domainapp.basics.model.meta.module.model.ModelDesc(model = vn.com.courseman.modules.simple.coursemodule.model.CourseModule.class), viewDesc = @domainapp.basics.model.meta.module.ViewDesc(formTitle = "Form: CourseModule", imageIcon = "CourseModule.png", domainClassLabel = "CourseModule", view = domainapp.basics.core.View.class), controllerDesc = @domainapp.basics.model.meta.module.controller.ControllerDesc())
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

    @AttributeDesc(label = "enrolments")
    private Collection<Enrolment> enrolments;
}
