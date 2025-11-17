import { HOST } from '../../commons/hosts';
import  performRequest  from "../../commons/api/rest-client";

const endpoint = {
    device: '/devices',
    deviceForUser: '/devices/user'
};

//Client Role
function getDevicesByUserId(userId, callback) {
    let request = new Request(HOST.backend_api + endpoint.deviceForUser + '/' + userId, {
        method: 'GET',
    });
    performRequest(request, callback);
}

// Admin hole
function getDevices(callback) {
    let request = new Request(HOST.backend_api + endpoint.device, {
        method: 'GET',
    });
    performRequest(request, callback);
}

function postDevice(device, callback) {
    let request = new Request(HOST.backend_api + endpoint.device, {
        method: 'POST',
        body: JSON.stringify(device)
    });
    performRequest(request, callback);
}

function updateDevice(deviceId, device, callback) {
     let request = new Request(HOST.backend_api + endpoint.device + '/' + deviceId, {
        method: 'PUT',
        body: JSON.stringify(device)
    });
    performRequest(request, callback);
}

function deleteDevice(deviceId, callback) {
     let request = new Request(HOST.backend_api + endpoint.device + '/' + deviceId, {
        method: 'DELETE',
    });
    performRequest(request, callback);
}

export {
    getDevicesByUserId,
    getDevices,
    postDevice,
    updateDevice,
    deleteDevice
};