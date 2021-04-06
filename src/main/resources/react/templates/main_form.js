import React from "react";
import BaseMainForm from "../base/BaseMainForm";
import {{ view.name.list }} from "./{{ view.name.list }}";
import {{ view.name.form }} from "./{{ view.name.form }}";
{{ view.submodule.imports }}

export default class {{ view.name.main }} extends BaseMainForm {
  constructor(props) {
    super(props);
    this.state = {
      ...this.state,
      current: {
        ...this.state.current,
        type: this.getPossibleTypes.bind(this)() ? this.getPossibleTypes()[0] : undefined
      }
    }
  }
  getPossibleTypes() {
    {{ possibleTypes }}
  }

  renderTitle() {
    return (
      <>
        <h2 className="text-center">Manage: {{ view.title }}</h2>
      </>
    );
  }

  renderListView() {
    return <{{ view.name.list }} {...this.props} {...this.state}
      changeToDetailsView={() => this.handleStateChange("viewType", "details")}
      handleStateChange={this.handleStateChange}
      partialApplyWithCallbacks={this.partialApplyWithCallbacks} />
  }

  renderForm() {
    return <{{ view.name.form }} {...this.props} {...this.state}
      handleStateChange={this.handleStateChange.bind(this)}
      handleTypeChange={(e) => this.setState({ current: {...this.state.current, type: e.target.value} })} />;
  }

  renderSubmodules() {
    return (<>
      {{ view.submodules }}
      </>);
  }
}