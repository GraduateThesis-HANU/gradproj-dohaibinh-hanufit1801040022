package domainapp.modules.webappgen.frontend.models;

import domainapp.modules.webappgen.frontend.templates.JsTemplate;

public interface JsFrontendElement {
    JsTemplate getTemplate();
    String getAsString();
}
