package domainapp.modules.webappgen.frontend.generators;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.module.view.AttributeDesc;
import domainapp.modules.common.model.parser.ClassAST;
import domainapp.modules.common.parser.ParserToolkit;
import domainapp.modules.webappgen.frontend.examples.utils.InheritanceUtils;
import domainapp.modules.webappgen.frontend.generators.utils.ClassAssocUtils;
import domainapp.modules.webappgen.frontend.generators.utils.DomainTypeRegistry;
import domainapp.modules.webappgen.frontend.generators.utils.MCCUtils;
import domainapp.modules.webappgen.frontend.generators.utils.ViewStateUtils;
import domainapp.modules.mccl.model.MCC;
import org.modeshape.common.text.Inflector;

import java.util.*;
import java.util.stream.Collectors;

import static domainapp.modules.webappgen.frontend.generators.utils.FileUtils.readWholeFile;

/**
 * Generate a form component from MCC.
 */
public class FormGenerator implements ViewGenerator {
    public enum Type {
        MAIN {
            @Override
            public String toString() {
                return "MainForm";
            }
        },
        FORM_ONLY {
            @Override
            public String toString() {
                return "Form";
            }
        },
        SUBFORM {
            @Override
            public String toString() {
                return "Submodule";
            }
        },
        LIST_ITEM {
            @Override
            public String toString() {
                return "ListItemView";
            }
        }
    }
    private static final Inflector inflector = Inflector.getInstance();
    private static final DomainTypeRegistry domainTypeRegistry = DomainTypeRegistry.getInstance();

    private final String template;
    private final MCC mcc;
    private final String title;
    private final String domainTypeName;
    private final Type type;
    private final List<String> imports;
    private String parent;

    public FormGenerator(MCC mcc, Type type) {
        this.type = type;
        this.mcc = mcc;
        this.title = getTitleFrom(mcc);
        this.domainTypeName = mcc.getDomainClass().getName();
        this.template = readWholeFile(getClass()
                .getClassLoader().getResource(getTemplateFile(type)).getFile());
        this.imports = new LinkedList<>();
        this.parent = "";
    }

    public FormGenerator(String domainTypeName, Type type) {
        this.title = "";
        this.mcc = null;
        this.domainTypeName = domainTypeName;
        this.type = type;
        this.template = readWholeFile(getClass()
                .getClassLoader().getResource(getTemplateFile(type)).getFile());
        this.imports = new LinkedList<>();
        this.parent = "";
    }

    public FormGenerator(String domainTypeName, Type type, String parent) {
        this(domainTypeName, type);
        this.parent = parent;
    }

    protected static String getTemplateFile(Type type) {
        switch (type) {
            case MAIN: return "react/templates/main_form.js";
            case FORM_ONLY: return "react/templates/form.js";
            case SUBFORM: return "react/templates/subform.js";
            case LIST_ITEM: return "react/templates/list_item.js";
            default: return null;
        }
    }

    public String getFormName() {
        return domainTypeName.concat(type.toString());
    }

    @Override
    public String generate() {
        String result = template
                .replace("{{ view.name.module }}", domainTypeName.concat("Module"))
                .replace("{{ view.name.main }}", domainTypeName.concat("MainForm"))
                .replace("{{ view.name.list }}", domainTypeName.concat("ListView"))
                .replace("{{ view.name.form }}", domainTypeName.concat("Form"))
                .replace("{{ view.name.submodule }}", domainTypeName.concat("Submodule"))
                .replace("{{ view.title }}", title)
                .replace("{{ possibleTypes }}", getPossibleSubtypesAsString())
                .replace("{{ view.dir }}", toFolderString(domainTypeName))
                .replace("{{ view.parent }}", parent);
        if (type == Type.SUBFORM) return result;
        return result.replace("{{ view.submodules }}", generateSubmodules())
                .replace("{{ view.form }}", generateFormJSX())
                .replace("{{ view.submodule.imports }}",
                        String.join("\n", imports).concat("\n"));
    }

    private static String toFolderString(String original) {
        Inflector inflector = Inflector.getInstance();
        return inflector.underscore(original)
                .toLowerCase(Locale.ROOT)
                .replace("_", "-");
    }

