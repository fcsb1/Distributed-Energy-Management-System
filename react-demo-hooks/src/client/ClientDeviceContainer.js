import React, { useState, useEffect } from 'react';
import { Card, CardHeader, Col, Row } from 'reactstrap';
import APIResponseErrorMessage from "../commons/errorhandling/api-response-error-message";
import { getDevicesByUserId } from "../device/api/device-api";
import { useAuth } from '../auth/useAuth';
import ReactTable from 'react-table';
import 'react-table/react-table.css';

function ClientDeviceContainer() {
    const [tableData, setTableData] = useState([]);
    const [isLoaded, setIsLoaded] = useState(false);
    const [error, setError] = useState({ status: 0, errorMessage: null });
    const auth = useAuth(); //get auth context

    useEffect(() => {
        if (auth.user && auth.user.userId) {
            fetchClientDevices(auth.user.userId);
        }
    }, [auth.user]); //re-run if auth.user changes

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

    const columns = [
        {
            Header: 'Name',
            accessor: 'name',
        },
        {
            Header: 'Max Consumption (kW)',
            accessor: 'maxConsumption',
        },
        {
            Header: 'ID',
            accessor: 'id',
        }
    ];

    return (
        <div>
            <CardHeader>
                <strong> My Devices </strong>
            </CardHeader>
            <Card>
                <br />
                <Row>
                    <Col sm={{ size: '8', offset: 1 }}>
                        {isLoaded &&
                            <ReactTable
                                data={tableData}
                                columns={columns}
                                defaultPageSize={10}
                                showPagination={true}
                                style={{ height: '400px' }}
                            />
                        }
                        {error.status > 0 &&
                            <APIResponseErrorMessage
                                errorStatus={error.status}
                                error={error.errorMessage}
                            />}
                    </Col>
                </Row>
            </Card>
        </div>
    );
}

export default ClientDeviceContainer;