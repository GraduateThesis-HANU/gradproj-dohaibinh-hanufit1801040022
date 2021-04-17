package examples.domainapp.modules.webappgen.frontend;

import domainapp.modules.webappgen.frontend.examples.services.coursemodule.model.CompulsoryModule;
import domainapp.modules.webappgen.frontend.examples.services.coursemodule.model.CourseModule;
import domainapp.modules.webappgen.frontend.examples.services.coursemodule.model.ElectiveModule;
import domainapp.modules.webappgen.frontend.examples.services.enrolment.model.Enrolment;
import domainapp.modules.webappgen.frontend.examples.services.sclass.model.SClass;
import domainapp.modules.webappgen.frontend.examples.services.student.model.City;
import domainapp.modules.webappgen.frontend.examples.services.student.model.Gender;
import domainapp.modules.webappgen.frontend.examples.services.student.model.ModuleStudent;
import domainapp.modules.webappgen.frontend.examples.services.student.model.Student;
import domainapp.modules.webappgen.frontend.generators.FormGenerator;
import domainapp.modules.webappgen.frontend.generators.utils.DomainTypeRegistry;
import domainapp.modules.webappgen.frontend.generators.utils.MCCUtils;
import domainapp.modules.mccl.model.MCC;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class StudentReactViewGen {
    static final Class<?>[] models = {
            Student.class,
            City.class,
            Gender.class,
            Enrolment.class,
            SClass.class,
            CourseModule.class,
            ElectiveModule.class,
            CompulsoryModule.class
    };

    static {
        DomainTypeRegistry.getInstance().addDomainTypes(models);
    }

    public static void main(String[] args) throws IOException {
        MCC mcc = MCCUtils.readMCC(Student.class, ModuleStudent.class);

        String formTrigger = "<Button onClick={this.handleShow}>+ Create</Button>\n";

        String studentCreateForm = new FormGenerator(
                mcc, FormGenerator.Type.FORM_ONLY)
                .generate();
//        String studentDetailsLine = new ListViewGenerator.ListItemViewGenerator(
//                mcc, "StudentDetails").generate();

        // write to file
        BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/binh_dh/vscode/courseman-react/src/student/ViewCreate.js"));
        writer.write(studentCreateForm);
        writer.flush();

        BufferedWriter writer2 = new BufferedWriter(new FileWriter("/Users/binh_dh/vscode/courseman-react/src/student/ViewDetails.js"));
//        writer2.write(studentDetailsLine);
        writer2.flush();
    }
}
