import React, { Component } from 'react'
import { Button, ListGroupItem } from 'reactstrap'

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