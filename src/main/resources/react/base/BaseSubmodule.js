import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons';
import { Button } from "react-bootstrap";
import BaseForm from "./BaseForm";

export default class BaseSubmodule extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      expanded: false // default: collapsed
    };

    this.handleExpand = this.handleExpand.bind(this);
    this.renderModule = this.renderModule.bind(this);
    this.renderExpandButton = this.renderExpandButton.bind(this);
  }

  handleExpand() {
    const expanded = this.state.expanded;
    this.setState({
      expanded: !expanded
    });
  }

  renderExpandButton() {
    return (<>
      <Button variant={this.props.compact ? "outline-secondary" : "success"}
        className={this.props.compact ? "" : "mr-1"} onClick={this.handleExpand}>
      {this.props.compact === true ? "" : <>{this.props.title}</>}
      <FontAwesomeIcon className={this.props.compact === true ? "ml-0" : "ml-1"}
        icon={this.state.expanded === true ? faChevronUp : faChevronDown} />
    </Button>
    </>);
  }

  renderModule(props) {

  }

  render() {
    return (<>
      {this.renderExpandButton()}
      {this.state.expanded ?
        this.renderModule(this.props)
        : ""}
    </>);
  }
}