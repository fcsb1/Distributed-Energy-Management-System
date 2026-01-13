import React, { useState, useEffect } from 'react';
import { Card, CardHeader, Col, Row, FormGroup, Label, Input, Nav, NavItem, NavLink, TabContent, TabPane, Alert } from 'reactstrap';
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
    const [selectedDevice, setSelectedDevice] = useState(null);
    const [activeTab, setActiveTab] = useState('devices');


    const auth = useAuth();
    const [ws, setWs] = useState(null);
    const [notification, setNotification] = useState(null);
    const [botMessages, setBotMessages] = useState([]);
    const [adminMessages, setAdminMessages] = useState([]);
    const [chatMode, setChatMode] = useState('bot');
    const [adminList, setAdminList] = useState([]);

    // Fetch Devices
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

    //WebSocket Logic
    useEffect(() => {
        const username = auth.user?.sub || 'Guest';
        const userId = auth.user?.userId || auth.user?.id || 'null';

        // Establish connection

        const websocket = new WebSocket(`ws://localhost:80/ws/chat?userType=client&username=${username}&userId=${userId}`);

        websocket.onopen = () => {
            console.log('WebSocket Connected in Container');
        };

        websocket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);

                // Handle Notification (Global Alert)
                if (data.type === 'notification') {
                    setNotification(data.message);
                    setTimeout(() => setNotification(null), 5000); // Auto-hide after 5s
                }
                // Handle Admin List updates
                else if (data.type === 'admin_list') {
                    setAdminList(JSON.parse(data.message));
                }
                // Handle Chat Messages
                else if (data.type === 'chat' || data.type === 'system') {
                    // Logic to split messages between Bot and Admin history
                    if (data.from === 'Bot' || (data.type === 'system' && data.message.includes('Connected as'))) {
                        setBotMessages(prev => [...prev, data]);
                    } else {
                        // Assume everything else is admin related
                        setAdminMessages(prev => [...prev, data]);
                    }
                }
            } catch (error) {
                console.error('Error parsing message:', error);
            }
        };

        websocket.onclose = () => {
            console.log('WebSocket Disconnected');
        };

        setWs(websocket);

        return () => {
            websocket.close();
        };
    }, [auth.user]);

    // Render
    const columns = [
        { Header: 'Name', accessor: 'name' },
        { Header: 'Max Consumption (kW)', accessor: 'maxConsumption' },
        { Header: 'ID', accessor: 'id' }
    ];

    return (
        <div>
            {/* Global Notification Alert  */}
            {notification && (
                <Alert color="danger" style={{ position: 'fixed', top: '80px', right: '20px', zIndex: 9999 }}>
                    <strong>⚠️ Alert:</strong> {notification}
                </Alert>
            )}

            <CardHeader>
                <strong> My Devices & Support </strong>
                {/* Connection Status Indicator */}
                <span style={{ float: 'right', fontSize: '12px', color: ws && ws.readyState === WebSocket.OPEN ? 'green' : 'red' }}>
                    ● {ws && ws.readyState === WebSocket.OPEN ? 'Connected' : 'Disconnected'}
                </span>
            </CardHeader>
            <Card>
                <br />
                <Row>
                    <Col sm={{ size: '10', offset: 1 }}>
                        <Nav tabs>
                            <NavItem>
                                <NavLink active={activeTab === 'devices'} onClick={() => setActiveTab('devices')} style={{ cursor: 'pointer' }}>
                                    📊 My Devices
                                </NavLink>
                            </NavItem>
                            <NavItem>
                                <NavLink active={activeTab === 'chat'} onClick={() => setActiveTab('chat')} style={{ cursor: 'pointer' }}>
                                    💬 Customer Support
                                </NavLink>
                            </NavItem>
                        </Nav>
                    </Col>
                </Row>

                <Row>
                    <Col sm={{ size: '10', offset: 1 }}>
                        <TabContent activeTab={activeTab}>
                            {/* --- Devices Tab --- */}
                            <TabPane tabId="devices">
                                <div style={{ marginTop: '20px' }}>
                                    {isLoaded && tableData.length > 0 && (
                                        <>
                                            <ReactTable data={tableData} columns={columns} defaultPageSize={5} showPagination={true} style={{ height: '250px' }} />
                                            <Card className="mt-4">
                                                <CardHeader><strong>Energy Consumption History</strong></CardHeader>
                                                <div className="p-3">
                                                    <Row>
                                                        <Col md={6}>
                                                            <FormGroup>
                                                                <Label for="deviceSelect">Select Device:</Label>
                                                                <Input type="select" id="deviceSelect" value={selectedDevice ? selectedDevice.id : ''} onChange={handleDeviceSelect}>
                                                                    <option value="">-- Select a device --</option>
                                                                    {tableData.map(device => (
                                                                        <option key={device.id} value={device.id}>{device.name}</option>
                                                                    ))}
                                                                </Input>
                                                            </FormGroup>
                                                        </Col>
                                                    </Row>
                                                    {selectedDevice && <ConsumptionChart deviceId={selectedDevice.id} deviceName={selectedDevice.name} />}
                                                </div>
                                            </Card>
                                        </>
                                    )}
                                    {isLoaded && tableData.length === 0 && <div className="alert alert-info">No devices found.</div>}
                                    {!isLoaded && <p>Loading devices...</p>}
                                    {error.status > 0 && <APIResponseErrorMessage errorStatus={error.status} error={error.errorMessage} />}
                                </div>
                            </TabPane>

                            {/* --- Chat Tab --- */}
                            <TabPane tabId="chat">
                                <div style={{ marginTop: '20px' }}>
                                    {/* 4. Pass everything down to the Child Widget */}
                                    <ChatWidget
                                        websocket={ws}
                                        chatMode={chatMode}
                                        setChatMode={setChatMode}
                                        botMessages={botMessages}
                                        setBotMessages={setBotMessages}
                                        adminMessages={adminMessages}
                                        setAdminMessages={setAdminMessages}
                                        adminList={adminList}
                                    />
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