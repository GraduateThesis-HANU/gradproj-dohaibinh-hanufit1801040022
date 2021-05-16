package domainapp.modules.webappgen.frontend.models.views;

import com.github.javaparser.ast.body.FieldDeclaration;
import domainapp.modules.common.model.parser.ClassAST;
import domainapp.modules.mccl.model.MCC;
import domainapp.modules.webappgen.frontend.models.views.fields.ViewField;
import domainapp.modules.webappgen.frontend.templates.JsTemplate;
import domainapp.modules.webappgen.frontend.templates.JsTemplates;
import org.modeshape.common.text.Inflector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * TODO:
 * +- Generate the form view
 * +- Generate form view for inheritance tree
 */
class FormView extends View implements HasSubmodule {
    private final String backingClass;
    private final Collection<SubmoduleView> submoduleViews = new ArrayList<>();

    public FormView(MCC viewDesc) {
        super(viewDesc, JsTemplates.FORM);
        this.backingClass = viewDesc.getDomainClass().getName();
        populateSubmodules();
    }

    public FormView(ClassAST cls) {
        super(cls.getCls(), JsTemplates.FORM);
        this.backingClass = cls.getName();
        populateSubmodules();
    }

    public FormView(Class cls, Collection<FieldDeclaration> domainFields) {
        super(cls.getSimpleName(), domainFields, JsTemplates.FORM);
        this.backingClass = cls.getSimpleName();
        populateSubmodules();
    }

    private void populateSubmodules() {
        Collection<SubmoduleView> submoduleViews = getReferredViews().stream()
                .map(referredView -> referredView.replace("Submodule", ""))
                .map(referredView ->
                        new SubmoduleView(referredView,
                                Inflector.getInstance().lowerCamelCase(this.backingClass)))
                .collect(Collectors.toList());
        this.submoduleViews.addAll(submoduleViews);
    }

    public Collection<SubmoduleView> getSubmoduleViews() {
        return submoduleViews;
    }

    @Override
    public JsTemplate getTemplate() {
        return JsTemplates.FORM;
    }

    String renderForm() {
        return this.getViewFields().stream()
                    .map(ViewField::getAsString)
                    .reduce("", (s1, s2) -> s1 + "\n<br />\n" + s2);
    }

    @Override
    public String getAsString() {
        return getTemplate().getAsString()
                .replace("{{ view.name.form }}", backingClass.concat("Form"))
                .replace("{{ view.title }}", getTitle())
                .replace("{{ view.form }}", "return (<>" + renderForm() + "\n</>);")
                .replace("{{ view.submodule.imports }}",
                        this.getReferredViews().stream()
                                .map(view -> String.format("import %s from \"./%s\";", view, view))
                                .reduce("", (s1, s2) -> s1 + "\n" + s2));
    }

    @Override
    public String getFileName() {
        return backingClass.concat("Form");
    }

    private static Class<?> getClassBy(MCC viewDesc) {
        try {
            return Class.forName(viewDesc.getFqn());
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Class<?> getClassBy(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
