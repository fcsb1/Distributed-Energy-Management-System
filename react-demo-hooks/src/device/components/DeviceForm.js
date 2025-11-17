import React, { useState } from 'react';
import { Col, Row, Button, FormGroup, Input, Label } from 'reactstrap';
import APIResponseErrorMessage from "../../commons/errorhandling/api-response-error-message";
import { postDevice, updateDevice } from "../api/device-api";

function DeviceForm(props) {
    const [error, setError] = useState({ status: 0, errorMessage: null });

    const [formControls, setFormControls] = useState({
        id: { value: props.selectedDevice ? props.selectedDevice.id : '' },
        name: { value: props.selectedDevice ? props.selectedDevice.name : '' },
        maxConsumption: { value: props.selectedDevice ? props.selectedDevice.maxConsumption : '' },
        userId: { value: props.selectedDevice ? props.selectedDevice.userId : null }, // This is the FK
    });

    const isEdit = !!props.selectedDevice;

    function handleChange(event) {
        let name = event.target.name;
        let value = event.target.value;

        // Handle 'Unassigned' case
        if (name === 'userId' && value === "Unassigned") {
            value = null;
        }

        setFormControls(prevControls => ({
            ...prevControls,
            [name]: { ...prevControls[name], value: value }
        }));
    }

    function handleSubmit() {
        let device = {
            name: formControls.name.value,
            maxConsumption: parseFloat(formControls.maxConsumption.value), // Ensure it's a number
            userId: formControls.userId.value || null // Send null if unassigned
        };

        const deviceId = formControls.id.value;
        const apiCall = isEdit ? updateDevice(deviceId, device, handleApiResponse) : postDevice(device, handleApiResponse);
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
            <FormGroup id='name'>
                <Label for='nameField'>Device Name:</Label>
                <Input name='name' id='nameField'
                       onChange={handleChange}
                       defaultValue={formControls.name.value}
                       required
                />
            </FormGroup>

            <FormGroup id='maxConsumption'>
                <Label for='maxConsumptionField'>Max Consumption (kW):</Label>
                <Input name='maxConsumption' id='maxConsumptionField' type="number"
                       onChange={handleChange}
                       defaultValue={formControls.maxConsumption.value}
                       required
                />
            </FormGroup>

            <FormGroup id='userId'>
                <Label for='userIdField'>Assign to User:</Label>
                <Input name='userId' id='userIdField' type="select"
                       onChange={handleChange}
                       value={formControls.userId.value || "Unassigned"} // Handle null value
                >
                    <option value="Unassigned">Unassigned</option>
                    {props.users.map(user => (
                        <option key={user.id} value={user.id}>
                            {user.username} (ID: {user.id})
                        </option>
                    ))}
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

export default DeviceForm;