package domainapp.modules.webappgen.frontend.models.views;

import java.util.Collection;

public interface HasSubmodule {
    Collection<SubmoduleView> getSubmoduleViews();
}
