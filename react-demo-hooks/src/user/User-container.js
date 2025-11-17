import React, { useState, useEffect } from 'react';
import { Button, Card, CardHeader, Col, Modal, ModalBody, ModalHeader, Row } from 'reactstrap';
import APIResponseErrorMessage from "../commons/errorhandling/api-response-error-message";
import UserForm from "./components/User-form";
import * as API_USERS from "./api/User-api";
import UserTable from "./components/User-table";

function UserContainer() {
    const [tableData, setTableData] = useState([]);
    const [isLoaded, setIsLoaded] = useState(false);
    const [error, setError] = useState({ status: 0, errorMessage: null });

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null); // Used for editing

    useEffect(() => {
        fetchUsers();
    }, []);

    function fetchUsers() {
        return API_USERS.getUsers((result, status, err) => {
            if (result !== null && status === 200) {
                setTableData(result);
                setIsLoaded(true);
            } else {
                setError({ status: status, errorMessage: err });
            }
        });
    }

    function toggleModal() {
        setIsModalOpen(!isModalOpen);
        if (isModalOpen) {
            setSelectedUser(null); // Clear selection when closing
        }
    }

    function handleAdd() {
        setSelectedUser(null); // ensure no user is selected for 'add'
        toggleModal();
    }

    function handleEdit(user) {
        setSelectedUser(user);
        toggleModal();
    }

    function handleDelete(userId) {
        if (window.confirm("Are you sure you want to delete this user?")) {
            API_USERS.deleteUser(userId, (result, status, err) => {
                 if (status === 204) { // 204 No Content
                    fetchUsers(); // Refresh table on success
                } else {
                    setError({ status: status, errorMessage: err });
                }
            });
        }
    }

    function reload() {
        setIsLoaded(false);
        toggleModal();
        fetchUsers();
    }

    return (
        <div>
            <CardHeader>
                <strong> User Management </strong>
            </CardHeader>
            <Card>
                <br />
                <Row>
                    <Col sm={{ size: '8', offset: 1 }}>
                        <Button color="primary" onClick={handleAdd}>Add User</Button>
                    </Col>
                </Row>
                <br />
                <Row>
                    <Col sm={{ size: '8', offset: 1 }}>
                        {isLoaded &&
                            <UserTable
                                tableData={tableData}
                                onEdit={handleEdit}
                                onDelete={handleDelete}
                            />
                        }
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
                    {selectedUser ? "Edit User" : "Add User"}
                </ModalHeader>
                <ModalBody>
                    <UserForm
                        reloadHandler={reload}
                        selectedUser={selectedUser}
                    />
                </ModalBody>
            </Modal>
        </div>
    );
}

export default UserContainer;