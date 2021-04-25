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
  Input,
  Row,
  Col,
  Button
} from "reactstrap";
import FriendsList from "components/FriendsList";

let ps = null;

export default function RecommenderPage() {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedUsers, setSelectedUsers] = useState([])
  const currFriends = useRef(null)
  let friendList = [] 

  const getFriendsList = () => {
    return axios.get('/data/get_friend_list', {withCredentials: true});
  }

  const populateFriendsList = () => {
    getFriendsList().then((response) => {
      const res = response.data;
      return res.selectedUsers;
    })
    .catch((error) => {
      console.log(error.response);
    });
  }

  const getCurrFriendInput = (e) => {
    setSearchTerm(e.target.value)
  }

  const dynamicSearch = () => {
    return friendList.filter(name => name.toLowerCase().includes(searchTerm.toString().toLowerCase()))
  }

  const recommend = () => {
    axios.post("/data/recommend", {"friendIDs": friendList},
    {withCredentials: true}).then(response => {
      if(response.status == 200)
      {
        return axios.get("/data/recommend", {withCredentials: true});
      }
    }).catch(error => {
      console.log(error);
    });
  }

  const loadOptions = (inputValue, callback) => {
    setTimeout(() => {
      getFriendsList().then((response) => {
        if(response.status != 200) {
          return callback([])
        }
        let friends = response.data.friends
        if(!friends) {
          return callback([])
        }
        friends = friends.map(id => ({
          value: id, 
          label: id
        }))
        callback(friends)
      })
      .catch((error) => {
        console.log(error.response);
        callback([])
      });
    }, 1000)
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

  const selectStyle = {
    option: provided => ({
      ...provided,
      color: "black"
    }),
    control: provided => ({
      ...provided,
      color: "black",
      width: "100%",
    }),
    singleValue: provided => ({
      ...provided,
      color: "black"
    }),
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
                  <h4 className="title">Recommender List</h4>
                  <Container fluid>
                    <Row>
                      <Col xs={9} md={10}>
                        <AsyncSelect
                          placeholder="Enter friend to get recs from"
                          isMulti
                          cacheOptions
                          loadOptions={loadOptions}
                          defaultOptions
                          onChange={setSelectedUsers}
                          styles={selectStyle}
                          theme={theme => ({
                            ...theme,
                            borderRadius: 5,
                            colors: {
                              ...theme.colors,
                              primary: "#Ad2dca", 
                            }
                          })}
                        />
                      </Col>
                      <Col xs={3} md={2}>
                        <Button 
                          className="btn-round" 
                          color="warning" 
                          type="button"
                          size="sm"
                          onClick={recommend}
                        > Recommend
                        </Button>
                      </Col>
                    </Row>
                  </Container>
                  <Input 
                    type="text"
                    placeholder="Search for a current friend based on username" 
                    style={searchBarStyle}
                    value={searchTerm}
                    onChange={getCurrFriendInput}
                  />
                </CardHeader>
                <CardBody>
                  <FriendsList
                    ref={currFriends} 
                    names={dynamicSearch()} 
                    keyword={searchTerm}
                  />
                </CardBody>
              </Card>
            </Container>
          </div>
          <Footer />
      </div>
    </>
  );
}
