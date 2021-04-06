package examples.domainapp.modules.webappgen.backend;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.modules.webappgen.backend.annotations.bridges.TargetType;
import domainapp.modules.webappgen.backend.base.controllers.ServiceRegistry;
import domainapp.modules.webappgen.backend.base.services.CrudService;
import domainapp.modules.webappgen.backend.generators.GenerationMode;
import domainapp.modules.webappgen.backend.generators.WebServiceGenerator;
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
import vn.com.courseman.model.basic.City;
import vn.com.courseman.model.basic.CourseModule;
import vn.com.courseman.model.basic.Enrolment;
import vn.com.courseman.model.basic.Student;

import java.text.SimpleDateFormat;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;

/**
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "vn.com.courseman",
        "domainapp.modules.webappgen.backend"})
public class SpringApp {

    // 1. initialise the model
    static final Class<?>[] model = {
            CourseModule.class,
//            CompulsoryModule.class,
//            ElectiveModule.class,
            Enrolment.class,
            Student.class,
            City.class
    };

    private static SoftwareImpl sw;

    /**
     * @param args The arguments of the program.
     */
    public static void main(final String[] args) {
        System.out.println("------------");

        WebServiceGenerator generator = new WebServiceGenerator(
                TargetType.SPRING, GenerationMode.BYTECODE);
        generator.setGenerateCompleteCallback(() -> {
            sw = SoftwareFactory.createDefaultDomSoftware();
            sw.init();
            try {
                sw.addClasses(model);
            } catch (NotPossibleException
                    | NotFoundException
                    | DataSourceException e) {
                throw new RuntimeException(e);
            }
            // populate the service registry
            final ServiceRegistry registry = ServiceRegistry.getInstance();
            ApplicationContext ctx = SpringApplication.run(SpringApp.class, args);
            ctx.getBeansOfType(CrudService.class).forEach((k, v) -> {
                registry.put(k, v);
            });

        });
        generator.generateWebService(model);
        System.out.println("------------");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:3000");
            }
        };
    }

    @Bean
    public SoftwareImpl getSoftwareImpl() {
        return sw;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule());
        mapper.setVisibility(FIELD, ANY);
        return builder -> builder
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .configure(mapper);
    }
}
