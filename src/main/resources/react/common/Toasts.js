import { useState } from "react";
import { Toast } from "react-bootstrap";

export function CustomToast({ header, children, timeout, onClick }) {
  const [show, changeShow] = useState(true);

  return show ? (
      <Toast show={show} delay={timeout} onClick={onClick}
            onClose={() => changeShow(false)} autohide={true}
            style={{
              zIndex: "9999",
              background: "white"
            }}>
        <Toast.Header>{header}</Toast.Header>
        <Toast.Body>{children}</Toast.Body>
      </Toast>) : "";
}

export function ToastWrapper({ children }) {
  return (
    <div
      aria-live="polite"
      aria-atomic="true"
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: "100vw",
        margin: 'auto',
        zIndex: 9999
      }}>
      <div
        style={{
          position: 'fixed',
          top: "5%",
          right: "5%",
        }}
      >
        {children}
      </div>
    </div>);
}