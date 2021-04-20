/*!

=========================================================
* BLK Design System React - v1.2.0
=========================================================

* Product Page: https://www.creative-tim.com/product/blk-design-system-react
* Copyright 2020 Creative Tim (https://www.creative-tim.com)
* Licensed under MIT (https://github.com/creativetimofficial/blk-design-system-react/blob/main/LICENSE.md)

* Coded by Creative Tim

=========================================================

* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

*/
import React, { useContext, useState } from "react";
import AppContext from "../AppContext";
import axios from 'axios';
import { Link, useHistory } from "react-router-dom";
// reactstrap components
import {
  Button,
  Collapse,
  DropdownToggle,
  DropdownMenu,
  DropdownItem,
  UncontrolledDropdown,
  NavbarBrand,
  Navbar,
  NavItem,
  NavLink,
  Nav,
  Container,
  Row,
  Col,
  UncontrolledTooltip,
} from "reactstrap";

export default function IndexNavbar() {
  const [collapseOpen, setCollapseOpen] = React.useState(false);
  const [collapseOut, setCollapseOut] = React.useState("");
  const [color, setColor] = React.useState("navbar-transparent");
  const history = useHistory();
  const context = useContext(AppContext);
  const clientID = "3cceff6ff3144834b845505bcfab9cd7";
  const redirectURI = "http://localhost:5000/callback/";
  //const redirectURI = "http://songbirds-dev.us-west-1.elasticbeanstalk.com/callback/";
  React.useEffect(() => {
    ping().then((response) => {
            if(response.status == 200){
              context.setAuthText("Log out");
              context.setDisplay(true);
            }
            else{
              context.setAuthText("Log in");
              context.setDisplay(false);
            }
          }).catch((error) => {
            console.log(error.response);
          });
    window.addEventListener("scroll", changeColor);
    return function cleanup() {
      window.removeEventListener("scroll", changeColor);
    };
  },[]);
  const changeColor = () => {
    if (
      document.documentElement.scrollTop > 99 ||
      document.body.scrollTop > 99
    ) {
      setColor("bg-info");
    } else if (
      document.documentElement.scrollTop < 100 ||
      document.body.scrollTop < 100
    ) {
      setColor("navbar-transparent");
    }
  };
  const toggleCollapse = () => {
    document.documentElement.classList.toggle("nav-open");
    setCollapseOpen(!collapseOpen);
  };
  const onCollapseExiting = () => {
    setCollapseOut("collapsing-out");
  };
  const onCollapseExited = () => {
    setCollapseOut("");
  };
  const scrollToDownload = () => {
    document
      .getElementById("download-section")
      .scrollIntoView({ behavior: "smooth" });
  };
  const getSpotifyLogin = (csrfStateValue) => {
    return axios.get('/login', { params: { state: csrfStateValue } }, {withCredentials: true});
  }
  const primeLogin = (csrfStateValue) => {
    return axios.get('/prime_login', { params: { state: csrfStateValue } }, {withCredentials: true});
  }
  const logout = () => {
    return axios.get('/logout', {withCredentials: true});
  }
  const train = () => {
      return axios.get('/data/train', {withCredentials: true});
  }
  const recommend = () => {
      return axios.get('/data/recommend', {withCredentials: true});
  }
  const ping = () => {
      return axios.get('/ping', {withCredentials: true});
  }
  const updateLoginHomepage = () => {
    if(context.authBtnText == "Log in") {
      var csrfStateValue = Math.random().toString(36).slice(2);
      primeLogin(csrfStateValue).then((response1) => {
              window.open("https://accounts.spotify.com/authorize?client_id=" + clientID + "&response_type=code&redirect_uri="
              + redirectURI + "&scope=user-top-read%20user-read-recently-played%20user-read-email%20playlist-modify-public%20playlist-modify-private%20playlist-read-private%20playlist-read-collaborative&state=" + csrfStateValue)
              getSpotifyLogin(csrfStateValue).then((response2) => {
                context.setAuthText("Log out");
                context.setDisplay(true);
              })
              .catch((error) => {
                console.log(error.response);
              });
      })
      .catch((error) => {
        console.log(error.response);
      });
    } else {
      logout().then((response) => {
        context.setAuthText("Log in");
        context.setDisplay(false);
        history.push("/components");
      })
      .catch((error) => {
        console.log(error.response);
      });
    }
  }
  const LoggedInButtons = () => (
    <Nav navbar>
      <NavItem>
        <Button 
          className="btn-link" 
          color="success"
          onClick={() => history.push("/friends")}
        >
          Friends
        </Button>
      </NavItem>
      <NavItem>
        <Button className="btn-link" color="warning">
          Recommender
        </Button>
      </NavItem>
      <NavItem>
        <Button className="btn-link" color="danger">
          New Playlists
        </Button>
      </NavItem>
      <NavItem>
         <Button
                onClick={train}
         >
                Train
         </Button>
      </NavItem>
      <NavItem>
        <Button
          className="nav-link d-none d-lg-block"
          color="primary"
          target="_blank"
          onClick={() => history.push("/profile-page")}
        > 
        <i className="tim-icons icon-single-02" /> Profile
        </Button>
      </NavItem>
    </Nav>
  );
  return (
    <Navbar className={"fixed-top " + color} color-on-scroll="100" expand="lg">
      <Container>
        <div className="navbar-translate">
          <NavbarBrand to="/" tag={Link} id="navbar-brand">
          <strong>Songbirds</strong>
          </NavbarBrand>
          <UncontrolledTooltip placement="bottom" target="navbar-brand" onClick={()=>history.push("/components")}>
            <strong>Home Page</strong>
          </UncontrolledTooltip>
          <button
            aria-expanded={collapseOpen}
            className="navbar-toggler navbar-toggler"
            onClick={toggleCollapse}
          >
            <span className="navbar-toggler-bar bar1" />
            <span className="navbar-toggler-bar bar2" />
            <span className="navbar-toggler-bar bar3" />
          </button>
        </div>
        <Collapse
          className={"justify-content-end " + collapseOut}
          navbar
          isOpen={collapseOpen}
          onExiting={onCollapseExiting}
          onExited={onCollapseExited}
        >
          <div className="navbar-collapse-header">
            <Row>
              <Col className="collapse-brand" xs="6">
                <a href="#pablo" onClick={(e) => e.preventDefault()}>
                  BLK•React
                </a>
              </Col>
              <Col className="collapse-close text-right" xs="6">
                <button
                  aria-expanded={collapseOpen}
                  className="navbar-toggler"
                  onClick={toggleCollapse}
                >
                  <i className="tim-icons icon-simple-remove" />
                </button>
              </Col>
            </Row>
          </div>
          <Nav navbar>
            {/* <UncontrolledDropdown nav>
              <DropdownToggle
                caret
                color="default"
                data-toggle="dropdown"
                href="#pablo"
                nav
                onClick={(e) => e.preventDefault()}
              >
                <i className="fa fa-cogs d-lg-none d-xl-none" />
                Getting started
              </DropdownToggle>
              <DropdownMenu className="dropdown-with-icons">
                <DropdownItem href="https://demos.creative-tim.com/blk-design-system-react/#/documentation/overview">
                  <i className="tim-icons icon-paper" />
                  Documentation
                </DropdownItem>
                <DropdownItem tag={Link} to="/register-page">
                  <i className="tim-icons icon-bullet-list-67" />
                  Register Page
                </DropdownItem>
                <DropdownItem tag={Link} to="/landing-page">
                  <i className="tim-icons icon-image-02" />
                  Landing Page
                </DropdownItem>
                <DropdownItem tag={Link} to="/profile-page">
                  <i className="tim-icons icon-single-02" />
                  Profile Page
                </DropdownItem>
              </DropdownMenu>
            </UncontrolledDropdown> */}
            { context.displayOptions ? <LoggedInButtons /> : null }
            <NavItem>
              <Button
                className="nav-link d-none d-lg-block"
                color="primary"
                onClick={updateLoginHomepage}
              >
                 {context.authBtnText}
              </Button>
            </NavItem>
          </Nav>
        </Collapse>
      </Container>
    </Navbar>
  );
}
