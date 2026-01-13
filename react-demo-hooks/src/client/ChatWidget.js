import React, { useState, useEffect, useRef } from 'react';
import { Card, CardHeader, CardBody, Input, Button, FormGroup } from 'reactstrap';

// Now accepts props from ClientDeviceContainer
function ChatWidget({
    websocket,
    chatMode,
    setChatMode,
    botMessages,
    setBotMessages,
    adminMessages,
    setAdminMessages,
    adminList
}) {
    const [inputMessage, setInputMessage] = useState('');
    const [selectedAdmin, setSelectedAdmin] = useState('');
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    // Scroll whenever messages change
    useEffect(() => {
        scrollToBottom();
    }, [botMessages, adminMessages, chatMode]);

    // Handle mode switching logic (request admin list if needed)
    useEffect(() => {
        if (chatMode === 'admin' && websocket && websocket.readyState === WebSocket.OPEN) {
            websocket.send(JSON.stringify({ type: 'get_admins' }));
        }
    }, [chatMode, websocket]);

    const sendMessage = () => {
        if (!inputMessage.trim() || !websocket) return;

        const message = {
            type: 'chat',
            message: inputMessage
        };

        // Send via parent's socket
        websocket.send(JSON.stringify(message));

        const myMsg = {
            type: 'chat',
            from: 'You',
            message: inputMessage,
            timestamp: new Date().toISOString()
        };

        // Update parent's state
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
        if (!selectedAdmin || !websocket) return;

        websocket.send(JSON.stringify({
            type: 'select_admin',
            adminId: selectedAdmin
        }));

        // Add local system message
        setAdminMessages(prev => [...prev, {
            type: 'system',
            from: 'System',
            message: 'Connecting to admin...',
            timestamp: new Date().toISOString()
        }]);
    };

    const currentMessages = chatMode === 'bot' ? botMessages : adminMessages;
    const isConnected = websocket && websocket.readyState === WebSocket.OPEN;

    return (
        <Card style={{ height: '600px', display: 'flex', flexDirection: 'column' }}>
            <CardHeader>
                <strong>Customer Support Chat</strong>
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
    );
}

export default ChatWidget;