package com.hanu.courseman.modules.student;

import com.hanu.courseman.modules.address.model.Address;
import com.hanu.courseman.modules.enrolment.model.Enrolment;
import com.hanu.courseman.modules.student.model.Student;
import domainapp.basics.core.View;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;

import java.util.Collection;

/**
 * @overview
 *  Module for {@link Student}s.
 *
 * @author dmle
 */
@ModuleDescriptor(name = "ModuleStudent",
        modelDesc = @ModelDesc(
                model = Student.class),
        viewDesc = @ViewDesc(
                formTitle = "Form: Student",
                imageIcon = "Student.png",
                domainClassLabel = "Student",
                view = View.class),
        controllerDesc = @ControllerDesc())
public class ModuleStudent {
  @AttributeDesc(label = "Manage Students")
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
