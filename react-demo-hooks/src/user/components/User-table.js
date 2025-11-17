import React from "react";
import ReactTable from 'react-table';
import 'react-table/react-table.css';
import { Button } from 'reactstrap';

function UserTable(props) {

    const columns = [
        {
            Header: 'Username',
            accessor: 'username',
        },
        {
            Header: 'ID',
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

export default UserTable;