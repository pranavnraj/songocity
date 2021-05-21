import React, { Component } from 'react'
import { ListGroup } from 'reactstrap'

import FriendEntry from './FriendEntry'

class FriendsList extends Component {
    createList = () => {
        let list = this.props.names
        if(list.length > 0) {
            return (
                <ListGroup>
                    {this.props.names.map(name => 
                        <FriendEntry 
                            name={name} 
                            deleteFriend={this.props.deleteFriend}
                        />
                    )}
                </ListGroup>
            )
        } else {
            return <p>You currently don't have any friends on Songbirds. </p>
        }
    }

    arrayEquals = (a, b) =>
        a.length === b.length &&
        a.every((v, i) => v === b[i])

    componentDidUpdate(prevProps) {
        console.log("component updated")
        if(!this.arrayEquals(prevProps.names, this.props.names)) {
            console.log("arrays not equal")
            this.forceUpdate()
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