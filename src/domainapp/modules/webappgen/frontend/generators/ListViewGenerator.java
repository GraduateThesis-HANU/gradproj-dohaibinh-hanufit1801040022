package domainapp.modules.webappgen.frontend.generators;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.modules.common.model.parser.ClassAST;
import domainapp.modules.common.parser.ParserToolkit;
import domainapp.modules.common.parser.statespace.metadef.MetaAttrDef;
import domainapp.modules.webappgen.frontend.examples.utils.InheritanceUtils;
import domainapp.modules.webappgen.frontend.generators.utils.DomainTypeRegistry;
import domainapp.modules.webappgen.frontend.generators.utils.FileUtils;
import domainapp.modules.webappgen.frontend.generators.utils.MCCUtils;
import domainapp.modules.mccl.model.MCC;
import org.modeshape.common.text.Inflector;

import java.util.*;
import java.util.stream.Collectors;

public class ListViewGenerator implements ViewGenerator {
    private static final Inflector inflector = Inflector.getInstance();
    private static final String templateFile = "react/templates/list.js";
    private final String template;
    private final MCC mcc;
    private final String objectName;
    private final Collection<String> dependentModules;

    public ListViewGenerator(MCC mcc, String objectName, Collection<String> dependentModules) {
        this.mcc = mcc;
        this.template = FileUtils.readWholeFile(getClass()
                .getClassLoader().getResource(templateFile).getFile());
        this.objectName = objectName;
        this.dependentModules = new LinkedList<>(dependentModules);
        this.dependentModules.add(objectName);
    }

    private String generateHeading(FieldDeclaration viewField) {
        String columnHeading = ParserToolkit.getAnnotation(viewField, AttributeDesc.class)
                .getPairs()
                .stream()
                .filter(entry -> entry.getNameAsString().equals("label"))
                .findFirst()
                .map(MemberValuePair::getValue)
                .map(Expression::asLiteralStringValueExpr)
                .map(LiteralStringValueExpr::getValue)
                .orElse("");
        if (columnHeading.isEmpty()) return "";
        return String.format(
                "<th>%s</th>",
                columnHeading
        );
    }

