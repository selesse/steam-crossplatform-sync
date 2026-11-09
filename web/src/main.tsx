import "temporal-polyfill/global";
import React from "react";
import ReactDOM from "react-dom/client";
import Routes from "./Routes";
import "./index.css";

const root = document.getElementById("root");
if (!root) {
  throw "Root element not found";
}
ReactDOM.createRoot(root).render(
  <React.StrictMode>
    <Routes />
  </React.StrictMode>,
);
