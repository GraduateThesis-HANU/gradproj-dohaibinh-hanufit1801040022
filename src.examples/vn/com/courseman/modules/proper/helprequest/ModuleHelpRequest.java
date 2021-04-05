package vn.com.courseman.modules.proper.helprequest;

import domainapp.basics.core.View;
import domainapp.basics.model.config.ApplicationModule.ModuleType;
import domainapp.basics.model.config.view.Region.Type;
import domainapp.basics.model.meta.MetaConstants.AlignmentX;
import domainapp.basics.model.meta.Select;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.basics.view.datafields.JTextField;
import domainapp.core.Controller;
import domainapp.db.JFlexiDataSource;
import domainapp.view.layout.TwoColumnLayoutBuilder;
import vn.com.courseman.modules.proper.helprequest.model.HelpRequest;
import vn.com.courseman.modules.proper.student.model.Student;

/**
 * @overview
 *  Module for {@link HelpRequest} (HelpRequest)
 *  
 * @author dmle
 */
@ModuleDescriptor(
name="ModuleHelpRequest",
modelDesc=@ModelDesc(
    model=HelpRequest.class
),
viewDesc=@ViewDesc(
    domainClassLabel="Help Request",
    formTitle="Help Request", 
    imageIcon="helprequest.jpg",
    viewType=Type.Data,
    //parent=RegionName.Tools,
    view=View.class,
    layoutBuilderType=TwoColumnLayoutBuilder.class,
    topX=0.5,topY=0.0,widthRatio=0.5f,heightRatio=0.9f
),
controllerDesc=@ControllerDesc(
    controller=Controller.class
    // openPolicy=OpenPolicy.I_C
    //,isDataFieldStateListener=true  // listens to state change event of list field
),
type=ModuleType.DomainData,
isPrimary=true
)
public class ModuleHelpRequest {
  @AttributeDesc(label="Help Request")
  private String title;

  @AttributeDesc(label="Id",alignX=AlignmentX.Center)
  private int id;

  @AttributeDesc(label="Student"
      ,type=JTextField.class
      ,editable=false
      ,ref=@Select(clazz=Student.class,attributes={Student.A_name})
      ,modelDesc=@ModelDesc(model=Student.class,dataSourceType=JFlexiDataSource.class)
      )
  private Student student;

  @AttributeDesc(label="Content")
  private String content;

}