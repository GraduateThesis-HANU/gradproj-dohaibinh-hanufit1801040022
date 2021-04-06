package domainapp.modules.webappgen.frontend.examples.modules;

import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.modules.webappgen.frontend.examples.model.Address;
import domainapp.modules.webappgen.frontend.examples.model.Enrolment;
import domainapp.modules.webappgen.frontend.examples.model.Student;

import java.util.Collection;

@ModuleDescriptor(name = "ModuleStudent",
        modelDesc = @ModelDesc(model = Student.class),
        viewDesc = @ViewDesc(formTitle = "Form: Student", imageIcon = "Student.png", domainClassLabel = "Student", view = domainapp.basics.core.View.class), controllerDesc = @domainapp.basics.model.meta.module.controller.ControllerDesc())
public class ModuleStudent {

    @AttributeDesc(label = "Students")
    private String title;

    @AttributeDesc(label = "ID")
    private int id;

    @AttributeDesc(label = "Name")
    private String name;

    @AttributeDesc(label = "Address")
    private Address address;

    @AttributeDesc(label = "enrolments")
    private Collection<Enrolment> enrolments;
}
