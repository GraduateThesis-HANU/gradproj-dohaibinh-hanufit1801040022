package examples.domainapp.tool.software;

import domainapp.basics.exceptions.NotPossibleException;
import domainapp.software.SoftwareFactory;
import domainapp.softwareimpl.DomSoftware;

/**
 * @overview 
 *  A class the set up the software data source. It should be run only once and 
 *  involves creating a relational model for the domain model of the software. 
 *  
 * @author Duc Minh Le (ducmle)
 */
public class MainConfigureSoftware {
  
  public static void main(String[] args) {
    try {
      DomSoftware sw = SoftwareFactory.createDefaultDomSoftware();
      
      sw.configure();
      
      //sw.deleteConfig();
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
