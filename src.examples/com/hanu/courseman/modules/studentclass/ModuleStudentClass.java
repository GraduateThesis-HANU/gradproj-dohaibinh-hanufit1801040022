package com.hanu.courseman.modules.studentclass;

import com.hanu.courseman.modules.studentclass.model.StudentClass;
import com.hanu.courseman.modules.student.ModuleStudent;
import com.hanu.courseman.modules.student.model.Student;
import domainapp.basics.core.View;
import domainapp.basics.model.config.view.Region;
import domainapp.basics.model.config.view.Region.RegionName;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.SetUpDesc;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc.OpenPolicy;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.basics.view.panels.DefaultPanel;
import domainapp.core.Controller;
import domainapp.setup.commands.CopyResourceFilesCommand;

import java.util.List;

/**
 * @overview 
 *  Represents the module descriptor of the software module that manages the domain objects of 
 *  <tt>SClass</tt>
 * 
 * @author dmle
 */
@ModuleDescriptor(name="ModuleSClass",
modelDesc=@ModelDesc(
    model= StudentClass.class
),
viewDesc=@ViewDesc(
    formTitle="Manage Student Classes",
    domainClassLabel="Student Class",
    imageIcon="sclass.jpg",
    view=View.class,
    viewType=Region.Type.Data,
    parent=RegionName.Tools
),
  controllerDesc=@ControllerDesc(
      controller=Controller.class,
      openPolicy=OpenPolicy.O_C
      ),  // v2.6.4b
  isPrimary=true,
  childModules={ModuleStudent.class}
  ,setUpDesc=@SetUpDesc(postSetUp=CopyResourceFilesCommand.class)
)
public class ModuleStudentClass {
  @AttributeDesc(label="Student Class")
  private String title;

  @AttributeDesc(label="Id")
  private int id;

  @AttributeDesc(label="Name")
  private String name;
  
  @AttributeDesc(label="Students",type= DefaultPanel.class,
      controllerDesc=@ControllerDesc(
          //openPolicy=OpenPolicy.O
          /*testing: load all student objects with children*/
          openPolicy=OpenPolicy.L_C
        )
      )
  private List<Student> students;
}
