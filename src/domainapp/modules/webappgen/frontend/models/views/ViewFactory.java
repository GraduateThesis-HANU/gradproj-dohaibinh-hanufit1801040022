package domainapp.modules.webappgen.frontend.models.views;

import domainapp.modules.common.model.parser.ClassAST;
import domainapp.modules.mccl.model.MCC;
import domainapp.modules.webappgen.backend.utils.InheritanceUtils;
import domainapp.modules.webappgen.frontend.utils.MCCUtils;

public final class ViewFactory {
    public static final View createListView(MCC viewDesc) {
        return new ListView(viewDesc);
    }

    public static final View createFormView(Class cls) {
        if (!InheritanceUtils.getSubtypesOf(cls).isEmpty()) {
            return new FormViewWithTypeSelect(cls);
        } else {
            return new FormView(createClassAST(cls));
        }
    }

    private static final ClassAST createClassAST(Class cls) {
        ClassAST classAST = new ClassAST(cls.getSimpleName(),
                MCCUtils.getFullPath(cls).toString());
        return classAST;
    }

}
