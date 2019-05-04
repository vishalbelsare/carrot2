import PropTypes from "prop-types";
import React from "react";

import "./LightDarkSwitch.css";

export function LightDarkSwitch(props) {
  return (
    <React.Fragment>
      <input className="tgl tgl-ios" id="theme" type="checkbox" tabIndex="0"
             checked={props.dark} onChange={props.onChange} />
      <label className="tgl-btn" htmlFor="theme"
             title={`Switch to ${props.dark ? 'light' : 'dark'} theme`} />
    </React.Fragment>
  );
}

LightDarkSwitch.propTypes = {
  dark: PropTypes.bool.isRequired,
  onChange: PropTypes.func.isRequired
};