    private String generateSubmodules() {
        final Class<?> domainCls = domainTypeRegistry.getDomainTypeByName(domainTypeName);
        final List<Class<?>> associatedClasses = ClassAssocUtils.getNested(domainCls);
        final String importTemplate = "import %s from \"./%s\";";
        final StringBuilder result = new StringBuilder();
        for (Class<?> associatedClass : associatedClasses) {
            result.append(generateSubmodule(associatedClass.getSimpleName()))
                .append("\n");

            // add imports
            imports.add(String.format(importTemplate,
                    associatedClass.getSimpleName().concat("Submodule"),
                    associatedClass.getSimpleName().concat("Submodule")));
        }
        return result.toString();
    }

    private static String lowerFirstChar(String original) {
        if (Character.isLowerCase(original.charAt(0))) return original;
        if (original.length() == 1) return "" + Character.toLowerCase(original.charAt(0));
        return Character.toLowerCase(original.charAt(0))
                + original.substring(1);
    }

    private String generateSubmodule(String simpleName) {
        // generate submodule file
        final String template = "{this.props.excludes && this.props.excludes.includes(\"" + lowerFirstChar(simpleName) + "\") ? \"\" : <>\n" +
                "<{{ view.submodule.name }}\n" +
                "\tmode='submodule'\n" +
                "\tviewType={this.state.viewType}\n" +
                "\ttitle=\"{{ view.submodule.title }}\"\n" +
                "\tcurrent={this.state.current." + lowerFirstChar(simpleName) + "}\n" +
                "\tparentName='{{ view.parent }}'" +
                "\tparent={this.state.current}\n" +
                "\tparentId={this.state.currentId}\n" +
                "\tparentAPI={this.props.mainAPI}\n" +
                "\tpartialApplyWithCallbacks={this.partialApplyWithCallbacks} />" +
                "</>}";
        return template
                .replace("{{ view.parent }}", this.parent)
                .replace("{{ view.submodule.name }}", simpleName.concat("Submodule"))
                .replace("{{ view.submodule.title }}", "Manage ".concat(simpleName));
    }

    private String getPossibleSubtypesAsString() {
        Class<?> domainType = domainTypeRegistry
                .getDomainTypeByName(domainTypeName);
        Map<String, String> subtypeMap = InheritanceUtils.getSubtypeMapFor(domainType);
        if (!subtypeMap.isEmpty()) {
            return "return [\n\t".concat(String.join(", \n\t",
                    subtypeMap.keySet()
                            .stream()
                            .map(k -> "\"" + k + "\"")
                            .collect(Collectors.toList())))
                    .concat("]");
        }
        return "";
    }

