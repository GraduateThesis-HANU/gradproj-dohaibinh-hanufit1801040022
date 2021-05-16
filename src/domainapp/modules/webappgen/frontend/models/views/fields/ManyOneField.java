package domainapp.modules.webappgen.frontend.models.views.fields;

import domainapp.modules.common.parser.statespace.metadef.FieldDef;
import domainapp.modules.webappgen.frontend.templates.JsTemplate;
import domainapp.modules.webappgen.frontend.templates.JsTemplates;

class ManyOneField extends AssociativeInputField {
    public ManyOneField(FieldDef fieldDef, FieldDef idFieldDef, String idLabel) {
        super(fieldDef, idFieldDef, idLabel);
    }

    @Override
    public JsTemplate getTemplate() {
        return JsTemplates.MANY_ONE_INPUT_FIELD;
    }

}
