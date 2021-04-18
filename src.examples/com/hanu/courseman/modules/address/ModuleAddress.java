package com.hanu.courseman.modules.address;

import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.modules.webappgen.frontend.examples.model.Address;
import domainapp.modules.webappgen.frontend.examples.model.Student;

@ModuleDescriptor(
        name = "ModuleAddress",
        modelDesc = @ModelDesc(model = Address.class),
        viewDesc = @ViewDesc(formTitle = "Form: Address", imageIcon = "Address.png", domainClassLabel = "Address", view = domainapp.basics.core.View.class), controllerDesc = @domainapp.basics.model.meta.module.controller.ControllerDesc())
public class ModuleAddress {

    @AttributeDesc(label = "Address")
    private String title;

    @AttributeDesc(label = "ID")
    private int id;

    @AttributeDesc(label = "City name")
    private String name;

    @AttributeDesc(label = "Student")
    private Student student;
}
