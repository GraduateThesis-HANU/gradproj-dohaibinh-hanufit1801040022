import React, { useState } from "react";
import { Button, Modal } from "react-bootstrap";

export default function DeleteConfirmation(props) {
  const [show, setShow] = useState(false);
  const [hasModal, setHasModal] = useState(false);

  const handleClose = () => setShow(false);
  const handleShow = () => {
    setShow(true);
    setHasModal(true);
  };

  const onSuccess = result => {
    handleClose();
  };
  const onFailure = err => {
    handleClose();
  };
  const onAction = () => {
    try {
      onSuccess(props.action());
    } catch (err) {
      onFailure(err);
    }
  };

  return (
    <>
      <Button variant="danger" onClick={handleShow}>Delete</Button>
      {hasModal === true ? 
      <Modal show={show} onHide={handleClose} onExited={() => setHasModal(false)}>
        <Modal.Header closeButton={true}>
          <Modal.Title>Confirm delete</Modal.Title>
        </Modal.Header>
        
        <Modal.Body>Delete this resource?</Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" size="sm" onClick={handleClose}>
            Cancel
          </Button>
          <Button variant="danger" size="sm" onClick={onAction}>
            Confirm
          </Button>
        </Modal.Footer>
      </Modal>
      : null}
    </>
  );
};