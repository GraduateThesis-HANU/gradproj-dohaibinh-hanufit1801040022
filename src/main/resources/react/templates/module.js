import {{ view.name.main }} from "./{{ view.name.main }}";
import BaseAPI from "../base/BaseAPI";
import providers from "../common/BackendApiProviders";

{{ view.apis.declarations }}

export default function {{ view.name.module }}(props) {
  return <{{ view.name.main }}
    mainAPI={{{ view.api.main }}}
    {{ view.api.bindings }}
    {...props}
  />
}