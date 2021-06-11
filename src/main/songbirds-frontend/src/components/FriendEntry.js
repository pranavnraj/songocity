import React, { Component } from 'react'
import { Button, ListGroupItem } from 'reactstrap'
import axios from "axios";

const listItemStyle = {
    color: "black", 
    fontFamily: "nucleo", 
    fontWeight: "bold", 
    fontSize: "20px"
}

class FriendEntry extends Component {

    constructor() {
        super()
        this.state = {
            profilePicture: "src/assets/img/ryan.jpg",
        }
    }

    getProfilePicture = () => {
        let request = '/data/profile_picture?friend=' + this.props.name
        axios.get(request, {withCredentials: true}).then((response) => {
            this.setState({profilePicture: response.data.profile_pic})
        }).catch((error) => {
            console.log(error.response);
        })
    }
    
    componentDidMount() {
        this.getProfilePicture()
    }

    render() {
        return (
            <ListGroupItem style={listItemStyle}>
                <img
                alt="..."
                className="img-fluid rounded-circle shadow"
                src={this.state.profilePicture}
                style={{ width: "50px", margin: "10px"}}
                />
                {this.props.name}
                <div style={{float: "right", margin: "10px"}}>
                    <Button 
                        color="danger" 
                        onClick={this.props.deleteFriend(this.props.name)}>
                            Delete
                    </Button>
                </div>
            </ListGroupItem>
        )
    }
}

export default FriendEntry