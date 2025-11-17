import { HOST } from '../../commons/hosts';

function performRequest(request, callback) {

    // get token from local storage
    const token = localStorage.getItem('token');

    // add Authorization header if token exists
    if (token) {
        request.headers.set('Authorization', `Bearer ${token}`);
    }

    // add default headers if not present
    if (!request.headers.get('Accept')) {
        request.headers.set('Accept', 'application/json');
    }

    // always set Content-Type for non-GET requests, overwriting any default
    if (request.method !== 'GET' && request.method !== 'DELETE') {
        request.headers.set('Content-Type', 'application/json');
    }

    fetch(request)
        .then(
            function (response) {
                if (response.ok) {
                    // Handle No Content responsess
                    if (response.status === 204) {
                        callback(null, response.status, null);
                    } else {
                        response.json().then(json => callback(json, response.status, null));
                    }
                }
                else {
                    response.json().then(err => callback(null, response.status, err));
                }
            })
        .catch(function (err) {
            //catch any other unexpected error, and set custom code for error = 1
            callback(null, 1, err)
        });
}

export default performRequest;