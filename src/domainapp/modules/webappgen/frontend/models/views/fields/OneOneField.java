package domainapp.modules.webappgen.frontend.models.views.fields;

import domainapp.modules.common.parser.statespace.metadef.FieldDef;
import domainapp.modules.webappgen.frontend.templates.JsTemplate;
import domainapp.modules.webappgen.frontend.templates.JsTemplates;
import org.modeshape.common.text.Inflector;

class OneOneField extends AssociativeInputField {

    private static Inflector inflector = Inflector.getInstance();

    public OneOneField(FieldDef fieldDef, FieldDef idFieldDef, String idFieldLabel) {
        super(fieldDef, idFieldDef, idFieldLabel);
    }

    @Override
    public String getReferredView() {
        final String fieldTypeName = this.getFieldDef()
                .getType().asClassOrInterfaceType().getNameAsString();
        return fieldTypeName.concat("Submodule");
    }

    @Override
    public JsTemplate getTemplate() {
        return JsTemplates.ONE_ONE_INPUT_FIELD;
    }

    @Override
    public String getAsString() {
        final String fieldTypeName = this.getFieldDef()
                .getType().asClassOrInterfaceType().getNameAsString();

        return super.getAsString()
                .replace("{{ classNameCamelCase }}", inflector.lowerCamelCase(fieldTypeName))
                .replace("{{ submodule }}", fieldTypeName.concat("Submodule"))
                .replace("{{ classNameHumanReadable }}",
                        "Form: " + inflector.capitalize(
                                inflector.humanize(inflector.underscore(fieldTypeName))))
                .replace("{{ backingField }}", getDetailsField().getBackingField())
                .replace("{{ idBackingField }}", getIdField().getBackingField());

    }
}
