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
  Jumbotron,
  Row,
  Col,
  Button,
  Spinner
} from "reactstrap";
import FriendsList from "components/FriendsList";
//import globalVar from "views/examples/globalVar.js"

let ps = null;

export default function RecommenderPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [loading, setLoading]= React.useState(window.loading);
  const [recommenderLoading, setRecommenderLoading] = useState(false);
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

  const mySpinner = props => {
   const style = { position: "fixed", top: "50%", left: "50%", transform: "translate(-50%, -50%)" };
    return (
      <div style={style}>
        <Spinner color="primary"/>
      </div>
    );
  };

  const getCurrFriendInput = (e) => {
    setSearchTerm(e.target.value)
  }

  const dynamicSearch = () => {
    return friendList.filter(name => name.toLowerCase().includes(searchTerm.toString().toLowerCase()))
  }

  const recommend = () => {
      let body = []
      selectedUsers.forEach((item) => 
      {
        const friendID = item.value
        body.push(friendID)
      })
      if (body.length == 0) {
        return;
      }
      setRecommenderLoading(true)
      axios.post('/data/recommend', {"friendIDs": body}, {withCredentials: true})
      .then((response) => {
        setRecommenderLoading(false)
        const res = response.data;
        if (res.enoughSongs == true) {
           window.alert('Recommended Playlist created, check the New Playlists window to access it')
        } else {
           window.alert('Recommended Playlist created, check the New Playlists window to access it. \nDue to your limited number of songs in your playlists, the recommendation process may not be as effective. Please add more songs to get a more effective playlist recommendation.')
        }

        console.log(response)  
      })
      .catch((error) => {
        console.log(error)
      })
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

  const jumbotronStyle = {
      borderColor: "#Ad2dca",
      borderWidth: "2px",
      color: "blue",
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

  const onButtonClickHandler = () => 
  {
    window.alert('Creating new playlist...')
  }

  return (
    <>
      <IndexNavbar />
      <div className="wrapper">
          <div className="page-header1">
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
            {!loading && <Container className="align-items-start" style={{marginBottom:'60'}}>
                                    <Card className="card-coin card-plain">
                                      <CardBody>
                                          <h4 className="title">Pick friends to get recommendations from</h4>
                                      </CardBody>
                                    </Card>
                                  </Container>}
            {!loading &&
            <Container className="align-items-center">
              <Card color="default">
                <CardHeader>
                  <CardBody>
                     <p>Choose one or more of your friends using the search bar and hit "Recommend" to create playlists catered to you based on their interests!</p>
                  </CardBody>
                  <Container fluid>
                    <Row>
                      <Col xs={9} md={10}>
                        <AsyncSelect
                          placeholder="Search friend(s) to get recs"
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
                          onClick={() => 
                            {
                              recommend();
                            }}
                        > Recommend
                        </Button>
                      </Col>
                    </Row>
                  </Container>
                </CardHeader>
                <CardBody>
                                  <p>   Feel free to mix and match multiple friends to try different playlist combinations.</p>
                </CardBody>
              </Card>
            </Container>}
            {recommenderLoading && <div style= {{ position: "fixed", top: "50%", left: "50%", transform: "translate(-50%, -50%)" }}>
                                           <Spinner color="primary"/>
                                         </div>}
            {loading && <Card className="card-coin card-plain"> <Container className="align-items-center">
                              <CardHeader>
                                <h1 className="display-3" color="primary">The recommendation model is not yet finished training yet, please come back or reload in a few minutes!</h1>
                              </CardHeader>
                            </Container> </Card>}
          </div>
      </div>
    </>
  );
}
