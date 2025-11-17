import React, { useState, useEffect } from 'react';
import { Col, Row, Button, FormGroup, Input, Label } from 'reactstrap';
import APIResponseErrorMessage from "../../commons/errorhandling/api-response-error-message";
import { postUser, updateUser } from "../api/User-api";

function UserForm(props) {
    const [error, setError] = useState({ status: 0, errorMessage: null });

    const [formControls, setFormControls] = useState({
        id: { value: props.selectedUser ? props.selectedUser.id : '' },
        username: { value: props.selectedUser ? props.selectedUser.username : '' },
        password: { value: '' }, // blank for security
        role: { value: (props.selectedUser && props.selectedUser.role) || 'ROLE_CLIENT' },
    });

    const isEdit = !!props.selectedUser;

    function handleChange(event) {
        let name = event.target.name;
        let value = event.target.value;
        setFormControls(prevControls => ({
            ...prevControls,
            [name]: { ...prevControls[name], value: value }
        }));
    }

    function handleSubmit() {
        let user = {
            username: formControls.username.value,
            password: formControls.password.value,
            role: formControls.role.value
        };

        // Add ID only if it's an update
        if (isEdit) {
            user.id = formControls.id.value;
        }

        const apiCall = isEdit ? updateUser(user.id, user, handleApiResponse) : postUser(user, handleApiResponse);
    }

    const handleApiResponse = (result, status, err) => {
        if (result !== null && (status === 200 || status === 201)) {
            props.reloadHandler();
        } else {
            setError({ status: status, errorMessage: err });
        }
    };

    return (
        <div>
            <FormGroup id='username'>
                <Label for='usernameField'>Username:</Label>
                <Input name='username' id='usernameField'
                       onChange={handleChange}
                       defaultValue={formControls.username.value}
                       required
                />
            </FormGroup>

            <FormGroup id='password'>
                <Label for='passwordField'>Password:</Label>
                <Input name='password' id='passwordField' type="password"
                       placeholder={isEdit ? "Leave blank to keep unchanged" : "Password..."}
                       onChange={handleChange}
                       required={!isEdit} // Password required only on create
                />
            </FormGroup>

            <FormGroup id='role'>
                <Label for='roleField'>Role:</Label>
                <Input name='role' id='roleField' type="select"
                       onChange={handleChange}
                       defaultValue={formControls.role.value}
                       required
                >
                    <option value="ROLE_CLIENT">Client</option>
                    <option value="ROLE_ADMIN">Admin</option>
                </Input>
            </FormGroup>

            <Row>
                <Col sm={{ size: '4', offset: 8 }}>
                    <Button type={"submit"} onClick={handleSubmit} color="primary">Submit</Button>
                </Col>
            </Row>

            {error.status > 0 &&
             <APIResponseErrorMessage errorStatus={error.status} error={error.errorMessage} />}
        </div>
    );
}

export default UserForm;