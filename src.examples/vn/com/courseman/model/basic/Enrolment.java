package vn.com.courseman.model.basic;

import java.io.Serializable;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.util.Tuple;

/**
 * Represents an enrolment
 * 
 * @author dmle
 * 
 */
@DClass(schema="test_basic")
public class Enrolment implements Comparable, Serializable {
  static final long serialVersionUID = 2014L;
  private static int idCounter = 0;
  
  public static final String A_id = "id";

  // attributes
  @DAttr(name = A_id, id = true, auto = true, type = Type.Integer, length = 5, 
      optional = false, mutable = false, min=1)
  private int id;
  @DAttr(name = "student", type = Type.Domain, length = 5, optional = false)
  private Student student;
  @DAttr(name = "module", type = Type.Domain, length = 5, optional = false)
  private Module module;
  @DAttr(name = "internalMark", type = Type.Double, length = 4, optional = false, min = 0.0)
  private double internalMark;
  @DAttr(name = "examMark", type = Type.Double, length = 4, optional = false, min = 0.0)
  private double examMark;

  @DAttr(name = "finalGrade", type = Type.Char,length = 1,auto = true,mutable = false, optional = false)
  private char finalGrade;

  // constructor method
  public Enrolment(Student s, Module m) {
    this(null, s, m, 0.0, 0.0, null);
  }

  public Enrolment(Student s, Module m, Double internalMark, Double examMark) {
    this(null, s, m, internalMark, examMark, null);
  }

  // @version 2.0
  public Enrolment(Integer id, Student s, Module m, Double internalMark,
      Double examMark, 
      // v2.7.3: not used but needed to load data from source
      Character finalGrade) throws ConstraintViolationException {
    this.id = nextID(id);
    this.student = s;
    this.module = m;
    this.internalMark = internalMark.doubleValue();
    this.examMark = examMark.doubleValue();
    this.finalGrade = genGrade(internalMark, examMark);
  }

  // setter methods
  public void setStudent(Student s) {
    this.student = s;
  }

  public void setModule(Module m) {
    this.module = m;
  }

  public void setInternalMark(double mark) {
    this.internalMark = mark;
    finalGrade = genGrade(internalMark, examMark);
  }

  public void setExamMark(double mark) {
    this.examMark = mark;
    // generate final grade
    finalGrade = genGrade(internalMark, examMark);
  }

  // getter methods
  public int getId() {
    return id;
  }

  public Student getStudent() {
    return student;
  }

  public Module getModule() {
    return module;
  }

  public double getInternalMark() {
    return internalMark;
  }

  public double getExamMark() {
    return examMark;
  }

  public char getFinalGrade() {
    return finalGrade;
  }

  // override toString
  public String toString() {
    return toString(false);
  }

  public String toString(boolean full) {
    if (full)
      return "Enrolment(" + student + "," + module + ")";
    else
      return "Enrolment(" + getId() + "," + student.getId() + ","
          + module.getCode() + ")";
  }

  // a method to compute the final grade
  private char genGrade(double internal, double exam) {
    double finalMarkD = 0.4 * internal + 0.6 * exam;
    // round the mark to the closest integer value
    int finalMark = (int) Math.round(finalMarkD);

    if (finalMark < 5)
      return 'F';
    else if (finalMark == 5)
      return 'P';
    else if (finalMark <= 7)
      return 'G';
    else
      return 'E';
  }

  private static int nextID(Integer currID) {
    if (currID == null) { // generate one
      idCounter++;
      return idCounter;
    } else { // update
      int num;
      num = currID.intValue();
      
//      if (num <= idCounter) {
//        throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE, 
//            "Lỗi giá trị thuộc tính ID: {0}", num + "<=" + idCounter);
//      }
      
      if (num > idCounter) {
        idCounter=num;
      }   
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
      // check the right attribute
      if (attrib.name().equals("id")) {
        int maxIdVal = (Integer) maxVal;
        if (maxIdVal > idCounter)  
          idCounter = maxIdVal;
      } 
      // TODO add support for other attributes here 
    }
  }
  
  // implements Comparable interface
  public int compareTo(Object o) {
    if (o == null || (!(o instanceof Enrolment)))
      return -1;

    Enrolment e = (Enrolment) o;

    return this.student.getId().compareTo(e.student.getId());
  }

}