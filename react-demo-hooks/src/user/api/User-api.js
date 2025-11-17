import { HOST } from '../../commons/hosts';
import  performRequest  from "../../commons/api/rest-client";

const endpoint = {
    user: '/users'
};

function getUsers(callback) {
    let request = new Request(HOST.backend_api + endpoint.user, {
        method: 'GET',
    });
    performRequest(request, callback);
}

function postUser(user, callback) {
    let request = new Request(HOST.backend_api + endpoint.user, {
        method: 'POST',
        body: JSON.stringify(user)
    });
    performRequest(request, callback);
}

function updateUser(userId, user, callback) {
     let request = new Request(HOST.backend_api + endpoint.user + '/' + userId, {
        method: 'PUT',
        body: JSON.stringify(user)
    });
    performRequest(request, callback);
}

function deleteUser(userId, callback) {
     let request = new Request(HOST.backend_api + endpoint.user + '/' + userId, {
        method: 'DELETE',
    });
    performRequest(request, callback);
}

export {
    getUsers,
    postUser,
    updateUser,
    deleteUser
};