    private String generateFormJSX() {
        Class<?> domainType = domainTypeRegistry
                .getDomainTypeByName(domainTypeName);
        Map<String, String> subtypeMap = InheritanceUtils.getSubtypeMapFor(domainType);
        if (subtypeMap.isEmpty()) {
            return "return (<>"
                    + generateSingleTypeRender(domainType)
                    + "</>);";
        } else {
            try {
                String caseStatements = generateTypeBasedRender(subtypeMap, domainType);
                return "switch (this.props.current.type) {"
                        + caseStatements
                        + "}";
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private String generateTypeBasedRender(Map<String, String> subtypeMap, Class<?> superClass) throws ClassNotFoundException {
        final Collection<String> caseStatements = new LinkedList<>();
        for (String className : subtypeMap.keySet()) {
            Class<?> cls = Class.forName(subtypeMap.get(className));
            String caseStatement = String.format(
                    "case '%s': return (<>%s</>);",
                    className,
                    generateSingleTypeRender(cls, superClass, true));
            caseStatements.add(caseStatement);
        }
        return String.join("\n", caseStatements);
    }

    public String generateSingleTypeRender(Class<?> cls) {
        String template = readWholeFile(
                getClass().getClassLoader()
                        .getResource("react/templates/react_view_form_body.js").getFile());
        ClassAST classAST = new ClassAST(cls.getSimpleName(), MCCUtils.getFullPath(cls).toString());
        Collection<FieldDeclaration> actualDomainFields = classAST.getDomainFields()
                .stream()
                .filter(f -> !f.isStatic())
                .collect(Collectors.toList());
        String formBody = String.join("\n<br />\n",
                generateViewInputFields(classAST, actualDomainFields)
                    .stream()
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        return template.replace("{{ form.body }}", formBody);
    }

    public String generateSingleTypeRender(Class<?> cls,
                                           Class<?> superClass,
                                           boolean hasTypeSelect) {
        String template = readWholeFile(
                getClass().getClassLoader()
                        .getResource("react/templates/react_view_form_body.js").getFile());
        ClassAST classAST = new ClassAST(cls.getSimpleName(), MCCUtils.getFullPath(cls).toString());
        Collection<FieldDeclaration> fields = new LinkedList<>();
        if (classAST.getDomainFields() != null) {
            fields.addAll(classAST.getDomainFields());
        }
        ClassAST superClassAST = new ClassAST(superClass.getSimpleName(), MCCUtils.getFullPath(superClass).toString());
        fields.addAll(superClassAST.getDomainFields());
        fields = fields.stream().filter(field -> !field.isStatic()).collect(Collectors.toList());

        Collection<String> inputFields = new LinkedList<>();
        if (hasTypeSelect) {
            inputFields.add(generateTypeSelect(InheritanceUtils.getSubtypeMapFor(superClass).keySet()));
        }
        inputFields.addAll(generateViewInputFields(classAST, fields)
                .stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList()));
        String formBody = String.join("\n<br />\n", inputFields);
        return template.replace("{{ form.body }}", formBody);
    }

    private String generateTypeSelect(Collection<String> possibleTypes) {
        return createSelectOptionField(
                "Type",
                "type",
                new LinkedList<>(possibleTypes),
                possibleTypes != null && !possibleTypes.isEmpty());
    }

    protected String reorderImports(String src) {
        String[] lines = src.trim().split("\n");
        StringBuilder result = new StringBuilder();
        final List<String> importStatements = new LinkedList<>();
        for (String line : lines) {
            if (isImportStatement(line)) {
                importStatements.add(line);
            } else {
                result.append(line).append("\n");
            }
        }
        return String.join("\n", importStatements)
                .concat("\n")
                .concat(result.toString());
    }

    private static boolean isImportStatement(String srcLine) {
        return srcLine.startsWith("import");
    }

    private static String getTitleFrom(MCC mcc) {
        return mcc.getViewFields().stream()
                .filter(vf -> ParserToolkit.getFieldName(vf).equals("title"))
                .findFirst()
                .stream()
                .map(fieldDeclaration -> ParserToolkit.getAnnotation(fieldDeclaration, AttributeDesc.class))
                .map(MCCUtils::getExpressionMap)
                .map(attrs -> (LiteralStringValueExpr)attrs.get("label"))
                .map(LiteralStringValueExpr::getValue)
                .findFirst()
                .get();
    }

    private static String createSelectOptionField(String label,
                                                  String inputName,
                                                  Collection<Object> possibleValues,
                                                  boolean isTypeSelect) {
        String onChange = isTypeSelect ? "onChange={this.props.handleTypeChange} "
                : String.format("onChange={(e) => this.props.handleStateChange(\"current.%s\", e.target.value, false)}", inputName);
        final StringBuilder selectOptionField = new StringBuilder();
        selectOptionField.append("<FormGroup>").append("\n");
        selectOptionField.append(String.format("  <Form.Label>%s</Form.Label>\n", label));
        selectOptionField
                .append(String.format(
                        "  <Form.Control as=\"select\" " +
                                "value={this.props.current.%s} " +
                                onChange +
                                (isTypeSelect ? "disabled={this.props.viewType !== \"create\"} " : "") +
                                "custom>",
                        inputName, inputName, false))
                .append("\n");
        selectOptionField.append("    <option value='0' disabled selected>&lt;Please choose one&gt;</option>");
        for (Object possibleValue : possibleValues) {
            selectOptionField.append("    <option value=")
                    .append("\"")
                    .append(possibleValue)
                    .append("\"")
                    .append(">")
                    .append(possibleValue)
                    .append("</option>")
                    .append("\n");
        }
        selectOptionField.append("  </Form.Control>").append("\n");
        selectOptionField.append("</FormGroup>");
        return selectOptionField.toString();
    }

    Collection<String> generateViewInputFields(ClassAST classAST,
            Collection<FieldDeclaration> viewFields) {
        boolean shouldHaveId = true;
        Collection<String> formGroups = new LinkedList<>();
        for (FieldDeclaration viewField : viewFields) {
            if (!shouldHaveId && ViewStateUtils.isIdOrAuto(viewField)) continue;
            String viewInputField = generateViewInputField(viewField, classAST.getCls(), true);
            formGroups.add(viewInputField);
        }
        return formGroups;
    }

    static String getLabelFromAttrDescOrDefault(FieldDeclaration viewField,
                                                MCC mcc,
                                                ClassOrInterfaceDeclaration domainClass) {
        if (mcc != null && mcc.getViewFields().contains(viewField)) {
            NormalAnnotationExpr attributeDesc = ParserToolkit.getAnnotation(viewField, AttributeDesc.class);
            Map<String, Expression> expressionMap = MCCUtils.getExpressionMap(attributeDesc);
            return ((LiteralStringValueExpr) expressionMap.get("label")).getValue();
        } else {
            Map<String, Object> domainAttrDefs = ParserToolkit.getFieldDefFull(viewField)
                    .getAnnotation(DAttr.class)
                    .getProperties()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            String fieldName = ParserToolkit.getFieldName(viewField);
            if (mcc == null) return fieldName;
            Optional<FieldDeclaration> optionalViewField = mcc.getViewFields().stream()
                    .filter(field ->
                            ParserToolkit.getFieldName(field).equals(fieldName))
                    .findFirst();
            if (optionalViewField.isPresent()) {
                return getLabelFromAttrDescOrDefault(optionalViewField.get(), mcc, domainClass);
            } else {
                Object labelName = domainAttrDefs.get("name");
                if (labelName instanceof NameExpr) {
                    return ViewStateUtils.initValueOf((NameExpr) labelName,
                            domainClass).toString();
                } else if (labelName instanceof LiteralStringValueExpr) {
                    return ((LiteralStringValueExpr) labelName).getValue();
                } else {
                    return labelName.toString();
                }
            }
        }

    }

    private static String capitalizedHumanReadable(String original) {
        return inflector.capitalize(inflector.humanize(inflector.underscore(original)));
    }

    private String generateViewInputField(FieldDeclaration viewField,
                                  ClassOrInterfaceDeclaration domainClass,
                                  boolean fromDomainField) {
        String label = capitalizedHumanReadable(
                getLabelFromAttrDescOrDefault(viewField, mcc, domainClass));

        Map<String, Object> domainAttrDefs;
        if (fromDomainField) {
            domainAttrDefs = ParserToolkit.getFieldDefFull(viewField)
                    .getAnnotation(DAttr.class)
                    .getProperties()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            domainAttrDefs = ViewStateUtils.getDomainAttrsOfViewField(viewField, domainClass);
        }
        DAttr.Type type = (DAttr.Type) domainAttrDefs.get("type");
        List<String> extraAttrs = new LinkedList<>();

        Boolean isId = (Boolean) domainAttrDefs.getOrDefault("id", null);
        Boolean isAuto = (Boolean) domainAttrDefs.getOrDefault("auto", null);
        if ((isId != null && isId) || (isAuto != null && isAuto)) {
            extraAttrs.add("disabled");
        }

        Object domainFieldNameObj = domainAttrDefs.get("name");
        String domainFieldName;
        if (domainFieldNameObj instanceof NameExpr) {
            domainFieldName = ViewStateUtils.initValueOf((NameExpr) domainFieldNameObj, domainClass).toString();
        } else {
            domainFieldName = domainFieldNameObj.toString();
        }

        // TODO: have a case to create select/option box
        String inputType = "";
        if (type.isString()) {
            inputType = "text";
        } else if (type.isDate()) {
            inputType = "date";
        } else if (type.isNumeric()) {
            inputType = "number";
        } else if (type.isColor()) {
            inputType = "color";
        } else if (type.isBoolean()) {
            // TODO: create a checkbox
        } else if (type.isDomainType()) {
            String shortFieldTypeName = ParserToolkit.getFieldDefFull(viewField)
                    .getType()
                    .asClassOrInterfaceType()
                    .getNameAsString();
            Class<?> actualFieldType = domainTypeRegistry.getDomainTypeByName(shortFieldTypeName);
            if (actualFieldType == null) {
                return "";
            }
            if (actualFieldType.isEnum()) {
                return createSelectOptionField(
                        label, domainFieldName,
                        List.of(actualFieldType.getEnumConstants()), false);
            }

            Map<String, Object> assoc = ParserToolkit.getFieldDefFull(viewField)
                    .getAnnotation(DAssoc.class)
                    .getProperties()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (assoc.get("ascType").equals(DAssoc.AssocType.One2One)) {
                return "";
            }

            inputType = "text";
            extraAttrs.add("disabled");

            // return this form control plus another for its ID
            // get the id field
            if (!actualFieldType.isEnum()) {
                ClassAST classAST = new ClassAST(shortFieldTypeName, MCCUtils.getFullPath(actualFieldType).toString());
                FieldDeclaration itsIdInputField = MCCUtils.getIdFieldOf(classAST);
                // { this.props.excludes && this.props.excludes.includes("student") ? "" : <></> }
                String fName = Character.toLowerCase(shortFieldTypeName.charAt(0)) + shortFieldTypeName.substring(1);
                return "{ this.props.excludes && this.props.excludes.includes(\""+ fName +"\") ? \"\" : <>" +
                        "<FormGroup className='d-flex justify-content-between'>" +
                        generateViewInputField(itsIdInputField, classAST.getCls(), domainFieldName)
                                .replace("FormGroup", "Col")
                                .replace("<Col", "<Col md={2.5} className='px-0'")
                        .concat("\n")
                        .concat(generateTemplatedViewInputField(
                                    label, inputType, domainFieldName, extraAttrs, false)
                                .replace("FormGroup", "Col")
                                .replace("<Col", "<Col md={9} className='px-0'"))
                        .concat("</FormGroup>")
                        .concat("</> }");
                // generateViewInputField(itsIdInputField, classAST.getCls()));
            }
        } else if (type.isCollection()) {
            inputType = "";
        }

        if (inputType.isEmpty()) {
            return "";
        }

        return generateTemplatedViewInputField(
                label, inputType, domainFieldName, extraAttrs, false);
    }

    static String generateViewInputField(FieldDeclaration field,
                                         ClassOrInterfaceDeclaration domainClass,
                                         String referredField) {
        String label = referredField + " "
                + capitalizedHumanReadable(ParserToolkit.getFieldName(field));
        Map<String, Object> domainAttrDefs = ViewStateUtils.getDomainAttrsOfViewField(field, domainClass);
        String fieldName = ParserToolkit.getFieldName(field);

        DAttr.Type type = (DAttr.Type) domainAttrDefs.get("type");
        String inputType = "";
        if (type.isString()) {
            inputType = "text";
        } else if (type.isDate()) {
            inputType = "date";
        } else if (type.isNumeric()) {
            inputType = "number";
        } else if (type.isColor()) {
            inputType = "color";
        } else if (type.isBoolean()) {

        }
        return generateTemplatedViewInputField(
                label,
                inputType,
                referredField + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1),
                List.of(),
                true
        );
    }

    static String generateTemplatedViewInputField(String label,
                                                  String inputType,
                                                  String stateFieldName,
                                                  List<String> extraAttrs,
                                                  boolean needSyncing) {
        final StringBuilder result = new StringBuilder();

        result.append("<FormGroup>").append("\n");
        result.append(String.format("  <Form.Label>%s</Form.Label>\n", label));
        result.append(
                String.format("  <FormControl type=\"%s\" " +
                                "value={this.renderObject(\"current.%s\")} " +
                                "onChange={(e) => this.props.handleStateChange(\"current.%s\", e.target.value, %s)} %s />\n",
                        inputType, stateFieldName, stateFieldName, needSyncing, String.join(" ", extraAttrs)));
        result.append("</FormGroup>");
        return result.toString();
    }
}
