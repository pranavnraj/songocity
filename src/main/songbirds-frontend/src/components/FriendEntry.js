import React, { Component } from 'react'
import { ListGroupItem } from 'reactstrap'

const listItemStyle = {
    color: "black", 
    fontFamily: "nucleo", 
    fontWeight: "bold", 
    fontSize: "20px"
}

class FriendEntry extends Component {
    render() {
        return (
            <ListGroupItem style={listItemStyle}>
                <img
                alt="..."
                className="img-fluid rounded-circle shadow"
                src={require("assets/img/ryan.jpg").default}
                style={{ width: "50px", margin: "10px"}}
                />
                {this.props.name}
            </ListGroupItem>
        )
    }
}

export default FriendEntry