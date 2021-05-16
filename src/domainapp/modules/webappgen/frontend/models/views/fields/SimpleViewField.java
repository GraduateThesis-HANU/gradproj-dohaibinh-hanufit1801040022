package domainapp.modules.webappgen.frontend.models.views.fields;

import domainapp.basics.model.meta.DAttr;
import domainapp.modules.common.parser.statespace.metadef.FieldDef;
import domainapp.modules.webappgen.frontend.generators.utils.DomainTypeRegistry;
import domainapp.modules.webappgen.frontend.models.common.FieldDefExtensions;
import domainapp.modules.webappgen.frontend.templates.JsTemplate;
import domainapp.modules.webappgen.frontend.templates.JsTemplates;

import java.util.Map;
import java.util.Objects;

class SimpleViewField extends ViewField {
    private final String label;
    private boolean isDisabled;

    private SimpleViewField(FieldDef fieldDef, String label) {
        super(fieldDef);
        this.label = escapeQuotes(label);
        final Map<String, Object> attributes = FieldDefExtensions.getAttribute(getFieldDef());
        this.isDisabled = (boolean) attributes.getOrDefault("id", false)
                || (boolean) attributes.getOrDefault("auto", false);
    }

    public static String escapeQuotes(String input) {
        return input.replace("\"", "").replace("'", "");
    }

    public static SimpleViewField createUsing(FieldDef fieldDef, String label) {
//        ensureSimpleViewFieldDef(fieldDef);
        return new SimpleViewField(fieldDef, label);
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    private boolean isDisabled() {
        return isDisabled;
    }

    void setDisabled(boolean newDisabledState) {
        this.isDisabled = newDisabledState;
    }

    private String getInputType() {
        final DAttr.Type type = FieldDefExtensions.getDomainType(getFieldDef()).get();
        if (type.isString() || type.isDomainType()) return "text";
        else if (type.isDate()) return "date";
        else if (type.isNumeric()) return "number";
        else if (type.isColor()) return "color";
        else if (type.isBoolean()) return "checkbox";
        else if (type.isDomainReferenceType()
                && FieldDefExtensions.isPrimitiveOrEnumType(getFieldDef())) {
            return "select";
        } else {
            throw new IllegalStateException("Unsupported input type: " + type);
        }
    }

    @Override
    public JsTemplate getTemplate() {
        if (FieldDefExtensions.isPrimitiveOrEnumType(getFieldDef())) {
            return JsTemplates.SIMPLE_VIEW_FIELD;
        }
        String type = getInputType();
        switch (type) {
            case "select":
                return JsTemplates.SELECT_OPTION;
            case "checkbox":
                return JsTemplates.CHECKBOX;
            case "text":
                return JsTemplates.SIMPLE_VIEW_FIELD;
        }
        throw new IllegalStateException("Unsupported input type: " + type);
    }

    private static void ensureSimpleViewFieldDef(FieldDef fieldDef) {
        DAttr.Type type = FieldDefExtensions.getDomainType(fieldDef).orElse(null);
        if (type == null) {
            throw new IllegalArgumentException("Not reflecting a domain field: " + fieldDef);
        }
        String fieldTypeName = fieldDef.getType().isPrimitiveType() ?
                fieldDef.getType().asPrimitiveType().getType().name():
                fieldDef.getType().asClassOrInterfaceType().getNameAsString();
        Class fieldTypeCls = DomainTypeRegistry.getInstance().getDomainTypeByName(fieldTypeName);
        if ((type.isDomainType() || type.isDomainReferenceType())
                && (Objects.nonNull(fieldTypeCls) && !fieldTypeCls.isEnum())) {
            throw new IllegalArgumentException("Not a simple domain field: " + fieldDef);
        }
    }

    @Override
    public String getAsString() {
        return this.getTemplate().getAsString()
                .replace("{{ fieldLabel }}", getLabel())
                .replace("{{ fieldType }}", getInputType())
                .replace("{{ backingField }}", getBackingField())
                .replace("{{ disabledFlag }}", this.isDisabled() ? "disabled" : "");
    }
}
