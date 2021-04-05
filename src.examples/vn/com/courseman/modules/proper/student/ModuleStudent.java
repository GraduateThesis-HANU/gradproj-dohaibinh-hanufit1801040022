package vn.com.courseman.modules.proper.student;

import java.util.Collection;

import domainapp.basics.core.View;
import domainapp.basics.model.config.ApplicationModule.ModuleType;
import domainapp.basics.model.config.view.Region.RegionName;
import domainapp.basics.model.config.view.Region.Type;
import domainapp.basics.model.meta.MetaConstants.AlignmentX;
import domainapp.basics.model.meta.Select;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.containment.CTree;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc.OpenPolicy;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.basics.view.datafields.list.JListField;
import domainapp.core.Controller;
import domainapp.view.layout.TwoColumnLayoutBuilder;
import vn.com.courseman.modules.proper.coursemodule.ModuleCourseModule;
import vn.com.courseman.modules.proper.coursemodule.model.CourseModule;
import vn.com.courseman.modules.proper.enrolment.ModuleEnrolment;
import vn.com.courseman.modules.proper.student.model.Student;

/**
 * @overview
 *  Module for {@link Student}s. 
 *  
 * @author dmle
 */
@ModuleDescriptor(
name="ModuleStudent",
modelDesc=@ModelDesc(
    model=Student.class
),
viewDesc=@ViewDesc(
    domainClassLabel="Student",
    formTitle="Manage Students", 
    imageIcon="student.jpg",
    viewType=Type.Data,
    parent=RegionName.Tools,
    view=View.class,
    layoutBuilderType=TwoColumnLayoutBuilder.class,
    topX=0.5,topY=0.0,widthRatio=0.5f,heightRatio=0.9f
),
controllerDesc=@ControllerDesc(
    controller=Controller.class,
    openPolicy=OpenPolicy.I_C
    ,isDataFieldStateListener=true  // listens to state change event of list field
)
,containmentTree=@CTree(
    root=Student.class,
    stateScope={Student.A_id, Student.A_name, Student.A_modules}
)
,type=ModuleType.DomainData,
isViewer=true,isPrimary=true,
childModules={ ModuleEnrolment.class, ModuleCourseModule.class }
)
public class ModuleStudent {
  @AttributeDesc(label="Student")
  private String title;

  @AttributeDesc(label="Id",alignX=AlignmentX.Center)
  private int id;

  @AttributeDesc(label="Full name",alignX=AlignmentX.Center)
  private String name;

  @AttributeDesc(label="Needs help?"
      ,alignX=AlignmentX.Center
      ,isStateEventSource=true)
  private boolean helpRequested;
  
  @AttributeDesc(label="Enrols Into",
      type=JListField.class
      //v5.1: this configuration is no longer needed:
      //  ,modelDesc=@ModelDesc(model=CourseModule.class)
      ,ref=@Select(clazz=CourseModule.class,attributes={"name"})
      ,isStateEventSource=true
      ,width=100,height=5
  )
  private Collection<CourseModule> modules;
}