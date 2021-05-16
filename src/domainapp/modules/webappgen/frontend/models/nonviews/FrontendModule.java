package domainapp.modules.webappgen.frontend.models.nonviews;

import domainapp.modules.mccl.model.MCC;
import domainapp.modules.webappgen.backend.utils.ClassAssocUtils;
import domainapp.modules.webappgen.frontend.generators.utils.InheritanceUtils;
import domainapp.modules.webappgen.frontend.models.JsFrontendElement;
import domainapp.modules.webappgen.frontend.models.views.View;
import domainapp.modules.webappgen.frontend.models.views.ViewFactory;
import domainapp.modules.webappgen.frontend.templates.JsTemplate;
import domainapp.modules.webappgen.frontend.templates.JsTemplates;
import org.modeshape.common.text.Inflector;

import java.util.ArrayList;
import java.util.Collection;

// a view's index.js
public class FrontendModule implements JsFrontendElement {
    private static final Inflector inflector = Inflector.getInstance();
    private MCC viewDesc;
    private final String mainAPI;
    private final String title;
    private final Collection<String> apiNames = new ArrayList<>();
    private final Collection<String> possibleTypes = new ArrayList<>();
    private final Collection<View> views = new ArrayList<>();

    public FrontendModule(Class cls, MCC mcc) {
        this.viewDesc = mcc;
        this.title = createTitle(mcc);
        this.mainAPI = inflector.lowerCamelCase(viewDesc.getDomainClass().getName()).concat("API");
        views.add(ViewFactory.createFormView(cls));
        views.add(ViewFactory.createListView(viewDesc));
        makeApiNames(viewDesc);
        makePossibleTypes(cls);
    }

    private static String createTitle(MCC mcc) {
        return mcc.getPropertyVal("viewDesc", "formTitle").toString();
    }

    private void makePossibleTypes(Class cls) {
        this.possibleTypes.addAll(InheritanceUtils.getSubtypeMapFor(cls).keySet());
    }

    private void makeApiNames(final MCC viewDesc) {
        try {
            ClassAssocUtils.getAssociated(Class.forName(viewDesc.getDomainClass().getFqn()))
                    .stream()
                    .map(Class::getSimpleName)
                    .map(inflector::lowerCamelCase)
                    .map(name -> name + "API")
                    .forEach(apiNames::add);
            apiNames.add(mainAPI);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<View> getViews() {
        return this.views;
    }

    public MCC getViewDesc() {
        return this.viewDesc;
    }

    public String getFolder() {
        return inflector.pluralize(
                inflector.underscore(
                        this.viewDesc.getDomainClass().getName())
                .replace("_", "-"));
    }

    private static String makePlural(String original) {
        return inflector.pluralize(inflector.humanize(inflector.underscore(original)))
                .replace("_", "-");
    }

    private static String makeApiDeclaration(final String apiName) {
        final String objName = apiName.replace("API", "");
        return String.format("const %s = new BaseAPI(\"%s\", providers.axios);\n",
                objName, makePlural(objName));
    }

    @Override
    public JsTemplate getTemplate() {
        return JsTemplates.INDEX;
    }

    @Override
    public String getAsString() {
        String baseName = viewDesc.getDomainClass().getName();
        return getTemplate().getAsString()
                .replace("{{ view.name.list }}", baseName.concat("List"))
                .replace("{{ view.name.form }}", baseName.concat("Form"))
                .replace("{{ view.name.main }}", baseName.concat("MainView"))
                .replace("{{ possibleTypes }}",
                        String.format("return [%s]",
                                String.join(",", possibleTypes)))
                .replace("{{ view.title }}", title)
                .replace("{{ view.apis.declarations }}",
                        apiNames.stream()
                                .map(FrontendModule::makeApiDeclaration)
                                .reduce("", (s1, s2) -> s1 + "\n" + s2));
    }

}
