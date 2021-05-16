package domainapp.modules.webappgen.frontend.models.views;

import domainapp.modules.webappgen.frontend.templates.JsTemplates;
import org.modeshape.common.text.Inflector;

class SubmoduleView extends View {

    private final String referredClassName;
    private final String parent;

    public SubmoduleView(String referredClassName, String parent) {
        super(JsTemplates.SUBFORM, "");
        this.referredClassName = referredClassName;
        this.parent = parent;
    }

    @Override
    public String getFileName() {
        return referredClassName.concat("Submodule");
    }

    @Override
    public String getAsString() {
        return getTemplate().getAsString()
                .replace("{{ view.name.module }}", referredClassName.concat("Module"))
                .replace("{{ view.dir }}", Inflector.getInstance().underscore(referredClassName))
                .replace("{{ view.name.submodule }}", referredClassName.concat("Submodule"))
                .replace("{{ view.parent }}", parent);
    }
}
