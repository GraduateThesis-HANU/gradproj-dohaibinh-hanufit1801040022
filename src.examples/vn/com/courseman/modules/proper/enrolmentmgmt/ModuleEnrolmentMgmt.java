package vn.com.courseman.modules.proper.enrolmentmgmt;

import static domainapp.basics.model.config.view.Region.RegionName.Chart;
import static domainapp.basics.model.config.view.Region.RegionName.Delete;
import static domainapp.basics.model.config.view.Region.RegionName.Export;
import static domainapp.basics.model.config.view.Region.RegionName.First;
import static domainapp.basics.model.config.view.Region.RegionName.Last;
import static domainapp.basics.model.config.view.Region.RegionName.Next;
import static domainapp.basics.model.config.view.Region.RegionName.ObjectScroll;
import static domainapp.basics.model.config.view.Region.RegionName.Open;
import static domainapp.basics.model.config.view.Region.RegionName.Previous;
import static domainapp.basics.model.config.view.Region.RegionName.Print;
import static domainapp.basics.model.config.view.Region.RegionName.Update;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import domainapp.basics.core.View;
import domainapp.basics.model.config.view.Region;
import domainapp.basics.model.config.view.Region.RegionName;
import domainapp.basics.model.meta.MetaConstants;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.SetUpDesc;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.containment.CTree;
import domainapp.basics.model.meta.module.containment.Child;
import domainapp.basics.model.meta.module.containment.SubTree1L;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc.OpenPolicy;
import domainapp.basics.model.meta.module.model.ModelDesc;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.basics.model.util.properties.Property.PropertyName;
import domainapp.basics.model.util.properties.PropertyDesc;
import domainapp.basics.view.panels.DefaultPanel;
import domainapp.controller.datacontroller.command.manyAssoc.CreateObjectAndManyAssociatesDataControllerCommand;
import domainapp.controller.datacontroller.command.manyAssoc.UpdateObjectAndManyAssociatesDataControllerCommand;
import domainapp.core.Controller;
import domainapp.setup.commands.CopyResourceFilesCommand;
import domainapp.view.layout.TabLayoutBuilder;
import domainapp.view.layout.TwoColumnLayoutBuilder;
import vn.com.courseman.modules.proper.enrolmentmgmt.model.EnrolmentMgmt;
import vn.com.courseman.modules.proper.helprequest.ModuleHelpRequest;
import vn.com.courseman.modules.proper.helprequest.model.HelpRequest;
import vn.com.courseman.modules.proper.sclassregist.ModuleSClassRegistration;
import vn.com.courseman.modules.proper.sclassregist.model.SClassRegistration;
import vn.com.courseman.modules.proper.student.ModuleStudent;
import vn.com.courseman.modules.proper.student.model.Student;

@ModuleDescriptor(
    name="ModuleEnrolmentMgmt",
    modelDesc=@ModelDesc(
        model=EnrolmentMgmt.class
    ),
    viewDesc=@ViewDesc(
        formTitle="Manage Enrolment Management",
        domainClassLabel=
        
        "Enrolment Management",    
        imageIcon="enrolment.jpg",
        view=View.class,
        viewType=Region.Type.Data,
        layoutBuilderType=TabLayoutBuilder.class,
        topX=0.5,topY=0.0,//widthRatio=0.9f,heightRatio=0.9f,
        parent=RegionName.Tools,
        excludeComponents={
          // general actions
          Export, Print, Chart,
          // object-related actions
          Open, Update, Delete, //New,
          First, Previous, Next, Last, ObjectScroll,
        }
    ),
    controllerDesc=@ControllerDesc(
        controller=Controller.class
        /*customise createNew command to execute the activity model*/
//        ,props= {
//          @PropertyDesc(name=PropertyName.controller_dataController_new, valueIsClass=CreateAndExecActivityCommand.class, valueType=Class.class, valueAsString=MetaConstants.NullString)
//        }
    ),
    containmentTree=@CTree(
        root=EnrolmentMgmt.class,
            subtrees={
          // enrolmentmgmt -> student
          @SubTree1L(
            parent=EnrolmentMgmt.class,
            children={
              @Child(cname=Student.class,scope={"id", "name", "helpRequested", "modules"})
            }
          )
         }        
    )
    ,isPrimary=true,
    childModules={ModuleStudent.class, ModuleSClassRegistration.class, ModuleHelpRequest.class }
    ,setUpDesc=@SetUpDesc(postSetUp=CopyResourceFilesCommand.class)
)
public class ModuleEnrolmentMgmt {
  @AttributeDesc(label="Enrolment Management")
  private String title;

  // student registration 
  @AttributeDesc(label="Student Registration",
      layoutBuilderType=TwoColumnLayoutBuilder.class
      ,controllerDesc=@ControllerDesc(
          openPolicy=OpenPolicy.I
          // support many-many association with CourseModule
          ,props={
            // custom Create object command: to create {@link Enrolment} from the course modules
            @PropertyDesc(name=PropertyName.controller_dataController_create,
                valueIsClass=CreateObjectAndManyAssociatesDataControllerCommand.class, valueAsString=MetaConstants.NullValue,
                valueType=Class.class),
            // custom Update object command: to update {@link Enrolment} from the course modules
            @PropertyDesc(name=PropertyName.controller_dataController_update,
                valueIsClass=UpdateObjectAndManyAssociatesDataControllerCommand.class, valueAsString=MetaConstants.NullValue,
                valueType=Class.class)
          })
  )
  private Set<Student> students;
  
  // help desk 
  @AttributeDesc(label="Help Request"
      ,type=DefaultPanel.class
  )
  private List<HelpRequest> helpDesks;
  
  // class registration 
  @AttributeDesc(label="Class Registration"
      ,type=DefaultPanel.class
      )
  private Collection<SClassRegistration> sclassRegists;

}
