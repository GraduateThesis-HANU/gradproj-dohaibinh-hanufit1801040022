package vn.com.courseman.modules.proper.sclass.model;

import java.util.ArrayList;
import java.util.Collection;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.DomainIdable;
import domainapp.basics.model.Oid;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.MetaConstants;
import domainapp.basics.model.meta.Select;
import domainapp.basics.util.Tuple;
import vn.com.courseman.modules.proper.sclassregist.model.SClassRegistration;
import vn.com.courseman.modules.proper.student.model.Student;

/**
 * Represents a student class.
 * 
 * @author dmle
 *
 */
@DClass()
public class SClass implements DomainIdable {
  /*** 
   * Implements DomainIdable
   */
  // not a domain attribute
  private Oid oid;
  
  @Override
  public void setOid(Oid id) {
    this.oid = id;
  }

  @Override
  public Oid getOid() {
    return oid;
  }
  // end implement DomainIdable
  
  private static int idCounter;
  
  @DAttr(name="id",id=true,auto=true,length=6,mutable=false,optional=false,type=Type.Integer)
  private int id;

  @DAttr(name="name",length=20,type=Type.String,optional=false)
  private String name;
  
  ///// Many-Many Association to Student
  @DAttr(name = "students", type = Type.Collection, serialisable = false, filter = @Select(clazz = Student.class))
  @DAssoc(ascName = "M2-m-assoc-M1", role = "r2", ascType = AssocType.Many2Many, endType = AssocEndType.Many, associate = @Associate(type = Student.class, cardMin = 0, cardMax = MetaConstants.CARD_MORE), normAttrib = "classRegists")
  private Collection<Student> students;

  @DAttr(name = "classRegists", type = Type.Collection, optional = false, serialisable = false, filter = @Select(clazz = SClassRegistration.class))
  @DAssoc(ascName = "M2-assoc-I", role = "r2", ascType = AssocType.One2Many, endType = AssocEndType.One, associate = @Associate(type = SClassRegistration.class, cardMin = 0, cardMax = MetaConstants.CARD_MORE))
  private Collection<SClassRegistration> classRegists; 
  
  // derived
  private int classRegistsCount;
  ///// End Association to Student
  
  // links
//  @DAttr(name="enrolmentMgmt",type=Type.Domain,serialisable=false)
//  private EnrolmentMgmt enrolmentMgmt;
  
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  public SClass(String name) {
    this(null, name, null);
  }
  
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  @DOpt(type=DOpt.Type.RequiredConstructor)
  public SClass(String name, Collection<Student> students) {
    this(null, name, students);
  }
  
  @DOpt(type=DOpt.Type.DataSourceConstructor)
  public SClass(Integer id, String name) {
    this(id, name, null);
  }
  
  private SClass(Integer id, String name, Collection<Student> students) {
    this.id = nextID(id);
    this.name = name;
    this.students = students;
  }

  public void setName(String name) {
    this.name = name;
  }
    
  public String getName() {
    return name;
  }
  
  public int getId() {
    return id;
  }
  
