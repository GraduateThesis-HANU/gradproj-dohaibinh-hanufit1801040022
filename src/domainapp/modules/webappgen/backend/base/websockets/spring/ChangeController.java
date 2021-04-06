package domainapp.modules.webappgen.backend.base.websockets.spring;

import org.springframework.messaging.core.MessageSendingOperations;

import java.util.Collection;

public class ChangeController {

    private MessageSendingOperations<String> template;

    public void onDomainObjectChanged(Collection<String> affectedTypes) {
        for (String affectedType : affectedTypes) {
            template.convertAndSend("/topics/changes/" + affectedType,
                Change.defaultChange());
        }
    }

    public static class Change {
        private final String change;
        private final String content;

        public Change(final String change, final String content) {
            this.change = change;
            this.content = content;
        }

        public String getChange() {
            return change;
        }

        public String getContent() {
            return content;
        }

        public static Change defaultChange() {
            return new Change(
                "",
                ""
            );
        }
    }
}
