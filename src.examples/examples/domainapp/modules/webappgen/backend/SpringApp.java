package examples.domainapp.modules.webappgen.backend;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.modules.webappgen.backend.annotations.bridges.TargetType;
import domainapp.modules.webappgen.backend.base.controllers.RestfulController;
import domainapp.modules.webappgen.backend.base.controllers.ServiceRegistry;
import domainapp.modules.webappgen.backend.base.models.Identifier;
import domainapp.modules.webappgen.backend.base.services.CrudService;
import domainapp.modules.webappgen.backend.generators.GenerationMode;
import domainapp.modules.webappgen.backend.generators.WebServiceGenerator;
import domainapp.software.SoftwareFactory;
import domainapp.softwareimpl.SoftwareImpl;
import examples.domainapp.modules.webappgen.backend.services.coursemodule.model.CompulsoryModule;
import examples.domainapp.modules.webappgen.backend.services.coursemodule.model.CourseModule;
import examples.domainapp.modules.webappgen.backend.services.coursemodule.model.ElectiveModule;
import examples.domainapp.modules.webappgen.backend.services.enrolment.model.Enrolment;
import examples.domainapp.modules.webappgen.backend.services.sclass.model.SClass;
import examples.domainapp.modules.webappgen.backend.services.student.model.Address;
import examples.domainapp.modules.webappgen.backend.services.student.model.Student;
import org.joor.Reflect;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionOverrideException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;

/**
 * @author binh_dh
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "examples.domainapp.modules.webappgen.backend",
        "domainapp.modules.webappgen.backend"})
public class SpringApp {

    // 1. initialise the model
    static final Class<?>[] model = {
            CourseModule.class,
            CompulsoryModule.class,
            ElectiveModule.class,
            Enrolment.class,
            Student.class,
            Address.class,
            SClass.class
    };
    private static final List<Class> generatedClasses = new ArrayList<>();
    private static SoftwareImpl sw;

    /**
     * @param args The arguments of the program.
     */
    public static void main(final String[] args) {
        System.out.println("------------");

        WebServiceGenerator generator = new WebServiceGenerator(
                TargetType.SPRING,
                GenerationMode.SOURCE_CODE,
                "examples.domainapp.modules.webappgen.backend.services",
                "/Users/binh_dh/Documents/generated");
        generator.setGenerateCompleteCallback(_generatedClasses -> {
            generatedClasses.addAll(_generatedClasses);
            sw = SoftwareFactory.createDefaultDomSoftware();
            sw.init();
            try {
                sw.addClasses(model);
                sw.loadObjects(model);
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
            primarySources[generatedClassesCount] = SpringApp.class;

            GenericWebApplicationContext ctx = (GenericWebApplicationContext)
                    SpringApplication.run(primarySources, args);

            ctx.getBeansOfType(CrudService.class).forEach((k, v) -> registry.put(k, v));
        });
        generator.generateWebService(model);
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
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .autoDetectFields(true)
                .autoDetectGettersSetters(true);
                //.configure(mapper);
    }
}
