package vn.com.courseman.modules.proper.coursemodule;

import domainapp.basics.core.View;
import domainapp.basics.model.config.view.Region;
import domainapp.basics.model.config.view.Region.RegionName;
import domainapp.basics.model.meta.MetaConstants.AlignmentX;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.SetUpDesc;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.core.Controller;
import domainapp.setup.commands.CopyResourceFilesCommand;
import vn.com.courseman.modules.proper.coursemodule.model.CourseModule;

/**
 * @Overview
 *  Module for {@link CourseModule}
 * 
 * @author dmle
 */
@ModuleDescriptor(name="ModuleCourseModule",
modelDesc=@ModelDesc(
    model=CourseModule.class
),
viewDesc=@ViewDesc(
    formTitle="Manage Course Modules",
    domainClassLabel="Course Module",    
    imageIcon="coursemodule.jpg",
    viewType=Region.Type.Data,
    view=View.class,
    parent=RegionName.Tools
),
controllerDesc=@ControllerDesc(controller=Controller.class),
isPrimary=true
,setUpDesc=@SetUpDesc(postSetUp=CopyResourceFilesCommand.class)
)
public class ModuleCourseModule  {
  @AttributeDesc(label="Course Module")
  private String title;
  
  // attributes
  @AttributeDesc(label="Id",alignX=AlignmentX.Center)
  private int id;
  
  @AttributeDesc(label="Code", alignX=AlignmentX.Center)
  private String code;
  
  @AttributeDesc(label="Name")
  private String name;
  
  @AttributeDesc(label="Semester", alignX=AlignmentX.Center)
  private int semester;
  
  @AttributeDesc(label="Credits", alignX=AlignmentX.Center)
  private int credits;
}
