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
import React, { useState }  from "react";
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
  Input
} from "reactstrap";
import FriendsList from "components/FriendsList";

let ps = null;

export default function FriendsPage() {
  const [searchTerm, setSearchTerm] = useState('')
  let friendList = []

  const getFriendsList = () => {
    return axios.get('http://localhost:8888/data/get_friend_list', {withCredentials: true});
  }

  const populateFriendsList = () => {
    getFriendsList().then((response) => {
      const res = response.data;
      return res.friends;
    })
    .catch((error) => {
      console.log(error.response);
    });
  }

  const getInput = (e) => {
    setSearchTerm(e.target.value)
  }

  const dynamicSearch = () => {
    return friendList.filter(name => name.toLowerCase().includes(searchTerm.toString().toLowerCase()))
  }

  React.useEffect(() => {
    if (navigator.platform.indexOf("Win") > -1) {
      document.documentElement.className += " perfect-scrollbar-on";
      document.documentElement.classList.remove("perfect-scrollbar-off");
      let tables = document.querySelectorAll(".table-responsive");
      for (let i = 0; i < tables.length; i++) {
        ps = new PerfectScrollbar(tables[i]);
      }
    }
    document.body.classList.toggle("profile-page");
    friendList = populateFriendsList();

    // Specify how to clean up after this effect:
    return function cleanup() {
      if (navigator.platform.indexOf("Win") > -1) {
        ps.destroy();
        document.documentElement.className += " perfect-scrollbar-off";
        document.documentElement.classList.remove("perfect-scrollbar-on");
      }
      document.body.classList.toggle("profile-page");
    };
  },[]);

  const searchBarStyle = {
    borderColor: "#Ad2dca",
    borderWidth: "2px",
    color: "white",
    margin: 10
  }

  return (
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
              <Card color="default">
                <CardHeader>
                  <h4 className="title">Friends List</h4>
                  <AsyncSelect
                    isMulti
                    cacheOptions
                    defaultOptions
                  />
                  <Input 
                    type="text"
                    placeholder="Search for a current friend" 
                    style={searchBarStyle}
                    value={searchTerm}
                    onChange={getInput}
                  />
                </CardHeader>
                <CardBody>
                  <FriendsList names={dynamicSearch()} keyword={searchTerm}/>
                </CardBody>
              </Card>
            </Container>
          </div>
          <Footer />
      </div>
    </>
  );
}
