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
import React from "react";
import PerfectScrollbar from "perfect-scrollbar";

// core components
import axios from "axios";
import IndexNavbar from "components/Navbars/IndexNavbar.js";
import Footer from "components/Footer/Footer.js";
import { 
  Container, 
  ListGroup, 
  ListGroupItem, 
  Card, 
  CardHeader, 
  CardBody,
  Input
} from "reactstrap";

let ps = null;

export default function FriendsPage() {
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
  
  let friendList = []

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
  
  const listItemStyle = {
    color: "black", 
    fontFamily: "nucleo", 
    fontWeight: "bold", 
    fontSize: "20px"
  }

  const searchBarStyle = {
    borderColor: "#Ad2dca",
    borderWidth: "2px",
    color: "white"
  }
  
  const FriendsList = () => {
    if(friendList.length > 0) {
      return (
        <ListGroup>
          {friendList.map(function (item) { 
            return (
              <ListGroupItem style={listItemStyle}>
                <img
                alt="..."
                className="img-fluid rounded-circle shadow"
                src={require("assets/img/ryan.jpg").default}
                style={{ width: "50px", margin: "10px"}}
                />
                {item}
              </ListGroupItem>
            )
          })}
        </ListGroup>
      );
    } else {
      return <p>You currently don't have any friend on Spotify. </p>
    }
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
                  <Input placeholder="Search for a friend" style={searchBarStyle}/>
                </CardHeader>
                <CardBody>
                  <FriendsList />
                </CardBody>
              </Card>
            </Container>
          </div>
          <Footer />
      </div>
    </>
  );
}
