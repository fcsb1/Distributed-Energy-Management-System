import React, { useState, useEffect } from 'react';
import { Card, CardHeader, Col, Row, FormGroup, Label, Input, Nav, NavItem, NavLink, TabContent, TabPane } from 'reactstrap';
import APIResponseErrorMessage from "../commons/errorhandling/api-response-error-message";
import { getDevicesByUserId } from "../device/api/device-api";
import { useAuth } from '../auth/useAuth';
import ReactTable from 'react-table';
import 'react-table/react-table.css';
import ConsumptionChart from '../monitoring/components/ConsumptionChart';
import ChatWidget from './ChatWidget';

function ClientDeviceContainer() {
    const [tableData, setTableData] = useState([]);
    const [isLoaded, setIsLoaded] = useState(false);
    const [error, setError] = useState({ status: 0, errorMessage: null });
    const auth = useAuth();
    const [selectedDevice, setSelectedDevice] = useState(null);
    const [activeTab, setActiveTab] = useState('devices'); // 'devices' or 'chat'

    useEffect(() => {
        if (auth.user && auth.user.userId) {
            fetchClientDevices(auth.user.userId);
        }
    }, [auth.user]);

    function fetchClientDevices(userId) {
        return getDevicesByUserId(userId, (result, status, err) => {
            if (result !== null && status === 200) {
                setTableData(result);
                setIsLoaded(true);
            } else {
                setError({ status: status, errorMessage: err });
            }
        });
    }

    function handleDeviceSelect(event) {
        const deviceId = event.target.value;
        const device = tableData.find(d => d.id === deviceId);
        setSelectedDevice(device || null);
    }

    const columns = [
        {
            Header: 'Name',
            accessor: 'name',
        },
        {
            Header: 'Max Consumption (kW)',
            accessor: 'maxConsumption',
        },
        {
            Header: 'ID',
            accessor: 'id',
        }
    ];

    return (
        <div>
            <CardHeader>
                <strong> My Devices & Support </strong>
            </CardHeader>
            <Card>
                <br />
                {/* Tab Navigation */}
                <Row>
                    <Col sm={{ size: '10', offset: 1 }}>
                        <Nav tabs>
                            <NavItem>
                                <NavLink
                                    active={activeTab === 'devices'}
                                    onClick={() => setActiveTab('devices')}
                                    style={{ cursor: 'pointer' }}
                                >
                                    📊 My Devices
                                </NavLink>
                            </NavItem>
                            <NavItem>
                                <NavLink
                                    active={activeTab === 'chat'}
                                    onClick={() => setActiveTab('chat')}
                                    style={{ cursor: 'pointer' }}
                                >
                                    💬 Customer Support
                                </NavLink>
                            </NavItem>
                        </Nav>
                    </Col>
                </Row>

                {/* Tab Content */}
                <Row>
                    <Col sm={{ size: '10', offset: 1 }}>
                        <TabContent activeTab={activeTab}>
                            {/* Devices Tab */}
                            <TabPane tabId="devices">
                                <div style={{ marginTop: '20px' }}>
                                    {isLoaded && tableData.length > 0 && (
                                        <>
                                            <ReactTable
                                                data={tableData}
                                                columns={columns}
                                                defaultPageSize={5}
                                                showPagination={true}
                                                style={{ height: '250px' }}
                                            />

                                            {/* Device selector for chart */}
                                            <Card className="mt-4">
                                                <CardHeader>
                                                    <strong>Energy Consumption History</strong>
                                                </CardHeader>
                                                <div className="p-3">
                                                    <Row>
                                                        <Col md={6}>
                                                            <FormGroup>
                                                                <Label for="deviceSelect">Select Device to View Chart:</Label>
                                                                <Input
                                                                    type="select"
                                                                    id="deviceSelect"
                                                                    value={selectedDevice ? selectedDevice.id : ''}
                                                                    onChange={handleDeviceSelect}
                                                                >
                                                                    <option value="">-- Select a device --</option>
                                                                    {tableData.map(device => (
                                                                        <option key={device.id} value={device.id}>
                                                                            {device.name}
                                                                        </option>
                                                                    ))}
                                                                </Input>
                                                            </FormGroup>
                                                        </Col>
                                                    </Row>

                                                    {selectedDevice && (
                                                        <ConsumptionChart
                                                            deviceId={selectedDevice.id}
                                                            deviceName={selectedDevice.name}
                                                        />
                                                    )}
                                                </div>
                                            </Card>
                                        </>
                                    )}

                                    {isLoaded && tableData.length === 0 && (
                                        <div className="alert alert-info">
                                            No devices assigned to your account yet.
                                        </div>
                                    )}

                                    {!isLoaded && <p>Loading devices...</p>}

                                    {error.status > 0 &&
                                        <APIResponseErrorMessage
                                            errorStatus={error.status}
                                            error={error.errorMessage}
                                        />
                                    }
                                </div>
                            </TabPane>

                            {/* Chat Tab */}
                            <TabPane tabId="chat">
                                <div style={{ marginTop: '20px' }}>
                                    <ChatWidget />
                                </div>
                            </TabPane>
                        </TabContent>
                    </Col>
                </Row>
            </Card>
        </div>
    );
}

export default ClientDeviceContainer;