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
  CardHeader,
  CardBody,
  Input,
  Row,
  Col,
  Button
} from "reactstrap";
import FriendsList from "components/FriendsList";
import { Accordion, Card } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";

let ps = null;



export default function PlaylistsPage() {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedUsers, setSelectedUsers] = useState([])
  const currFriends = useRef(null)
  const [songDisplay, setSongDisplay] = useState([])


  const tabs = [
   { id: 2, label: "Tab 2", description: ["hello", <br/>, "there"] },
   { id: 3, label: "Tab 3", description: ["hello", "there"] }
  ];

  let total_songs = {};

  const getPlaylistList = () => {
    // Get a user's current playlist songs
    return axios.get('/data/get_playlist_list', {withCredentials: true});
  }

  const populatePlaylistList = () => {
        getPlaylistList().then((response) => {

          var allPlaylists = [];
          const res = response.data;

          for (var key in res) {

            var playlistEntry = {};
            var title = res[key]['title'];
            var songs = [];

            console.log(res[key]['tracks'])

            for (const song of res[key]['tracks']) {
                songs.push(song);
                //console.log(song)
                songs.push(<br/>);
            }
            playlistEntry["title"] = title;
            playlistEntry["songs"] = songs;
            allPlaylists.push(playlistEntry);

          }
          //console.log(allPlaylists)
          //console.log(response.data)
          setSongDisplay(allPlaylists)
        })
        .catch((error) => {
          console.log(error.response);
        });
    }

  const populateTotalSongs = () => {
        console.log("Break")
        //for (const key in songList) {
        //    console.log(key)
        //}
  }

  /*
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
  }*/

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

    populatePlaylistList();
    //console.log(songList);


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
            <Container className="align-items-start">
              <Card className="card-coin card-plain">
                <CardBody>
                    <h4 className="title">Here are your playlists generated by the Recommender. These playlists are also automatically created in your Spotify account and can be accessed like any other playlist through the app.</h4>
                </CardBody>
              </Card>
              {songDisplay.map(tab => (
                      <Accordion key={tab.title} defaultActiveKey={tab.title}>
                        <Card>
                          <Card.Header>
                            <Accordion.Toggle as={Button} variant="link" eventKey={tab.title}>
                              {tab.title}
                            </Accordion.Toggle>
                          </Card.Header>
                          <Accordion.Collapse eventKey={tab.title}>
                            <Card.Body>{tab.songs}</Card.Body>
                          </Accordion.Collapse>
                        </Card>
                      </Accordion>
                    ))}
            </Container>
          </div>
          <Footer />
      </div>
    </>
  );
}
