package domainapp.modules.webappgen.frontend.templates;

import domainapp.modules.webappgen.frontend.utils.FileUtils;

final class TemplateReader {

    /**
     * Read template from /resources/react/templates folder
     *
     * @param fileName
     * @return
     */
    synchronized static String readFromFile(String fileName) {
        return FileUtils.readWholeFile(TemplateReader.class.getClassLoader()
                .getResource("react/templates/" + fileName + ".js").getFile());
    }
}
