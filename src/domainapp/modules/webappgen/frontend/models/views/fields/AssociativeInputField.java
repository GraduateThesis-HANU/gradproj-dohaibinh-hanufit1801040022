package domainapp.modules.webappgen.frontend.models.views.fields;

import domainapp.modules.common.parser.statespace.metadef.FieldDef;
import org.modeshape.common.text.Inflector;

public abstract class AssociativeInputField extends ViewField {
    private static final Inflector inflector = Inflector.getInstance();
    private final ViewField idField;
    private final ViewField detailsField;

    public AssociativeInputField(FieldDef fieldDef, FieldDef idFieldDef, String idFieldLabel) {
        super(fieldDef);
        // create ID field
        this.idField = SimpleViewField.createUsing(idFieldDef, idFieldLabel);
        this.idField.setBackingField(fieldDef.getName() + inflector.upperCamelCase(idFieldDef.getName()));

        // create details field
        this.detailsField = SimpleViewField.createUsing(fieldDef,
                inflector.humanize(
                        inflector.underscore(
                                fieldDef.getType().asClassOrInterfaceType()
                                        .getNameAsString())));
    }

    public ViewField getIdField() {
        return this.idField;
    }

    public ViewField getDetailsField() {
        return this.detailsField;
    }

    private static String removeUnnecessaryFormGroup(String field) {
        return field.replace("<FormGroup>\n", "")
                .replace("</FormGroup>", "");
    }

    @Override
    public String getAsString() {
        return getTemplate().getAsString()
                .replace("{{ idField }}",
                        removeUnnecessaryFormGroup(this.getIdField().getAsString()))
                .replace("{{ detailsField }}",
                        removeUnnecessaryFormGroup(this.getDetailsField().getAsString()));
    }
}
