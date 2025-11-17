import React from 'react';
import { Button, Container, Jumbotron } from 'reactstrap';

import BackgroundImg from '../commons/images/blue.png';

const backgroundStyle = {
    backgroundPosition: 'center',
    backgroundSize: 'cover',
    backgroundRepeat: 'no-repeat',
    width: "100%",
    height: "1920px",
    backgroundImage: `url(${BackgroundImg})`
};
const textStyle = { color: 'white', };

function Home() {
    return (
        <div>
            <Jumbotron fluid style={backgroundStyle}>
                <Container fluid>

                    <h1 className="display-3" style={textStyle}>Energy Management System</h1>

                </Container>
            </Jumbotron>
        </div>
    );
}


export default Home;