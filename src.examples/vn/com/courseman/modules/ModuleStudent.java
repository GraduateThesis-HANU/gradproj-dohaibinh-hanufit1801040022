package vn.com.courseman.modules;

import java.util.Collection;

import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import vn.com.courseman.model.modulegen.Address;
import vn.com.courseman.model.modulegen.Enrolment;

@ModuleDescriptor(name = "ModuleStudent", modelDesc = @domainapp.basics.model.meta.module.model.ModelDesc(model = vn.com.courseman.model.modulegen.Student.class), viewDesc = @domainapp.basics.model.meta.module.ViewDesc(formTitle = "Form: Student", imageIcon = "Student.png", domainClassLabel = "Student", view = domainapp.basics.core.View.class), controllerDesc = @domainapp.basics.model.meta.module.controller.ControllerDesc())
public class ModuleStudent {

    @AttributeDesc(label = "title")
    private String title;

    @AttributeDesc(label = "id")
    private int id;

    @AttributeDesc(label = "name")
    private String name;

    @AttributeDesc(label = "address")
    private Address address;

    @AttributeDesc(label = "enrolments")
    private Collection<Enrolment> enrolments;
}
