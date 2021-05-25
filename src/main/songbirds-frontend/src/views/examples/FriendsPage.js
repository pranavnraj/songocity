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
import FriendsList from "components/FriendsList";

let ps = null;

export default function FriendsPage() {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedUsers, setSelectedUsers] = useState([])
  const currFriends = useRef(null)
  const [friendList, setFriendList] = useState([]) // store a list of current friends

  const getFriendsList = () => {
    // Get a user's current friends
    return axios.get('/data/get_friend_list', {withCredentials: true});
  }

  const queryAllUsers = (query) => {
    // Get all users that fit the query
    const httpStr = '/data/query_friend?id_query=' + query
    return axios.get(httpStr, {withCredentials: true})
  }

  const populateFriendsList = () => {
    getFriendsList().then((response) => {
      const res = response.data;
      setFriendList(res.friends);
    })
    .catch((error) => {
      console.log(error.response);
    });
  }

  const addFriends = () => {
    let ids = []
    selectedUsers.forEach(selection => {
       const friendID = selection.value
       ids.push(friendID)
    })

    axios.post('/data/add_friend', {
      "friendIDs": ids,
    }, {withCredentials: true})
    .then((response) => {
      console.log(response)
      getFriendsList().then((res) => {
        //friendList = res.data.friends
        setFriendList(res.data.friends)
        currFriends.current.names = friendList
        console.log(currFriends.current.names)
      })
      .catch((err) => {
        console.log(err)
      })  
    })
    .catch((error) => {
      console.log(error)
    })

  }

  const deleteFriend = (friend) => () => {
    axios.delete(
        '/data/remove_friend?friend=' + friend, 
        {withCredentials: true}
    ).then((response) => {
      console.log(response)
      getFriendsList().then((res) => {
        setFriendList(res.data.friends)
        currFriends.current.names = friendList
        console.log(currFriends.current.names)
      })
      .catch((err) => {
        console.log(err)
      })
    }).catch((error) => {
        console.log(error)
    })
  }

  const loadOptions = (inputValue, callback) => {
    // Async load list of users that fit the query
    setTimeout(() => {
      queryAllUsers(inputValue).then((response) => {
        if(!inputValue) {
          return callback([])
        }
        let friends = response.data.queries
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
    populateFriendsList();
    console.log(friendList)

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
                  <h4 className="title">Friend List</h4>
                  <CardBody>
                    <p>Start adding people as friends who are already on Songbirds. After you have added your friends, go to the Recommender page to create playlists for yourself based on their taste.</p>
                  </CardBody>
                  <Container fluid>
                    <Row>
                      <Col xs={9} md={10}>
                        <AsyncSelect
                          placeholder="Search names to add as friends"
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
                          color="danger" 
                          type="button"
                          size="sm"
                          onClick={addFriends}
                        > Add to Friends
                        </Button>
                      </Col>
                    </Row>
                  </Container>
                </CardHeader>
                <CardBody>
                  <FriendsList
                    ref={currFriends} 
                    names={friendList} 
                    deleteFriend={deleteFriend}
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
