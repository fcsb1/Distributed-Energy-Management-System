import { HOST } from '../commons/hosts';
import performRequest from "../commons/api/rest-client";

const endpoint = {
    monitoring: '/monitoring'
};

/**
 * Get hourly energy consumption for a device on a specific date.
 *
 * @param {string} deviceId - UUID of the device
 * @param {string} date - Date in format YYYY-MM-DD
 * @param {function} callback - Callback function (result, status, error)
 */
function getHourlyConsumption(deviceId, date, callback) {
    let request = new Request(
        HOST.backend_api + endpoint.monitoring + '/device/' + deviceId + '/consumption?date=' + date,
        {
            method: 'GET',
        }
    );
    performRequest(request, callback);
}

export {
    getHourlyConsumption
};