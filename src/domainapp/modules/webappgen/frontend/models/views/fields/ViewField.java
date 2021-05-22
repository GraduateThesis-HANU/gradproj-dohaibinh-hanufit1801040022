package domainapp.modules.webappgen.frontend.models.views.fields;

import domainapp.basics.model.meta.DAttr;
import domainapp.modules.common.parser.statespace.metadef.FieldDef;
import domainapp.modules.webappgen.frontend.utils.DomainTypeRegistry;
import domainapp.modules.webappgen.frontend.models.ViewableElement;
import domainapp.modules.webappgen.frontend.models.common.FieldDefExtensions;

import java.util.Objects;
import java.util.Optional;

public abstract class ViewField implements ViewableElement, Comparable<ViewField> {
    private final FieldDef fieldDef;
    private String backingField;

    public ViewField(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
        this.backingField = fieldDef.getName();
    }

    protected FieldDef getFieldDef() {
        return fieldDef;
    }

    public String getBackingField() {
        return backingField;
    }

    void setBackingField(String backingField) {
        this.backingField = backingField;
    }

    public String getDisplayField() {
        return fieldDef.getName();
    }

    public String getLabel() {
        return "";
    }

    public String getReferredView() {
        return "";
    }

    public final boolean isAssociativeField() {
        return this instanceof OneManyField
                || this instanceof AssociativeInputField;
    }

    public final boolean isOneManyField() {
        return this instanceof OneManyField;
    }

    @Override
    public int compareTo(ViewField that) {
        Optional<DAttr.Type> thisDomainTypeOpt = FieldDefExtensions.getDomainType(this.fieldDef);
        Optional<DAttr.Type> thatDomainTypeOpt = FieldDefExtensions.getDomainType(that.fieldDef);
        if (thisDomainTypeOpt.isEmpty() || thatDomainTypeOpt.isEmpty()) {
            throw new IllegalArgumentException("Cannot have empty field defs!");
        }

        DAttr.Type thisDomainType = thisDomainTypeOpt.get();
        DAttr.Type thatDomainType = thatDomainTypeOpt.get();

        if (thisDomainType.isDomainReferenceType()) {
            if (this.fieldDef.getType().asClassOrInterfaceType().getTypeArguments().isPresent()) {
                if (that.fieldDef.getType().asClassOrInterfaceType().getTypeArguments().isPresent()) {
                    return 0;
                }
                return 1;
            }
            String thisTypeName = this.fieldDef.getType().asClassOrInterfaceType().getNameAsString();
            Class thisType = DomainTypeRegistry.getInstance().getDomainTypeByName(thisTypeName);
            String thatTypeName = this.fieldDef.getType().asClassOrInterfaceType().getNameAsString();
            Class thatType = DomainTypeRegistry.getInstance().getDomainTypeByName(thatTypeName);
            if (Objects.isNull(thisType) && !Objects.isNull(thatType)) {
                return -1;
            }
            if (Objects.isNull(thisType) && Objects.isNull(thatType)) {
                return 0;
            }
            if (thisType.isEnum() && (thatDomainType.isPrimitive() || thatType.isEnum())) {
                return 0;
            }
        }

//        if (thatDomainType.isDomainReferenceType()) {
//            return -that.compareTo(this);
//        }

        return 0;
    }
}
