import React from "react";
import ReactTable from 'react-table';
import 'react-table/react-table.css';
import { Button } from 'reactstrap';

function DeviceTable(props) {
    //a lookup map for user IDs to usernames
    const userMap = new Map(props.userData.map(user => [user.id, user.username]));

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
            Header: 'Assigned User',
            accessor: 'userId', // This is the userId from DeviceDTO
            Cell: cellProps => (
                <span>{userMap.get(cellProps.value) || 'Unassigned'}</span>
            )
        },
        {
            Header: 'Device ID',
            accessor: 'id',
        },
        {
            Header: 'Actions',
            accessor: 'actions',
            Cell: cellProps => (
                <div>
                    <Button color="primary" size="sm"
                        onClick={() => props.onEdit(cellProps.original)}>
                        Edit
                    </Button>
                    {' '}
                    <Button color="danger" size="sm"
                        onClick={() => props.onDelete(cellProps.original.id)}>
                        Delete
                    </Button>
                </div>
            )
        }
    ];

    return (
        <ReactTable
            data={props.tableData}
            columns={columns}
            defaultPageSize={10}
            showPagination={true}
            style={{ height: '400px' }}
        />
    );
}

export default DeviceTable;