  public String toString() {
    return "Class("+getId()+","+getName()+")";
  }
  
//  public boolean equals(Object o) {
//    if (o ==null || (!(o instanceof SClass))) {
//      return false;
//    }
//    
//    return ((SClass)o).id == this.id;
//  }

  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SClass other = (SClass) obj;
    if (id != other.id)
      return false;
    return true;
  }

  private static int nextID(Integer currID) {
    if (currID == null) {
      idCounter++;
      return idCounter;
    } else {
      int num = currID.intValue();
      if (num > idCounter)
        idCounter = num;
      //setIdCounter(currID);
      
      return currID;
    }
  }

  /**
   * @requires 
   *  minVal != null /\ maxVal != null
   * @effects 
   *  update the auto-generated value of attribute <tt>attrib</tt>, specified for <tt>derivingValue</tt>, using <tt>minVal, maxVal</tt>
   */
  @DOpt(type=DOpt.Type.AutoAttributeValueSynchroniser)
  public static void updateAutoGeneratedValue(
      DAttr attrib,
      Tuple derivingValue, 
      Object minVal, 
      Object maxVal) throws ConstraintViolationException {
    
    if (minVal != null && maxVal != null) {
      //TODO: update this for the correct attribute if there are more than one auto attributes of this class 

      int maxIdVal = (Integer) maxVal;
      if (maxIdVal > idCounter)  
        idCounter = maxIdVal;
    }
  }
  
  @DOpt(type = DOpt.Type.Getter)
  @AttrRef(value = "students")
  public Collection<Student> getStudents() {
      return students;
  }

  @DOpt(type = DOpt.Type.Setter)
  @AttrRef(value = "students")
  public void setStudents(Collection<Student> associates) {
      this.students = associates;
  }

  @DOpt(type=DOpt.Type.LinkCountGetter)
  public Integer getClassRegistsCount() {
    return classRegistsCount;
  }

  @DOpt(type=DOpt.Type.LinkCountSetter)
  public void setClassRegistsCount(int count) {
    classRegistsCount = count;
  }
  
  /**
 * @effects 
 *  add <tt>associate</tt> to {@link #students}
 */
  @AttrRef(value = "students")
  private void addStudent(Student associate) {
      if (students == null)
          students = new ArrayList<>();
      if (!students.contains(associate)) {
          students.add(associate);
      }
  }

  /**
 * @effects 
 *  remove <tt>associate</tt> from {@link #students}
 */
  @AttrRef(value = "students")
  private void removeStudent(Student associate) {
      if (students != null) {
          students.remove(associate);
      }
  }

  @DOpt(type = DOpt.Type.Getter)
  @AttrRef(value = "classRegists")
  public Collection<SClassRegistration> getClassRegists() {
      return classRegists;
  }

  @DOpt(type = DOpt.Type.Setter)
  @AttrRef(value = "classRegists")
  public void setClassRegists(Collection<SClassRegistration> associates) {
      this.classRegists = associates;
      classRegistsCount = associates.size();
  }

  @DOpt(type = DOpt.Type.LinkAdderNew)
  @AttrRef(value = "classRegists")
  public boolean addNewClassRegists(SClassRegistration associate) {
      classRegists.add(associate);
      // update  students
      addStudent(associate.getStudent());
      classRegistsCount++;
      // no other attributes changed
      return false;
  }

  @DOpt(type = DOpt.Type.LinkAdderNew)
  @AttrRef(value = "classRegists")
  public boolean addNewClassRegists(Collection<SClassRegistration> associates) {
      classRegists.addAll(associates);
      // update students
      for (SClassRegistration assoc : associates) {
          addStudent(assoc.getStudent());
      }
      classRegistsCount += associates.size();
      // no other attributes changed
      return false;
  }

  @DOpt(type = DOpt.Type.LinkAdder)
  @AttrRef(value = "classRegists")
  public boolean addClassRegists(SClassRegistration associate) {
      if (!classRegists.contains(associate)) {
          classRegists.add(associate);
          // update students
          addStudent(associate.getStudent());
          classRegistsCount++;
      }
      // no other attributes changed
      return false;
  }

  @DOpt(type = DOpt.Type.LinkAdder)
  @AttrRef(value = "classRegists")
  public boolean addClassRegists(Collection<SClassRegistration> associates) {
      for (SClassRegistration assoc : associates) {
          if (!classRegists.contains(assoc)) {
              classRegists.add(assoc);
              // update students
              addStudent(assoc.getStudent());
              classRegistsCount++;
          }
      }
      // no other attributes changed
      return false;
  }

  @DOpt(type = DOpt.Type.LinkRemover)
  @AttrRef(value = "classRegists")
  public boolean removeClassRegists(SClassRegistration associate) throws ConstraintViolationException {
      boolean removed = classRegists.remove(associate);
      if (removed) {
          // update students
          removeStudent(associate.getStudent());
          classRegistsCount--;
      }
      // no other attributes changed
      return false;
  }
}
