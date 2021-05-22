package domainapp.modules.webappgen.frontend.models.common;

import com.github.javaparser.ast.body.FieldDeclaration;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.modules.common.parser.ParserToolkit;
import domainapp.modules.common.parser.statespace.metadef.FieldDef;
import domainapp.modules.webappgen.frontend.utils.DomainTypeRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class FieldDefExtensions {
    public static Optional<DAttr.Type> getDomainType(FieldDef fieldDef) {
        return Optional.ofNullable(fieldDef.getAnnotation(DAttr.class))
                .flatMap(dAttr -> dAttr.getProperties()
                        .stream()
                        .filter(prop -> prop.getKey().equals("type"))
                        .findFirst())
                .map(entry -> (DAttr.Type)entry.getValue());
    }

    public static boolean isPrimitiveOrEnumType(FieldDef fieldDef) {
        final Optional<DAttr.Type> domainTypeOpt = FieldDefExtensions.getDomainType(fieldDef);
        if (domainTypeOpt.isEmpty()) return false;
        DAttr.Type domainType = domainTypeOpt.get();

        if (domainType.isPrimitive() || domainType.isDate()
                || domainType.isColor()) return true;

        if (domainType.isCollection()) return false;

        if (domainType.isDomainReferenceType()) {
            String thisTypeName = fieldDef.getType().asClassOrInterfaceType().getNameAsString();
            Class thisType = DomainTypeRegistry.getInstance().getDomainTypeByName(thisTypeName);

            if (Objects.isNull(thisType) || thisType.isEnum()) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Object> getAssociation(FieldDef fieldDef) {
        return fieldDef.getAnnotation(DAssoc.class)
                .getProperties()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> getAttribute(FieldDef fieldDef) {
        return fieldDef.getAnnotation(DAttr.class)
                .getProperties()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static FieldDeclaration getCorrespondingDomainField(
            final FieldDeclaration viewField,
            final Collection<FieldDeclaration> domainFields) {
        return domainFields.stream()
                .filter(field -> ParserToolkit.getFieldName(field)
                        .equals(ParserToolkit.getFieldName(viewField)))
                .findFirst()
                .orElse(null);
    }

    public static FieldDeclaration getCorrespondingViewField(
            final FieldDeclaration domainField,
            final Collection<FieldDeclaration> viewFields) {
        return viewFields.stream()
                .filter(field -> ParserToolkit.getFieldName(field)
                        .equals(ParserToolkit.getFieldName(domainField)))
                .findFirst()
                .orElse(null);
    }
}
