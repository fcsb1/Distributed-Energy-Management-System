import React, { useState, useEffect, useRef } from 'react';
import { Card, CardHeader, CardBody, Input, Button, Row, Col, ListGroup, ListGroupItem, Badge } from 'reactstrap';
import { useAuth } from '../auth/useAuth';

function AdminChatDashboard() {
    const auth = useAuth();
    const [ws, setWs] = useState(null);
    const [activeClients, setActiveClients] = useState([]);
    const [selectedClient, setSelectedClient] = useState(null);
    const [conversations, setConversations] = useState({}); // clientSessionId -> messages[]
    const [inputMessage, setInputMessage] = useState('');
    const [isConnected, setIsConnected] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [conversations, selectedClient]);

    useEffect(() => {
        // Connect to WebSocket as admin
        const username = auth.user?.sub || 'Admin';
        const websocket = new WebSocket(`ws://localhost:80/ws/chat?userType=admin&username=${username}`);

        websocket.onopen = () => {
            console.log('Admin WebSocket Connected');
            setIsConnected(true);
        };

        websocket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                console.log('Admin received:', data);

                if (data.type === 'client_list') {
                    const clients = JSON.parse(data.message);
                    setActiveClients(clients);
                } else if (data.type === 'chat') {
                    // Message from a client
                    const clientId = data.adminId; // The backend sets 'adminId' to the Client's Session ID for routing
                    if (clientId) {
                        setConversations(prev => ({
                            ...prev,
                            [clientId]: [...(prev[clientId] || []), data]
                        }));

                        // FIX: Dynamically update activeClients.
                        // If client isn't in the list, add them. If they are, mark as new message.
                        setActiveClients(prevClients => {
                            const clientExists = prevClients.some(c => c.sessionId === clientId);

                            if (clientExists) {
                                return prevClients.map(client =>
                                    client.sessionId === clientId
                                        ? { ...client, hasNewMessage: true }
                                        : client
                                );
                            } else {
                                // Add the new client to the list immediately using info from the message
                                return [...prevClients, {
                                    sessionId: clientId,
                                    username: data.from, // The username comes in the 'from' field
                                    hasNewMessage: true
                                }];
                            }
                        });
                    }
                } else if (data.type === 'system') {
                    console.log('System message:', data.message);
                }
            } catch (error) {
                console.error('Error parsing message:', error);
            }
        };

        websocket.onclose = () => {
            console.log('Admin WebSocket Disconnected');
            setIsConnected(false);
        };

        websocket.onerror = (error) => {
            console.error('Admin WebSocket Error:', error);
        };

        setWs(websocket);

        // Request client list
        setTimeout(() => {
            if (websocket.readyState === WebSocket.OPEN) {
                websocket.send(JSON.stringify({ type: 'get_clients' }));
            }
        }, 500);

        return () => {
            if (websocket) {
                websocket.close();
            }
        };
    }, [auth.user]);

    const selectClient = (client) => {
        setSelectedClient(client);

        // Clear new message indicator
        setActiveClients(prevClients =>
            prevClients.map(c =>
                c.sessionId === client.sessionId
                    ? { ...c, hasNewMessage: false }
                    : c
            )
        );
    };

    const sendMessage = () => {
        if (!inputMessage.trim() || !ws || !selectedClient) return;

        const message = {
            type: 'chat',
            to: selectedClient.sessionId,
            message: inputMessage
        };

        ws.send(JSON.stringify(message));

        // Add to local conversation
        setConversations(prev => ({
            ...prev,
            [selectedClient.sessionId]: [
                ...(prev[selectedClient.sessionId] || []),
                {
                    type: 'chat',
                    from: 'You',
                    message: inputMessage,
                    timestamp: new Date().toISOString()
                }
            ]
        }));

        setInputMessage('');
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    };

    const refreshClientList = () => {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: 'get_clients' }));
        }
    };

    const currentMessages = selectedClient
        ? (conversations[selectedClient.sessionId] || [])
        : [];

    return (
        <div>
            <CardHeader>
                <strong>Admin Chat Dashboard</strong>
                <span style={{
                    float: 'right',
                    fontSize: '12px',
                    color: isConnected ? 'green' : 'red'
                }}>
                    ● {isConnected ? 'Connected' : 'Disconnected'}
                </span>
            </CardHeader>
            <Card>
                <CardBody>
                    <Row style={{ height: '600px' }}>
                        {/* Client List */}
                        <Col md={4} style={{
                            borderRight: '1px solid #ddd',
                            height: '100%',
                            overflowY: 'auto'
                        }}>
                            <div style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                marginBottom: '15px'
                            }}>
                                <h5>Active Clients</h5>
                                <Button
                                    size="sm"
                                    color="primary"
                                    onClick={refreshClientList}
                                >
                                    🔄
                                </Button>
                            </div>

                            <ListGroup>
                                {activeClients.map(client => (
                                    <ListGroupItem
                                        key={client.sessionId}
                                        active={selectedClient?.sessionId === client.sessionId}
                                        onClick={() => selectClient(client)}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        <div style={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'center'
                                        }}>
                                            <span>👤 {client.username}</span>
                                            {client.hasNewMessage && (
                                                <Badge color="danger">New</Badge>
                                            )}
                                        </div>
                                        <small style={{ color: '#666' }}>
                                            {client.sessionId.substring(0, 8)}...
                                        </small>
                                    </ListGroupItem>
                                ))}
                            </ListGroup>

                            {activeClients.length === 0 && (
                                <div style={{
                                    textAlign: 'center',
                                    padding: '20px',
                                    color: '#999'
                                }}>
                                    No active clients
                                </div>
                            )}
                        </Col>

                        {/* Chat Area */}
                        <Col md={8} style={{
                            display: 'flex',
                            flexDirection: 'column',
                            height: '100%'
                        }}>
                            {selectedClient ? (
                                <>
                                    {/* Chat Header */}
                                    <div style={{
                                        padding: '10px',
                                        borderBottom: '1px solid #ddd',
                                        backgroundColor: '#f8f9fa'
                                    }}>
                                        <h6 style={{ margin: 0 }}>
                                            Chatting with: <strong>{selectedClient.username}</strong>
                                        </h6>
                                    </div>

                                    {/* Messages */}
                                    <div style={{
                                        flex: 1,
                                        overflowY: 'auto',
                                        padding: '15px',
                                        backgroundColor: '#f9f9f9'
                                    }}>
                                        {currentMessages.map((msg, index) => (
                                            <div
                                                key={index}
                                                style={{
                                                    marginBottom: '10px',
                                                    textAlign: msg.from === 'You' ? 'right' : 'left'
                                                }}
                                            >
                                                <div style={{
                                                    display: 'inline-block',
                                                    maxWidth: '70%',
                                                    padding: '8px 12px',
                                                    borderRadius: '8px',
                                                    backgroundColor: msg.from === 'You' ? '#007bff' : '#e9ecef',
                                                    color: msg.from === 'You' ? 'white' : 'black'
                                                }}>
                                                    <strong>{msg.from}:</strong> {msg.message}
                                                </div>
                                                <div style={{
                                                    fontSize: '10px',
                                                    color: '#999',
                                                    marginTop: '2px'
                                                }}>
                                                    {msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : ''}
                                                </div>
                                            </div>
                                        ))}
                                        <div ref={messagesEndRef} />
                                    </div>

                                    {/* Input Area */}
                                    <div style={{
                                        padding: '15px',
                                        borderTop: '1px solid #ddd',
                                        backgroundColor: 'white'
                                    }}>
                                        <div style={{ display: 'flex', gap: '10px' }}>
                                            <Input
                                                type="text"
                                                placeholder="Type your reply..."
                                                value={inputMessage}
                                                onChange={(e) => setInputMessage(e.target.value)}
                                                onKeyPress={handleKeyPress}
                                                disabled={!isConnected}
                                            />
                                            <Button
                                                color="primary"
                                                onClick={sendMessage}
                                                disabled={!isConnected || !inputMessage.trim()}
                                            >
                                                Send
                                            </Button>
                                        </div>
                                    </div>
                                </>
                            ) : (
                                <div style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    height: '100%',
                                    color: '#999'
                                }}>
                                    <div style={{ textAlign: 'center' }}>
                                        <h4>👈 Select a client to start chatting</h4>
                                        <p>Active clients will appear in the list on the left</p>
                                    </div>
                                </div>
                            )}
                        </Col>
                    </Row>
                </CardBody>
            </Card>
        </div>
    );
}

export default AdminChatDashboard;