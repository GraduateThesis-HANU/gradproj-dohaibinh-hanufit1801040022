package vn.com.courseman.modules.simple.address;

import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import vn.com.courseman.modules.simple.student.model.Student;

@ModuleDescriptor(name = "ModuleAddress", modelDesc = @domainapp.basics.model.meta.module.model.ModelDesc(model = vn.com.courseman.modules.simple.address.model.Address.class), viewDesc = @domainapp.basics.model.meta.module.ViewDesc(formTitle = "Form: Address", imageIcon = "Address.png", domainClassLabel = "Address", view = domainapp.basics.core.View.class), controllerDesc = @domainapp.basics.model.meta.module.controller.ControllerDesc())
public class ModuleAddress {

    @AttributeDesc(label = "title")
    private String title;

    @AttributeDesc(label = "id")
    private int id;

    @AttributeDesc(label = "cityName")
    private String cityName;

    @AttributeDesc(label = "student")
    private Student student;
}
