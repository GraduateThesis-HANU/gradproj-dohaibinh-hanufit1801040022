package domainapp.modules.webappgen.frontend.examples.services.student.model;

import domainapp.basics.core.View;
import domainapp.basics.model.config.ApplicationModule.ModuleType;
import domainapp.basics.model.config.view.Region.RegionName;
import domainapp.basics.model.config.view.Region.Type;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.containment.CTree;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc.OpenPolicy;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.core.Controller;
import domainapp.modules.webappgen.frontend.examples.services.enrolment.model.Enrolment;
import domainapp.modules.webappgen.frontend.examples.services.sclass.model.SClass;
import domainapp.view.layout.TwoColumnLayoutBuilder;

import java.util.Collection;
import java.util.Date;

/**
 * @overview
 *  Module for {@link Student}s.
 *
 * @author dmle
 */
@ModuleDescriptor(
        name = "ModuleStudent",
        modelDesc = @ModelDesc(model = Student.class),
        viewDesc = @ViewDesc(
                domainClassLabel = "Student",
                formTitle = "Manage Students",
                imageIcon = "student.jpg",
                viewType = Type.Data,
                parent = RegionName.Tools,
                view = View.class,
                layoutBuilderType = TwoColumnLayoutBuilder.class,
                topX = 0.5, topY = 0.0,
                widthRatio = 0.5f, heightRatio = 0.9f),
        controllerDesc = @ControllerDesc(
                controller = Controller.class,
                openPolicy = OpenPolicy.I_C, // listens to state change event of list field
                isDataFieldStateListener = true),
        containmentTree = @CTree(
                root = Student.class,
                stateScope = { Student.A_id, Student.A_name }),
        type = ModuleType.DomainData,
        isViewer = true,
        isPrimary = true)
public class ModuleStudent {

  // @AttributeDesc(label="Student")
  // private String title;

  @AttributeDesc(label = "Student")
  private String title;

  @AttributeDesc(label = "ID")
  private String id;

  @AttributeDesc(label = "Full name")
  private String name;

  @AttributeDesc(label = "Gender")
  private Gender gender;

  @AttributeDesc(label = "Date of Birth")
  private Date dob;

  @AttributeDesc(label = "Address")
  private City address;

  @AttributeDesc(label = "Email")
  private String email;

  @AttributeDesc(label = "Student class")
  private SClass sclass;

  @AttributeDesc(label = "Enrolments")
  private Collection<Enrolment> enrolments;

}