    private String generateDomainHeading(FieldDeclaration viewField) {
        String columnHeading = ParserToolkit.getFieldDefFull(viewField)
                .getAnnotation(DAttr.class)
                .getProperties()
                .stream()
                .filter(x -> x.getKey().equals("name"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("")
                .toString();
        if (columnHeading.isEmpty()) return "";
        columnHeading = inflector.underscore(columnHeading);
        columnHeading = inflector.humanize(columnHeading);
        columnHeading = inflector.capitalize(columnHeading);
        return String.format(
                "<th>%s</th>",
                columnHeading
        );
    }

    private static boolean isRequiredField(FieldDeclaration field) {
        return ParserToolkit.getFieldDefFull(field)
                .getAnnotation(DAttr.class)
                .getProperties()
                .stream()
                .filter(entry ->
                        entry.getKey().equals("optional")
                                && entry.getValue().equals(Boolean.TRUE))
                .findAny()
                .isEmpty();
    }

    private String generateHeadings() {
        if (mcc == null) {
            Class<?> domainClass = DomainTypeRegistry.getInstance()
                    .getDomainTypeByName(objectName);
            ClassAST domainClassAST = new ClassAST(objectName, MCCUtils.getFullPath(domainClass).toString());
            return String.join("\n",
                    domainClassAST.getDomainFields().stream()
                        .filter(vf -> !vf.isStatic())
                        .filter(vf -> isRequiredField(vf))
                        .map(this::generateDomainHeading)
                        .filter(heading -> !heading.isEmpty())
                        .collect(Collectors.toList()));
        }
        return String.join("\n",
                mcc.getViewFields().stream()
                        .filter(vf -> !ParserToolkit.getFieldName(vf).equals("title"))
                        .filter(vf -> ListItemViewGenerator.isDisplayField(mcc, vf))
                        .map(this::generateHeading)
                        .filter(heading -> !heading.isEmpty())
                        .collect(Collectors.toList()));
    }

    @Override
    public String generate() {
        String headings = generateHeadings();
        Collection<String> possibleSubtypes = getPossibleSubtypes();
        return template
                .replace("{{ view.name.list }}", objectName.concat("ListView"))
                .replace("{{ view.name.listItem }}", objectName.concat("ListItemView"))
                .replace("{{ view.api.list }}", dependentModules.stream()
                        .map(s -> lowerFirstChar(s).concat("API"))
                        .reduce((s1, s2) -> s1 + ", " + s2).orElse(""))
                .replace("{{ view.api.bindings }}", dependentModules.stream()
                        .map(ListViewGenerator::apiBindingFromModuleName)
                        .reduce((s1, s2) -> s1 + "\n" + s2).orElse(""))
                .replace("{{ view.api.main }}", lowerFirstChar(objectName).concat("API"))
                .replace("{{ view.list.headings }}", headings)
                .replace("{{ view.typing.possibleTypes }}",
                        possibleSubtypes == null ? "undefined" :
                        "{" + String.join(",\n",
                                possibleSubtypes.stream()
                                        .map(s -> s + ": \"" + s + "\"")
                                        .collect(Collectors.toList())) + "}");
    }

    private Collection<String> getPossibleSubtypes() {
        String simpleDomainClassName = objectName;
        Class<?> domainClass = DomainTypeRegistry.getInstance()
                .getDomainTypeByName(simpleDomainClassName);
        Map<String, String> subtypeMap = InheritanceUtils.getSubtypeMapFor(domainClass);
        if (subtypeMap.isEmpty()) return null;
        return subtypeMap.keySet();
    }

    private static String apiBindingFromModuleName(String moduleName) {
        String apiObjectName = lowerFirstChar(moduleName).concat("API");
        return String.format("%s={this.props.%s}", apiObjectName, apiObjectName);
    }

    private static String lowerFirstChar(String original) {
        if (Character.isLowerCase(original.charAt(0))) return original;
        if (original.length() == 1) return "" + Character.toLowerCase(original.charAt(0));
        return Character.toLowerCase(original.charAt(0))
                + original.substring(1);
    }

    public static class ListItemViewGenerator
            extends FormGenerator implements ModalViewGenerator {
        private Collection<String> displayFields;

        public ListItemViewGenerator(MCC mcc) {
            super(mcc, Type.LIST_ITEM);
            this.displayFields = getDisplayFields(mcc);
        }

        public ListItemViewGenerator(String domainTypeName) {
            super(domainTypeName, Type.LIST_ITEM);
            this.displayFields = getDisplayFields(domainTypeName);
        }

        @Override
        public String generateModalTrigger() {
            return String.format("<%s onClick={this.handleShow} {...this.props} />", getFormName());
        }

        @Override
        public String generate() {
            String result = super.generate()
                    .replace("{{ view.name.listItem }}", getFormName())
                    .replace("{{ displayFields }}", String.join("\n", displayFields));
            return reorderImports(result);
        }

        private static Collection<String> getDisplayFields(String domainTypeName) {
            Class<?> domainType = DomainTypeRegistry.getInstance()
                    .getDomainTypeByName(domainTypeName);
            ClassAST domainClassAST = new ClassAST(domainTypeName,
                    MCCUtils.getFullPath(domainType).toString());
            return domainClassAST.getDomainFields()
                    .stream()
                    .filter(vf -> !vf.isStatic())
                    .filter(vf -> isRequiredField(vf))
                    .map(ParserToolkit::getFieldDefFull)
                    .map(fieldDef -> fieldDef.getAnnotation(DAttr.class))
                    .filter(Objects::nonNull)
                    .map(attr -> attr.getProperties()
                            .stream().filter(x -> x.getKey().equals("name"))
                            .map(Map.Entry::getValue)
                            .findFirst().orElse(""))
                    .map(s -> String.format("<td style={this.verticalAlignCell} onClick={this.changeCurrent}>{this.renderObject(this.props.current.%s)}</td>", s))
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        private static Collection<String> getDisplayFields(MCC mcc) {
            return mcc.getViewFields()
                    .stream()
                    .filter(vf -> !ParserToolkit.getFieldName(vf).equals("title"))
                    .filter(vf -> isDisplayField(mcc, vf))
                    .map(ParserToolkit::getFieldName)
//                    .map(fieldName -> getDisplayName(mcc, fieldName))
                    .filter(Objects::nonNull)
                    .map(s -> String.format("<td style={this.verticalAlignCell} onClick={this.changeCurrent}>{this.renderObject(this.props.current.%s)}</td>", s))
                    .collect(Collectors.toList());
        }

        private static boolean isDisplayField(MCC mcc, FieldDeclaration viewField) {
            String name = ParserToolkit.getFieldName(viewField);
            Optional<FieldDeclaration> fieldDeclarationOptional =
                    mcc.getDomainClass()
                            .getDomainFieldsByName(List.of(name))
                            .stream()
                            .findFirst();
            if (fieldDeclarationOptional.isEmpty()) return false;
            FieldDeclaration fieldDeclaration = fieldDeclarationOptional.get();
            MetaAttrDef annotation = ParserToolkit.getFieldDefFull(fieldDeclaration)
                    .getAnnotation(DAssoc.class);
            if (annotation == null) return true;
            Map<String, Object> dAssoc = Optional.of(annotation)
                    .map(metaAttrDef -> metaAttrDef.getProperties().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    .orElse(Map.of());
            Object ascType = dAssoc.get("ascType");
            Object endType = dAssoc.get("endType");
            // has no @DAssoc or has @DAssoc(ascType=One2Many, endType=Many)
            boolean result = (ascType.equals(DAssoc.AssocType.One2Many) & endType.equals(DAssoc.AssocEndType.Many))
                    || ascType.equals(DAssoc.AssocType.One2One);
            return result;
        }
    }
}
