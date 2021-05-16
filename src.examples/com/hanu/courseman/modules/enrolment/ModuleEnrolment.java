package com.hanu.courseman.modules.enrolment;

import com.hanu.courseman.modules.coursemodule.model.CourseModule;
import com.hanu.courseman.modules.enrolment.model.Enrolment;
import com.hanu.courseman.modules.student.model.Student;
import domainapp.basics.core.View;
import domainapp.basics.model.config.view.Region;
import domainapp.basics.model.config.view.Region.RegionName;
import domainapp.basics.model.meta.MetaConstants.AlignmentX;
import domainapp.basics.model.meta.Select;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.SetUpDesc;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.basics.view.datafields.list.JComboField;
import domainapp.core.Controller;
import domainapp.setup.commands.CopyResourceFilesCommand;

/**
 * @Overview
 *  Module for {@link Enrolment}
 * 
 * @author dmle
 */
@ModuleDescriptor(name="ModuleEnrolment",
modelDesc=@ModelDesc(
    model= Enrolment.class
),
viewDesc=@ViewDesc(
    formTitle="Manage Enrolment",
    domainClassLabel="Enrolment"
    //,imageIcon="-"
    ,imageIcon="enrolment.jpg",
    viewType=Region.Type.Data,
    parent=RegionName.Tools,
    view=View.class
),
controllerDesc=@ControllerDesc(
    controller=Controller.class,
    isDataFieldStateListener=true
),
isPrimary=true
,setUpDesc=@SetUpDesc(postSetUp=CopyResourceFilesCommand.class)
)
public class ModuleEnrolment  {

  @AttributeDesc(label="Manage Enrolments")
  private String title;
  
  // attributes
  @AttributeDesc(label="Id", alignX=AlignmentX.Center)
  private int id;
  
  @AttributeDesc(label="Student",
      type=JComboField.class,
      // use this if this field is displayed in a JObjectTable
      //width=150,height=25,
      ref=@Select(clazz= Student.class,attributes={"name"}),
      loadOidWithBoundValue=true,  // this must be set to true if displayOidWithBoundValue = true
      displayOidWithBoundValue=true)
  private Student student;
  
  @AttributeDesc(label="Course Module", 
      type=JComboField.class,
      // this is is needed for JObjectTable 
      width=80,height=25,
      ref=@Select(clazz= CourseModule.class,attributes={"code"}),
      isStateEventSource=true,      
      alignX=AlignmentX.Center)
  private CourseModule courseModule;
  
  @AttributeDesc(label="Internal Mark", alignX=AlignmentX.Center)
  private double internalMark;
  
  @AttributeDesc(label="Exam Mark", alignX=AlignmentX.Center)
  private double examMark;
  
  @AttributeDesc(label="Final Grade", alignX=AlignmentX.Center)
  private char finalGrade;
}
