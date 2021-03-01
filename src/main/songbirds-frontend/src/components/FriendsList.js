import React, { Component } from 'react'
import { ListGroup } from 'reactstrap'
import FriendEntry from './FriendEntry'

class FriendsList extends Component {
    createList = () => {
        let list = this.props.names
        if(list.length > 0) {
            return (
                <ListGroup>
                    {this.props.names.map(name => <FriendEntry name={name} />)}
                </ListGroup>
            )
        } else if(this.props.keyword.length == 0) {
            return <p>You currently don't have any friend on Spotify. </p>
        } else {
            return <p>No friend matching current search keyword(s)</p>
        }
    }

    render() {
        return(
            <div>
                {this.createList()}
            </div>
        )
    }
}

export default FriendsList