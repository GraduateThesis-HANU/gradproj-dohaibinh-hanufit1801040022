package vn.com.courseman.modules.proper.enrolmentmgmt.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import vn.com.courseman.modules.proper.helprequest.model.HelpRequest;
import vn.com.courseman.modules.proper.sclassregist.model.SClassRegistration;
import vn.com.courseman.modules.proper.student.model.Student;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.MetaConstants;
import domainapp.basics.model.meta.Select;

/**
 * @overview
 *  Represents the enrolment management activity
 *  
 * @author dmle
 * @version 3.4c
 */
@DClass(serialisable=false, singleton=true)
public class EnrolmentMgmt {
  @DAttr(name = "id", id = true, auto = true, type = Type.Integer, length = 5, 
      optional = false, mutable = false)
  private int id;
  private static int idCounter = 0;

  // student registration 
  @DAttr(name="students", type=Type.Collection,filter=@Select(clazz=Student.class),serialisable=false)
  @DAssoc(ascName="register-students",role="mgmt",
    ascType=AssocType.One2Many,endType=AssocEndType.One,
    associate=@Associate(
        type=Student.class,cardMin=0,cardMax=MetaConstants.CARD_MORE,
        updateLink=false
     ))
  private Set<Student> students;

  // help desk 
  @DAttr(name="helpDesks", type=Type.Collection,filter=@Select(clazz=HelpRequest.class),serialisable=false)
  @DAssoc(ascName="ask-help",role="mgmt",
    ascType=AssocType.One2Many,endType=AssocEndType.One,
    associate=@Associate(
        type=HelpRequest.class,cardMin=0,cardMax=MetaConstants.CARD_MORE,
        updateLink=false
     ))
  private List<HelpRequest> helpDesks;

  // class registrations 
  @DAttr(name="sclassRegists", type=Type.Collection,filter=@Select(clazz=SClassRegistration.class),serialisable=false)
  @DAssoc(ascName="manage-class-registration",role="mgmt",
    ascType=AssocType.One2Many,endType=AssocEndType.One,
    associate=@Associate(
        type=SClassRegistration.class,cardMin=0,cardMax=MetaConstants.CARD_MORE,
        updateLink=false
    ))
  private Collection<SClassRegistration> sclassRegists;
  
  // not used at the moment
  public EnrolmentMgmt(Integer id) {
    this.id = nextID(id);
  }

  // for use by object form
  public EnrolmentMgmt() {
    this(null);
  }

  public int getId() {
    return id;
  }

  private static int nextID(Integer currID) {
    if (currID == null) { // generate one
      idCounter++;
      return idCounter;
    } else { // update
      int num;
      num = currID.intValue();
      
      if (num > idCounter) {
        idCounter=num;
      }   
      return currID;
    }
  }
}
