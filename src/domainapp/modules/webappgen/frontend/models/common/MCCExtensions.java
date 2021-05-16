package domainapp.modules.webappgen.frontend.models.common;

import com.github.javaparser.ast.body.FieldDeclaration;
import domainapp.basics.model.meta.DAttr;
import domainapp.modules.common.parser.ParserToolkit;
import domainapp.modules.common.parser.statespace.metadef.FieldDef;
import domainapp.modules.mccl.model.MCC;

public final class MCCExtensions {
    public static FieldDef getIdFieldDef(MCC mcc) {
        return mcc.getDomainClass().getDomainFields()
                .stream()
                .filter(MCCExtensions::isIdField)
                .findFirst()
                .map(ParserToolkit::getFieldDefFull)
                .orElse(null);
    }

    private static boolean isIdField(FieldDeclaration fieldDeclaration) {
        return ParserToolkit.getAnnotation(fieldDeclaration, DAttr.class)
                .getPairs()
                .stream()
                .filter(pair -> pair.getNameAsString().equals("id"))
                .map(ParserToolkit::parseAnoMemberValue)
                .map(val -> (Boolean) val)
                .findFirst()
                .orElse(false);
    }
}
