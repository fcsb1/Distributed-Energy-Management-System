import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './app'
import 'bootstrap/dist/css/bootstrap.min.css';

import { AuthProvider } from './auth/AuthProvider';

ReactDOM.render(
    <AuthProvider>
        <App />
    </AuthProvider>,
    document.getElementById('root'));