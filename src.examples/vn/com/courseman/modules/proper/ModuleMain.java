package vn.com.courseman.modules.proper;

import static domainapp.basics.model.config.view.Region.RegionName.Add;
import domainapp.basics.core.View;
import domainapp.basics.model.config.ApplicationModule.ModuleType;
import domainapp.basics.model.config.view.Region.RegionName;
import domainapp.basics.model.config.view.Region.Type;
import domainapp.basics.model.meta.module.ModuleDescriptor;
import domainapp.basics.model.meta.module.SetUpDesc;
import domainapp.basics.model.meta.module.ViewDesc;
import domainapp.basics.model.meta.module.controller.ControllerDesc;
import domainapp.basics.model.util.properties.Property.PropertyName;
import domainapp.basics.model.util.properties.PropertyDesc;
import domainapp.core.Controller;
import domainapp.setup.commands.CopyResourceFilesCommand;

@ModuleDescriptor(
  name="ModuleMain",
  viewDesc=@ViewDesc(
    formTitle="Course Management App: CourseMan",
    imageIcon="courseman.jpg",
    view=View.class,
    viewType=Type.Main,
    topX=0.5,topY=0.0,widthRatio=0.75f,heightRatio=1f, 
    children={
        RegionName.Desktop,
        RegionName.MenuBar,
        RegionName.ToolBar,
        RegionName.StatusBar
    },
    excludeComponents={
      // general actions
      // Export, Print, Chart,
      // object-related actions
      Add // experimental
    },
    props={
      @PropertyDesc(name=PropertyName.view_toolBar_buttonIconDisplay,
          valueAsString="true",valueType=Boolean.class),
      @PropertyDesc(name=PropertyName.view_toolBar_buttonTextDisplay,
          valueAsString="false",valueType=Boolean.class),
      @PropertyDesc(name=PropertyName.view_searchToolBar_buttonIconDisplay,
          valueAsString="true",valueType=Boolean.class),
      @PropertyDesc(name=PropertyName.view_searchToolBar_buttonTextDisplay,
          valueAsString="false",valueType=Boolean.class),
          /* use these for object form actions
      @PropertyDesc(name=PropertyName.view_objectForm_actions_buttonIconDisplay,
          valueAsString="true",valueType=Boolean.class),
      @PropertyDesc(name=PropertyName.view_objectForm_actions_buttonTextDisplay,
          valueAsString="false",valueType=Boolean.class),
          */
      // international support
      @PropertyDesc(name=PropertyName.view_lang_international,
        valueAsString="true",valueType=Boolean.class),
    }
  ),
  controllerDesc=@ControllerDesc(controller=Controller.class),
  type=ModuleType.DomainMain
  ,setUpDesc=@SetUpDesc(postSetUp=CopyResourceFilesCommand.class)
)
public class ModuleMain {

}
