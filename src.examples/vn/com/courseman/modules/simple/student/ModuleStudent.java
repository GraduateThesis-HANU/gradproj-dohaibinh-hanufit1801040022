package vn.com.courseman.modules.simple.student;

import java.util.Collection;

import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import vn.com.courseman.modules.simple.address.model.Address;
import vn.com.courseman.modules.simple.enrolment.model.Enrolment;

@ModuleDescriptor(name = "ModuleStudent", modelDesc = @domainapp.basics.model.meta.module.model.ModelDesc(model = vn.com.courseman.modules.simple.student.model.Student.class), viewDesc = @domainapp.basics.model.meta.module.ViewDesc(formTitle = "Form: Student", imageIcon = "Student.png", domainClassLabel = "Student", view = domainapp.basics.core.View.class), controllerDesc = @domainapp.basics.model.meta.module.controller.ControllerDesc())
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
