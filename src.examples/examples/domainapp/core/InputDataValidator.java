package examples.domainapp.core;

import domainapp.basics.controller.ControllerTk;
import domainapp.basics.controller.helper.DataValidator;
import domainapp.basics.core.dodm.DODMBasic;
import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.model.config.Configuration;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.util.ApplicationToolKit;
import vn.com.courseman.model.basic.Enrolment;
import vn.com.courseman.model.basic.Student;

/**
 * @overview 
 *  Demonstrates how to use {@link DataValidator} to perform domain-specific 
 *  data validation rules on the input values 
 *  for the constructor operation of a domain class.
 *  
 * @author Duc Minh Le (ducmle)
 *
 * @version 
 */
public class InputDataValidator {
  public static void main(String[] args) {
    // 
    Configuration config = ApplicationToolKit.createMemoryBasedConfiguration("");
//    GUIToolkit.initInstance(config); 
    DODMBasic dodm = DODMBasic.getInstance(config);
    
    // register domain class
    Class<Enrolment> domainCls = Enrolment.class;
    try {
      dodm.addClass(domainCls);
    } catch (NotPossibleException | NotFoundException | DataSourceException e) {
      e.printStackTrace();
      return;
    }
      
    // create default data validator instance
    DataValidator<Enrolment> validator = ControllerTk.getDomainSpecificDataValidator(dodm, domainCls);
    System.out.printf("Created validator: %s%n", validator);
    
    // test input
    DAttr attrId = dodm.getDsm().getDomainConstraint(domainCls, Enrolment.A_id);
    int[] ids = {
        // invalid
        -2,0
        // valid
        ,2,10
    };
    
    System.out.printf("Non-associative domain attribute: %s.%s%n", 
        domainCls.getSimpleName(), 
        attrId.name());

    // validate 
    for (Object val: ids) {
      System.out.printf("Validating: %s%n", val);
      try {
        validator.validateDomainValue(attrId, val);
        System.out.printf("...OK!%n");
      } catch (ConstraintViolationException e) {
        e.printStackTrace();
      }
    }
  }
}
