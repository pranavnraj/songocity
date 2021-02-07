import React, { useState } from "react";
import { BrowserRouter, Route, Switch, Redirect } from "react-router-dom";

import "assets/css/nucleo-icons.css";
import "assets/scss/blk-design-system-react.scss?v=1.2.0";
import "assets/demo/demo.css";

import AppContext from "components/AppContext";
import Index from "views/Index.js";
import LandingPage from "views/examples/LandingPage.js";
import RegisterPage from "views/examples/RegisterPage.js";
import SignInPage from "views/examples/SignInPage.js";
import ProfilePage from "views/examples/ProfilePage.js";
import Homepage from "views/examples/Homepage.js";

export default function App () {
  const [authBtnText, setAuthText] = useState("Log in");
  const [displayOptions, setDisplay] = useState(false);

  const loginDisplay = {
    authBtnText: authBtnText,
    displayOptions: displayOptions,
    setAuthText,
    setDisplay,
  }

  return (
    <AppContext.Provider value={loginDisplay}>
      <BrowserRouter>
        <Switch>
          <Route exact path="/components" render={(props) => <Index {...props} />} />
          <Route
            path="/landing-page"
            render={(props) => <LandingPage {...props} />}
          />
          <Route
            path="/register-page"
            render={(props) => <RegisterPage {...props} />}
          />
          <Route
            path="/sign-in-page"
            render={(props) => <SignInPage {...props} />}
          />
          <Route
            path="/profile-page"
            render={(props) => <ProfilePage {...props} />}
          />
          <Route
            path="/homepage"
            render={(props) => <Homepage {...props}/>}
            />
          <Redirect from="/" to="/components" />
        </Switch>
      </BrowserRouter>
    </AppContext.Provider>
  );
}