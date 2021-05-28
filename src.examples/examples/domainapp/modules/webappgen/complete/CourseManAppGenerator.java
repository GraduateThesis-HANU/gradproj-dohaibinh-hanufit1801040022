package examples.domainapp.modules.webappgen.complete;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.hanu.courseman.SCC1;
import com.hanu.courseman.modules.ModuleMain;
import com.hanu.courseman.modules.address.ModuleAddress;
import com.hanu.courseman.modules.address.model.Address;
import com.hanu.courseman.modules.coursemodule.ModuleCourseModule;
import com.hanu.courseman.modules.coursemodule.model.CompulsoryModule;
import com.hanu.courseman.modules.coursemodule.model.CourseModule;
import com.hanu.courseman.modules.coursemodule.model.ElectiveModule;
import com.hanu.courseman.modules.enrolment.ModuleEnrolment;
import com.hanu.courseman.modules.enrolment.model.Enrolment;
import com.hanu.courseman.modules.student.ModuleStudent;
import com.hanu.courseman.modules.student.model.Gender;
import com.hanu.courseman.modules.student.model.Student;
import com.hanu.courseman.modules.studentclass.ModuleStudentClass;
import com.hanu.courseman.modules.studentclass.model.StudentClass;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.modules.webappgen.backend.annotations.bridges.TargetType;
import domainapp.modules.webappgen.backend.base.controllers.ServiceRegistry;
import domainapp.modules.webappgen.backend.base.services.CrudService;
import domainapp.modules.webappgen.backend.generators.GenerationMode;
import domainapp.modules.webappgen.backend.generators.WebServiceGenerator;
import domainapp.modules.webappgen.frontend.bootstrap.ViewBootstrapper;
import domainapp.modules.webappgen.frontend.utils.DomainTypeRegistry;
import domainapp.software.SoftwareFactory;
import domainapp.softwareimpl.SoftwareImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The software generator for CourseManApp.
 * @author binh_dh
 */
public class CourseManAppGenerator {
    // initialize the model
    private static final Class<?>[] models = {
            CourseModule.class,
            Enrolment.class,
            Student.class,
            Address.class,
            StudentClass.class,
            CompulsoryModule.class,
            ElectiveModule.class
    };
    // initialize module classes
    // one module per INHERITANCE TREE
    private static final Class<?>[] modules = {
            ModuleCourseModule.class,
            ModuleEnrolment.class,
            ModuleStudent.class,
            ModuleAddress.class,
            ModuleStudentClass.class
    };

    static {
        DomainTypeRegistry.getInstance().addDomainTypes(models);
        DomainTypeRegistry.getInstance().addDomainType(Gender.class);
    }

    private static final String backendTargetPackage = "com.hanu.courseman.backend";
    private static final String backendOutputPath = "src.examples";
    private static final String frontendOutputPath = "src.examples/com/hanu/courseman/frontend";
//    private static final String frontendOutputPath = "/Users/binh_dh/vscode/courseman-examples-2/src";

    public static void main(String[] args) {
        FrontendGenerator.setupAndGen();
        BackendApp.setupAndRun();
    }

    /**
     * @author binh_dh
     * Generate the frontend code to a specified package.
     */
    public static class FrontendGenerator {

        public static void setupAndGen() {
            Class sccClass = SCC1.class;

            ViewBootstrapper bootstrapper = new ViewBootstrapper(
                    frontendOutputPath, sccClass, ModuleMain.class,
                    models, modules
            );

            bootstrapper.bootstrapAndSave();
        }

        public static void main(String[] args) {
            setupAndGen();
        }
    }

    /**
     * @author binh_dh
     */
    @SpringBootApplication
    @ComponentScan(basePackages = {
            "com.hanu.courseman.backend",
            "domainapp.modules.webappgen.backend"})
    public static class BackendApp {

        private static final List<Class> generatedClasses = new ArrayList<>();
        private static SoftwareImpl sw;

        private static void onGenerateComplete(List<Class> _generatedClasses) {
            generatedClasses.addAll(_generatedClasses);
            sw = SoftwareFactory.createDefaultDomSoftware();
            sw.init();
            try {
                sw.addClasses(models);
                sw.loadObjects(models);
            } catch (NotPossibleException
                    | NotFoundException
                    | DataSourceException e) {
                throw new RuntimeException(e);
            }
            // populate the service registry
            final ServiceRegistry registry = ServiceRegistry.getInstance();

            final int generatedClassesCount = generatedClasses.size();
            Class[] primarySources = generatedClasses.toArray(
                    new Class[generatedClassesCount + 1]);
            primarySources[generatedClassesCount] = BackendApp.class;

            ApplicationContext ctx = SpringApplication.run(primarySources, new String[0]);

            ctx.getBeansOfType(CrudService.class).forEach((k, v) -> registry.put(k, v));
        }

        public static void setup() {
            System.out.println("------------");
            WebServiceGenerator generator = new WebServiceGenerator(
                    TargetType.SPRING,
                    GenerationMode.SOURCE_CODE,
                    backendTargetPackage,
                    backendOutputPath);
            generator.generateWebService(models);
            System.out.println("------------");
        }

        /**
         * Setup and run the backend process.
         */
        public static void setupAndRun() {
            System.out.println("------------");

            WebServiceGenerator generator = new WebServiceGenerator(
                    TargetType.SPRING,
                    GenerationMode.SOURCE_CODE,
                    backendTargetPackage,
                    backendOutputPath);
            generator.setGenerateCompleteCallback(
                    CourseManAppGenerator.BackendApp::onGenerateComplete);
            generator.generateWebService(models);
            System.out.println("------------");
        }

        @Bean
        public WebMvcConfigurer corsConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addCorsMappings(CorsRegistry registry) {
                    registry.addMapping("/**")
                            .allowedMethods("GET", "POST", "PATCH", "DELETE")
                            .allowedOrigins("http://localhost:3000");
                }
            };
        }

        @Bean
        public SoftwareImpl getSoftwareImpl() {
            return sw;
        }

        @Bean
        public Jackson2ObjectMapperBuilderCustomizer addCustomBigDecimalDeserialization() {
            return builder -> builder.dateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                    .modules(new ParameterNamesModule())
                    .serializationInclusion(JsonInclude.Include.NON_NULL);
            //.configure(mapper);
        }
    }
}
