import React, { useState, useEffect, useRef } from 'react';
import { Card, CardHeader, CardBody, Input, Button, FormGroup, Label, Alert } from 'reactstrap';
import { useAuth } from '../auth/useAuth';

function ChatWidget() {
    const auth = useAuth();
    const [ws, setWs] = useState(null);

    // Separate State for histories
    const [botMessages, setBotMessages] = useState([]);
    const [adminMessages, setAdminMessages] = useState([]);

    const [inputMessage, setInputMessage] = useState('');
    const [chatMode, setChatMode] = useState('bot'); // 'bot' or 'admin'
    const [adminList, setAdminList] = useState([]);
    const [selectedAdmin, setSelectedAdmin] = useState('');
    const [isConnected, setIsConnected] = useState(false);
    const [notification, setNotification] = useState(null);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    // Scroll whenever the active view's messages change
    useEffect(() => {
        scrollToBottom();
    }, [botMessages, adminMessages, chatMode]);

    useEffect(() => {
        // Reconnect on mode switch to reset backend routing
        const username = auth.user?.sub || 'Guest';
        // NEW: Get the ID from the auth user object.
        // Ensure your LoginResponseDTO or UserDTO includes the 'id' and it is stored in the auth context.
        const userId = auth.user?.userId || auth.user?.id || 'null';

        // Append chatMode to ensure clean state on backend
        // UPDATED: Added userId to the query params
        const websocket = new WebSocket(`ws://localhost:8085/ws/chat?userType=client&username=${username}&userId=${userId}`);

        websocket.onopen = () => {
            console.log('WebSocket Connected');
            setIsConnected(true);
            // If entering admin mode, immediately ask for admin list
            if (chatMode === 'admin') {
                websocket.send(JSON.stringify({ type: 'get_admins' }));
            }
        };

        websocket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);

                if (data.type === 'admin_list') {
                    setAdminList(JSON.parse(data.message));
                } else if (data.type === 'notification') {
                    setNotification(data.message);
                    setTimeout(() => setNotification(null), 5000);
                } else if (data.type === 'chat' || data.type === 'system') {
                    // Route incoming messages to correct history
                    // If it's from Bot, or we are in bot mode and it's a system msg (like "Welcome")
                    if (data.from === 'Bot' || (data.type === 'system' && chatMode === 'bot')) {
                        setBotMessages(prev => [...prev, data]);
                    } else {
                        // Otherwise it belongs to admin chat
                        setAdminMessages(prev => [...prev, data]);
                    }
                }
            } catch (error) {
                console.error('Error parsing message:', error);
            }
        };

        websocket.onclose = () => {
            setIsConnected(false);
        };

        setWs(websocket);

        return () => {
            websocket.close();
        };
    }, [auth.user, chatMode]);

    const sendMessage = () => {
        if (!inputMessage.trim() || !ws) return;

        const message = {
            type: 'chat',
            message: inputMessage
        };

        ws.send(JSON.stringify(message));

        const myMsg = {
            type: 'chat',
            from: 'You',
            message: inputMessage,
            timestamp: new Date().toISOString()
        };

        // Add to local history based on current mode
        if (chatMode === 'bot') {
            setBotMessages(prev => [...prev, myMsg]);
        } else {
            setAdminMessages(prev => [...prev, myMsg]);
        }

        setInputMessage('');
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') sendMessage();
    };

    const connectToAdmin = () => {
        if (!selectedAdmin || !ws) return;
        ws.send(JSON.stringify({
            type: 'select_admin',
            adminId: selectedAdmin
        }));
        setAdminMessages(prev => [...prev, {
            type: 'system',
            from: 'System',
            message: 'Connecting to admin...',
            timestamp: new Date().toISOString()
        }]);
    };

    // Helper to get current messages
    const currentMessages = chatMode === 'bot' ? botMessages : adminMessages;

    return (
        <div>
            {notification && (
                <Alert color="danger" style={{ position: 'fixed', top: '20px', right: '20px', zIndex: 9999 }}>
                    <strong>⚠️ Alert:</strong> {notification}
                </Alert>
            )}

            <Card style={{ height: '600px', display: 'flex', flexDirection: 'column' }}>
                <CardHeader>
                    <strong>Customer Support Chat</strong>
                    <span style={{ float: 'right', fontSize: '12px', color: isConnected ? 'green' : 'red' }}>
                        ● {isConnected ? 'Connected' : 'Disconnected'}
                    </span>
                </CardHeader>

                <CardBody style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                    <div style={{ marginBottom: '15px' }}>
                        <Button
                            color={chatMode === 'bot' ? 'primary' : 'secondary'}
                            size="sm"
                            onClick={() => setChatMode('bot')}
                            style={{ marginRight: '10px' }}
                        >
                            💬 Chat with Bot
                        </Button>
                        <Button
                            color={chatMode === 'admin' ? 'primary' : 'secondary'}
                            size="sm"
                            onClick={() => setChatMode('admin')}
                        >
                            👤 Chat with Admin
                        </Button>
                    </div>

                    {chatMode === 'admin' && (
                        <FormGroup style={{ marginBottom: '15px' }}>
                            <div style={{ display: 'flex', gap: '10px' }}>
                                <Input
                                    type="select"
                                    value={selectedAdmin}
                                    onChange={(e) => setSelectedAdmin(e.target.value)}
                                >
                                    <option value="">-- Choose an admin --</option>
                                    {adminList.map(admin => (
                                        <option key={admin.sessionId} value={admin.sessionId}>
                                            {admin.username}
                                        </option>
                                    ))}
                                </Input>
                                <Button color="success" onClick={connectToAdmin} disabled={!selectedAdmin}>
                                    Connect
                                </Button>
                            </div>
                        </FormGroup>
                    )}

                    <div style={{
                        flex: 1, overflowY: 'auto', border: '1px solid #ddd',
                        borderRadius: '4px', padding: '10px', backgroundColor: '#f9f9f9', marginBottom: '15px'
                    }}>
                        {currentMessages.map((msg, index) => (
                            <div key={index} style={{ marginBottom: '10px', textAlign: msg.from === 'You' ? 'right' : 'left' }}>
                                <div style={{
                                    display: 'inline-block', maxWidth: '70%', padding: '8px 12px', borderRadius: '8px',
                                    backgroundColor: msg.from === 'You' ? '#007bff' : msg.type === 'system' ? '#ffc107' : '#e9ecef',
                                    color: msg.from === 'You' || msg.type === 'system' ? 'white' : 'black'
                                }}>
                                    <strong>{msg.from}:</strong> {msg.message}
                                </div>
                            </div>
                        ))}
                        <div ref={messagesEndRef} />
                    </div>

                    <div style={{ display: 'flex', gap: '10px' }}>
                        <Input
                            type="text" placeholder="Type your message..."
                            value={inputMessage} onChange={(e) => setInputMessage(e.target.value)}
                            onKeyPress={handleKeyPress} disabled={!isConnected}
                        />
                        <Button color="primary" onClick={sendMessage} disabled={!isConnected || !inputMessage.trim()}>
                            Send
                        </Button>
                    </div>
                </CardBody>
            </Card>
        </div>
    );
}

export default ChatWidget;