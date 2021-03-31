package examples.domainapp.tool.software;

import domainapp.software.SoftwareFactory;
import domainapp.softwareimpl.DomSoftware;
import examples.domainapp.tool.services.coursemodule.model.CompulsoryModule;
import examples.domainapp.tool.services.coursemodule.model.CourseModule;
import examples.domainapp.tool.services.coursemodule.model.ElectiveModule;
import examples.domainapp.tool.services.enrolment.model.Enrolment;
import examples.domainapp.tool.services.sclass.model.SClass;
import examples.domainapp.tool.services.student.model.City;
import examples.domainapp.tool.services.student.model.Student;
import examples.domainapp.tool.services.student.reports.StudentsByCityJoinReport;
import examples.domainapp.tool.services.student.reports.StudentsByNameReport;


/**
 * @overview 
 *  Create and run a UI-based {@link DomSoftware} for a pre-defined model.  
 *  
 * @author dmle
 */
public class Main {
  
  // 1. initialise the model
  static final Class[] model = {
      CourseModule.class, 
      CompulsoryModule.class, 
      ElectiveModule.class, 
      Enrolment.class, 
      Student.class, 
      City.class, 
      SClass.class,
      // reports
      StudentsByNameReport.class,
      StudentsByCityJoinReport.class
  };
  
  /**
   * @effects 
   *  create and run a UI-based {@link DomSoftware} for a pre-defined model. 
   */
  public static void main(String[] args){
    // 2. create UI software
    DomSoftware sw = SoftwareFactory.createUIDomSoftware();
    
    // 3. run
    // create in memory configuration
    System.setProperty("domainapp.setup.SerialiseConfiguration", "false");
    
    // 3. run it
    try {
      sw.run(model);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }   
  }

}
