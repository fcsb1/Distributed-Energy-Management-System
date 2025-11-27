import { HOST } from '../commons/hosts';
import performRequest  from "../commons/api/rest-client";

const endpoint = {
    login: '/api/auth/login'
};

function login(userCredentials, callback) {
    let request = new Request(HOST.backend_api + endpoint.login, {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(userCredentials)
    });
    performRequest(request, callback);
}

export {
    login
};