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
import React, { useState, useRef }  from "react";
import PerfectScrollbar from "perfect-scrollbar";

// core components
import axios from "axios";
import AsyncSelect from 'react-select/async';
import IndexNavbar from "components/Navbars/IndexNavbar.js";
import Footer from "components/Footer/Footer.js";
import { 
  Container,  
  Card, 
  CardHeader, 
  CardBody,
  Row,
  Col,
  Button
} from "reactstrap";

export default function AboutUsPage() 
{
    return 
    (
        <>
        <IndexNavbar />
        <div className="wrapper">
            <div className="page-header">
                <img
                    alt="..."
                    className="dots"
                    src={require("assets/img/dots.png").default}
                />
                <img
                    alt="..."
                    className="path"
                    src={require("assets/img/path4.png").default}
                />
                <Container className="align-items-center">
                    <h4 className="title">About Us</h4>
                    <div className="row">
                    <img src={require("assets/img/Pranav.JPG").default}/>
                    <h5>Pranav Narasimmaraj, Founder and Software Architect/Developer</h5>
                    <p>I'm a passionate Software Engineer who loves to build projects up from scratch. 
                        I love solving problems involving cloud computing, distributed systems, and security.
                        This project was a great opportunity to express my skills in all the above areas as well as build a
                        useful song recommender for a popular app such as Spotify.</p>
                    </div>
                    <div className="row">
                    <h5>Qiyue (Cheery) Wang, Software Developer</h5>
                    <p> I'm a graduate student studying computer science at UCSD.
                        I had little knowledge on React before this project, but by working on the front-end side of this project,
                        I learned a lot more about react and I'm happy for it.
                    </p>
                    </div>
                    <div className="row">
                    <h5>Prateek Narayanan, Software Developer</h5>
                    <p>I'm an aspiring software engineer with an interest in full-stack development.
                        I enjoy working on software projects, especially on products that people would be able to use.
                        This project was a great opportunity for me to apply my skills in the React framework,
                        as well as work on a song recommender for an app like Spotify.
                    </p>
                    </div>
                </Container>
            </div>
        </div>
        </>
    );
}