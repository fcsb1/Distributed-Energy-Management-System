import React, { useState } from 'react';
import { withRouter } from 'react-router-dom';
import { useAuth } from './useAuth';
import { login as loginAPI } from './auth-api';
import { Container, Row, Col, Card, CardHeader, CardBody, FormGroup, Label, Input, Button } from 'reactstrap';
import APIResponseErrorMessage from '../commons/errorhandling/api-response-error-message';

function LoginPage(props) { // Receive props
    let auth = useAuth();

    const [error, setError] = useState({ status: 0, errorMessage: null });
    const [formControls, setFormControls] = useState({
        username: { value: '' },
        password: { value: '' }
    });

    // get history and location from props (passed by withRouter)
    let { history, location } = props;
    let { from } = location.state || { from: { pathname: "/" } };

    const handleChange = (event) => {
        const name = event.target.name;
        const value = event.target.value;
        setFormControls(prevControls => ({
            ...prevControls,
            [name]: { value: value }
        }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        let credentials = {
            username: formControls.username.value,
            password: formControls.password.value
        };

        loginAPI(credentials, (result, status, err) => {
            if (result !== null && status === 200) {
                // result.token contains the JWT
                auth.login(result.token);
                history.replace(from);
            } else {
                setError({ status: status, errorMessage: err });
            }
        });
    };

    return (
        <Container>
            <Row>
                <Col sm={{ size: 6, offset: 3 }} style={{ marginTop: '50px' }}>
                    <Card>
                        <CardHeader><strong>Login</strong></CardHeader>
                        <CardBody>
                            <form onSubmit={handleSubmit}>
                                <FormGroup id='username'>
                                    <Label for='usernameField'>Username:</Label>
                                    <Input name='username' id='usernameField'
                                           placeholder='Username...'
                                           onChange={handleChange}
                                           defaultValue={formControls.username.value}
                                           required
                                    />
                                </FormGroup>
                                <FormGroup id='password'>
                                    <Label for='passwordField'>Password:</Label>
                                    <Input name='password' id='passwordField'
                                           type='password'
                                           placeholder='Password...'
                                           onChange={handleChange}
                                           defaultValue={formControls.password.value}
                                           required
                                    />
                                </FormGroup>
                                <Row>
                                    <Col sm={{ size: '4', offset: 8 }}>
                                        <Button type="submit" color="primary">Submit</Button>
                                    </Col>
                                </Row>
                                {error.status > 0 &&
                                 <APIResponseErrorMessage errorStatus={error.status} error={error.errorMessage} />}
                            </form>
                        </CardBody>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
}

export default withRouter(LoginPage);