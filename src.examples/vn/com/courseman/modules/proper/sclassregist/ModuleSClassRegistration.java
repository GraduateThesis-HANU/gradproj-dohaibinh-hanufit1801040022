package vn.com.courseman.modules.proper.sclassregist;

import domainapp.basics.core.View;
import domainapp.basics.model.config.view.Region;
import domainapp.basics.model.meta.MetaConstants.AlignmentX;
import domainapp.basics.model.meta.Select;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.SetUpDesc;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.basics.view.datafields.JTextField;
import domainapp.basics.view.datafields.list.JComboField;
import domainapp.core.Controller;
import domainapp.db.JFlexiDataSource;
import domainapp.setup.commands.CopyResourceFilesCommand;
import vn.com.courseman.modules.proper.sclass.ModuleSClass;
import vn.com.courseman.modules.proper.sclass.model.SClass;
import vn.com.courseman.modules.proper.sclassregist.model.SClassRegistration;
import vn.com.courseman.modules.proper.student.ModuleStudent;
import vn.com.courseman.modules.proper.student.model.Student;

/**
 * @Overview
 *  Module for {@link SClassRegistration}
 * 
 * @author dmle
 */
@ModuleDescriptor(name="ModuleSClassRegistration",
modelDesc=@ModelDesc(
    model=SClassRegistration.class
),
viewDesc=@ViewDesc(
    formTitle="Manage class registration",
    domainClassLabel="SClass Registration"
    //,imageIcon="-"
    ,imageIcon="sclassRegistration.jpg",
    viewType=Region.Type.Data,
    //parent=RegionName.Tools,
    view=View.class
),
controllerDesc=@ControllerDesc(
    controller=Controller.class,
    isDataFieldStateListener=true
),
isPrimary=true
,childModules={ModuleStudent.class, ModuleSClass.class}
,setUpDesc=@SetUpDesc(postSetUp=CopyResourceFilesCommand.class)
)
public class ModuleSClassRegistration  {

  @AttributeDesc(label="Class Registration")
  private String title;
  
  // attributes
  @AttributeDesc(label="Id", alignX=AlignmentX.Center)
  private int id;
  
  @AttributeDesc(label="Student",
      /* use this config if this module is displayed on menu Tools
      type=JComboField.class,
      // use this if this field is displayed in a JObjectTable
      //width=150,height=25,
      ref=@Select(clazz=Student.class,attributes={"name"}),
      loadOidWithBoundValue=true,  // this must be set to true if displayOidWithBoundValue = true
      displayOidWithBoundValue=true
      */
      /* use this config if this module is NOT displayed on menu Tools AND to be used only as 
       * part of an activity flow where Student is known before-hand.
       * */
      type=JTextField.class
      , editable=false
      ,modelDesc=@ModelDesc(model=Student.class, dataSourceType=JFlexiDataSource.class)
      ,ref=@Select(clazz=Student.class,attributes={"name"})
      )
  private Student student;
  
  @AttributeDesc(label="Class", 
      type=JComboField.class,
      // this is is needed for JObjectTable 
      //width=80,height=25,
      ref=@Select(clazz=SClass.class,attributes={"name"}),
      isStateEventSource=true,      
      alignX=AlignmentX.Center)
  private SClass sClass;  
}
