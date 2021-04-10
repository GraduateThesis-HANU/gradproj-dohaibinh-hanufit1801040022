import React from "react";
import { Button, Col, Container, Form, FormControl, Row } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from '@fortawesome/free-solid-svg-icons';
import SockJS from "sockjs-client";
import QuickScrollFab from "../common/QuickScrollFab";
import AutoDismissAlert from "../common/AutoDismissAlert";
import DeleteConfirmation from "../common/DeleteConfirmation";
import constants from "../common/Constants";
import { StompOverWSClient } from "../common/StompClient";
import { CustomToast, ToastWrapper } from "../common/Toasts";

export default class BaseMainForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      current: {}, // list or single object
      viewType: props.viewType ? props.viewType : "create", // create | details | browse (list) | submodule
      currentId: undefined, // filtered ID
      searchInput: undefined // input for search box
    };
    // method binding
    this.renderActionButtons = this.renderActionButtons.bind(this);
    this.renderSubmodules = this.renderSubmodules.bind(this);
    this.renderNavigationButtons = this.renderNavigationButtons.bind(this);
    this.renderSearchInput = this.renderSearchInput.bind(this);
    this.renderTopButtons = this.renderTopButtons.bind(this);
    this.renderObject = this.renderObject.bind(this);
    this._renderObject = this._renderObject.bind(this);
    
    this.setAlert = this.setAlert.bind(this);
    this.resetState = this.resetState.bind(this);
    this.filterByType = this.filterByType.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.getCreateHandler = this.getCreateHandler.bind(this);
    this.getPossibleTypes = this.getPossibleTypes.bind(this);
    this.onOperationFailed = this.onOperationFailed.bind(this);
    this.handleStateChange = this.handleStateChange.bind(this);
    this.onOperationSuccess = this.onOperationSuccess.bind(this);
    this.retrieveObjectById = this.retrieveObjectById.bind(this);
    this.handleDeepStateChange = this.handleDeepStateChange.bind(this);
    this.updateCurrentObjectState = this.updateCurrentObjectState.bind(this);
    this.partialApplyWithCallbacks = this.partialApplyWithCallbacks.bind(this);
  }

  // lifecycle
  componentDidMount() {
    const socket = new SockJS(`${constants.host}/domainapp-ws`);
    const stompClient = new StompOverWSClient(socket);
    const self = this;
    stompClient.register([
      {
        endpoint: `/topic/${this.props.mainAPI.objectNamePlural}`,
        callback: (response) => {
          const message = JSON.parse(response.body).content;
          const notiList = self.state.notifications ? self.state.notifications : []
          const index = notiList.length;
          self.setState({
            notifications: [
              ...notiList,
              <CustomToast header="Notification" timeout={100000}
                  onClick={() => window.location.reload()}
                  onClose={() => self.setState({
                    notifications: self.state.notifications.splice(index, 1)
                  })}
                  children={message} />
            ]
          })
        }
      }
    ]);
  }

  // methods for view logic
  getPossibleTypes() { }
  filterByType(type) {
    if (!this.getPossibleTypes()) return;
    this.props.mainAPI.getByPageAndType([
      1, type,
      result => {
        this.setState({ current: result })}
    ]);
  }
  
  resetState() {
    this.setState({
      current: {
        type: this.getPossibleTypes() ? this.getPossibleTypes()[0] : undefined
      },
      currentId: undefined
    })
  }
  
  getCreateHandler() {
    if (this.props.parent) {
      const fn = this.partialApplyWithCallbacks(this.props.parentAPI.createInner);
      const objectNamePlural = this.props.mainAPI.objectNamePlural;
      const parentId = this.props.parentId;
      const parentName = this.props.parentName;
      const parentValue = this.props.parent;
      return function([data]) {
        data[parentName] = parentValue;
        return fn([data, objectNamePlural, parentId])
      }
    } else {
      return this.partialApplyWithCallbacks(this.props.mainAPI.create);
    }
  }

  handleSubmit() {
    const createUsing = this.getCreateHandler();
    const updateUsing = this.partialApplyWithCallbacks(this.props.mainAPI.updateById)
    if (this.state.viewType === "create" 
      || !this.state.currentId || this.state.currentId === "") {
      createUsing([this.state.current]);
    } else if (this.state.viewType === "details") {
      updateUsing([this.state.currentId, this.state.current]);
    }
  }

  handleStateChange(stateName, newValue, needsApiCall = false, onDone = undefined) {
    let newState = {};
    if (stateName.includes(".") && stateName.indexOf(".") === stateName.lastIndexOf(".")) {
      const outer = stateName.split(".")[0];
      const inner = stateName.split(".")[1];
      this.handleDeepStateChange(outer, inner, newValue, needsApiCall, onDone);
    } else {
      newState[stateName] = newValue;
    }
    
    if (needsApiCall) {
      const stateObjName = stateName.replace("Id", "");
      if (stateObjName.includes("current.")) {
        if (newValue === "") return;
        const shortName = stateObjName.replace("current.", "");
        this.retrieveObjectById(shortName, newValue,
          (result) => {
            newState["current"] = {...this.state.current};
            newState["current"][shortName] = result;
            this.setState(newState, onDone);
          },
          () => {
            newState["current"] = {...this.state.current};
            newState["current"][shortName] = "";
            this.setState(newState, onDone);
          });
      } else {
        this.retrieveObjectById(stateObjName, newValue,
          (result) => { newState[stateObjName] = result; this.setState(newState, onDone); },
          () => { newState[stateObjName] = ""; this.setState(newState, onDone); });
      }
    } else {
      this.setState(newState, onDone);
    }
  }
  handleDeepStateChange(outerName, innerName, newValue, needsApiCall, onDone) {
    let outer = this.state[outerName]; outer[innerName] = newValue;
    let newState = {}; newState[outerName] = outer;
    // ignoring `needsApiCall` for simplicity
    this.setState(newState, onDone);
  }
  renderObject(propPath) {
    const keys = propPath.split(".");
    let prop = this.props;
    for (let key of keys) {
      try {
        prop = prop[key];
      } catch (err) {
        return this._renderObject(undefined);
      }
    }
    return this._renderObject(prop);
  }
  _renderObject(obj) {
    if (obj === null || obj === undefined) {
      return "";
    }
    if (typeof (obj) === "object") {
      return Object.keys(obj)
        .map(key => obj[key])
        .reduce((k1, k2) => "" + k1 + "-" + k2);
    } else {
      return obj;
    }
  }
  retrieveObjectById(name, id, onSuccess, onFailure) {
    if (name === "current") {
      const className = this.constructor.name.replace("MainForm", "");
      const propName = className.charAt(0).toLowerCase() + className.substring(1);
      return this.retrieveObjectById(propName, id, onSuccess, onFailure);
    } else {
      const actualName = name.replace(".id", "").replace("current.", "");
      console.log(actualName)
      this.props[actualName + "API"].getById([id, onSuccess, onFailure]);
    }
  }

  updateCurrentObjectState(evt) {
    this.setState({ currentId: evt.target.value },
      function () {
        this.handleStateChange("currentId", this.state.currentId, true,
          function () {
            if (this.state.currentId && this.state.current !== {} 
                && !(this.state.current instanceof Array)) {
              this.handleStateChange("viewType", "details");
            } else {
              this.handleStateChange("viewType", "create");
            }
          });
      });
  }
  setAlert(variant, heading, text,
    onDisposed = () => this.setState({ alert: undefined })) {
    this.setState({
      alert: <AutoDismissAlert variant={variant}
        heading={heading} text={text} onDisposed={onDisposed} />
    })
  }
  onOperationSuccess(result) {
    if (result instanceof String) {
      this.setAlert("danger", "Message", result);
      return;
    }
    const extra = result && !(result instanceof(Response)) ?
        ` Affected: ${this._renderObject(result)}!` : "";
    // update UI somewhere here
    if (!(result instanceof(Response))) { // if not void
      this.handleStateChange("current", result);
    }
    // this.handleStateChange("currentId", "", true);
    this.setAlert("success", "Success", "Operation completed!" + extra);
  }

  onOperationFailed(err) {
    const reason = err ? ` Reason: ${err}` : "";
    console.trace(err)
    this.setAlert("danger", "Failure", "Operation failed!" + reason);
  }

  setListFromPage(page) {
    this.setState({
      list: page.content
    });
  }

  partialApplyWithCallbacks(func) {
    return args => {
      const oldArgs = args ? args : [];
      return func([...oldArgs, this.onOperationSuccess, this.onOperationFailed]);
    }
  }

  // base methods for drawing view
  renderSubmodules() { }

  renderNavigationButtons() {
    return (<>
      <Col className="px-0">
        <Button className="mr-2" variant="primary"
          onClick={() => this.handleStateChange(
            "viewType", "create", false, this.resetState)}>Main</Button>
        <Button className="mr-2" variant="primary"
          onClick={() => this.handleStateChange(
            "viewType", "browse", false,
            () => this.handleStateChange("current.type", "0"))}>Browse</Button>
        {this.state.viewType === "details"
          && this.state.currentId
          && this.state.current !== "" ? 
          <DeleteConfirmation action={
            () => this.partialApplyWithCallbacks(this.props.mainAPI.deleteById)([this.state.currentId])} /> : ""}
      </Col>
    </>);
  }
  renderIdInput() {
    return (<>
      <FormControl type="text" placeholder="ID..."
        onChange={this.updateCurrentObjectState}
        className="mr-1 col-md-4" value={this.state.currentId} />
    </>);
  }
  renderSearchInput() {
    return (<>
      <FormControl type="text" placeholder="Search"
        className="mr-1 col-md-6" value={this.state.searchInput} />
      <Button variant="outline-success">
        <FontAwesomeIcon icon={faSearch} />
      </Button>
    </>);
  }
  renderTypeDropdown() {
    const possibleTypes = this.getPossibleTypes();
    return (<>
      {possibleTypes && this.state.viewType === "browse" ?
        <Form.Control as="select" value={this.state.current.type} custom defaultValue="0"
          onChange={(e) => this.filterByType(e.currentTarget.value)}>
          <option value="0">&lt;--- choose a type ---&gt;</option>
          {Object.entries(possibleTypes)
            .map(([_, value]) => <option value={value}>{value}</option>)}
        </Form.Control> : ""}
    </>);
  }
  renderTopButtons() {
    return (<>
      <Row className="mx-0 d-flex justify-content-between">
        {this.renderNavigationButtons()}
        <Col className="px-0 d-flex justify-content-end">
          <Form className="d-flex justify-content-between" inline>
            {this.renderTypeDropdown()}
            {this.renderIdInput()}
            {this.renderSearchInput()}
          </Form>
        </Col>
      </Row>
    </>);
  }
  renderActionButtons() {
    return (<>
    <Row className="d-flex justify-content-end mx-0">
      <Col md={9} />
      <Button variant="secondary" onClick={this.resetState}>Reset</Button>
      <Button className="ml-2" onClick={this.handleSubmit}>Save</Button>
    </Row>
    </>);
	}

  render() {
    return (<>
      <Container>
        {this.state.alert ? this.state.alert : ""}
        {this.state.notifications && this.state.notifications.length > 0 ? 
          <ToastWrapper>{this.state.notifications}</ToastWrapper> : ""}
        {this.renderTitle()}
        <br />
        {this.renderTopButtons()}
        <br />
        {this.state.viewType === "browse" ? this.renderListView() : this.renderForm()}
        <br />
        {this.state.viewType === "browse" ? "" : this.renderSubmodules()}
        <br />
        {this.state.viewType === "browse" ? "" : this.renderActionButtons()}
        <br />
      </Container>
      <QuickScrollFab />
    </>);
  }
}