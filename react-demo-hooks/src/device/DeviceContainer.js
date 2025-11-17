import React, { useState, useEffect } from 'react';
import { Button, Card, CardHeader, Col, Modal, ModalBody, ModalHeader, Row } from 'reactstrap';
import APIResponseErrorMessage from "../commons/errorhandling/api-response-error-message";
import DeviceForm from "./components/DeviceForm";
import * as API_DEVICES from "./api/device-api";
import * as API_USERS from "../user/api/User-api";
import DeviceTable from "./components/DeviceTable";

function DeviceContainer() {
    const [tableData, setTableData] = useState([]);
    const [userData, setUserData] = useState([]); // to populate dropdown
    const [isLoaded, setIsLoaded] = useState(false);
    const [error, setError] = useState({ status: 0, errorMessage: null });

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedDevice, setSelectedDevice] = useState(null);

    useEffect(() => {
        fetchData();
    }, []);

    function fetchData() {
        setIsLoaded(false);
        // Fetch devices
        API_DEVICES.getDevices((result, status, err) => {
            if (result !== null && status === 200) {
                setTableData(result);

                // after fetching devices, fetch users
                API_USERS.getUsers((userResult, userStatus, userErr) => {
                    if (userResult !== null && userStatus === 200) {
                        setUserData(userResult);
                        setIsLoaded(true); // set loaded only after all data is fetched
                    } else {
                        setError({ status: userStatus, errorMessage: userErr });
                    }
                });

            } else {
                setError({ status: status, errorMessage: err });
            }
        });
    }

    function toggleModal() {
        setIsModalOpen(!isModalOpen);
        if (isModalOpen) {
            setSelectedDevice(null);
        }
    }

    function handleAdd() {
        setSelectedDevice(null);
        toggleModal();
    }

    function handleEdit(device) {
        setSelectedDevice(device);
        toggleModal();
    }

    function handleDelete(deviceId) {
        if (window.confirm("Are you sure you want to delete this device?")) {
            API_DEVICES.deleteDevice(deviceId, (result, status, err) => {
                 if (status === 204) {
                    fetchData(); // Refresh table
                } else {
                    setError({ status: status, errorMessage: err });
                }
            });
        }
    }

    function reload() {
        toggleModal();
        fetchData(); // Re-fetch all data
    }

    return (
        <div>
            <CardHeader>
                <strong> Device Management (Admin) </strong>
            </CardHeader>
            <Card>
                <br />
                <Row>
                    <Col sm={{ size: '8', offset: 1 }}>
                        <Button color="primary" onClick={handleAdd}>Add Device</Button>
                    </Col>
                </Row>
                <br />
                <Row>
                    <Col sm={{ size: '10', offset: 1 }}>
                        {isLoaded &&
                            <DeviceTable
                                tableData={tableData}
                                userData={userData} // Pass users to table for display
                                onEdit={handleEdit}
                                onDelete={handleDelete}
                            />
                        }
                        {!isLoaded && <p>Loading data...</p>}
                        {error.status > 0 &&
                            <APIResponseErrorMessage
                                errorStatus={error.status}
                                error={error.errorMessage}
                            />}
                    </Col>
                </Row>
            </Card>

            <Modal isOpen={isModalOpen} toggle={toggleModal} size="lg">
                <ModalHeader toggle={toggleModal}>
                    {selectedDevice ? "Edit Device" : "Add Device"}
                </ModalHeader>
                <ModalBody>
                    <DeviceForm
                        reloadHandler={reload}
                        selectedDevice={selectedDevice}
                        users={userData} // Pass user list to form for dropdown
                    />
                </ModalBody>
            </Modal>
        </div>
    );
}

export default DeviceContainer;