import React, { useState, useEffect } from 'react';
import { Card, CardHeader, CardBody, FormGroup, Label, Input, Row, Col, Alert } from 'reactstrap';
import { Bar } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';
import { getHourlyConsumption } from '../monitoring-api';

// Register Chart.js components
ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

function ConsumptionChart({ deviceId, deviceName }) {
    const [selectedDate, setSelectedDate] = useState(getTodayDate());
    const [chartData, setChartData] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [noData, setNoData] = useState(false);

    // Get today's date in YYYY-MM-DD format
    function getTodayDate() {
        const today = new Date();
        return today.toISOString().split('T')[0];
    }

    // Fetch data when date or device changes
    useEffect(() => {
        if (deviceId && selectedDate) {
            fetchConsumptionData();
        }
    }, [deviceId, selectedDate]);

    function fetchConsumptionData() {
        setIsLoading(true);
        setError(null);
        setNoData(false);

        getHourlyConsumption(deviceId, selectedDate, (result, status, err) => {
            setIsLoading(false);

            if (status === 200 && result !== null) {
                if (result.length === 0) {
                    setNoData(true);
                    setChartData(null);
                } else {
                    processChartData(result);
                }
            } else {
                setError('Failed to load consumption data');
                setChartData(null);
            }
        });
    }

    function processChartData(data) {
        // Create array for all 24 hours (0-23)
        const hourlyValues = new Array(24).fill(0);

        // Fill in the values we have
        data.forEach(item => {
            if (item.hour >= 0 && item.hour < 24) {
                hourlyValues[item.hour] = item.totalConsumption;
            }
        });

        // Create labels for hours (00:00, 01:00, etc.)
        const labels = Array.from({ length: 24 }, (_, i) =>
            i.toString().padStart(2, '0') + ':00'
        );

        setChartData({
            labels: labels,
            datasets: [
                {
                    label: 'Energy Consumption (kWh)',
                    data: hourlyValues,
                    backgroundColor: 'rgba(54, 162, 235, 0.6)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1,
                },
            ],
        });
    }

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
            },
            title: {
                display: true,
                text: `Energy Consumption - ${deviceName || 'Device'} - ${selectedDate}`,
            },
            tooltip: {
                callbacks: {
                    label: function(context) {
                        return `${context.parsed.y.toFixed(3)} kWh`;
                    }
                }
            }
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Hour of Day'
                }
            },
            y: {
                beginAtZero: true,
                title: {
                    display: true,
                    text: 'Energy (kWh)'
                }
            }
        }
    };

    function handleDateChange(event) {
        setSelectedDate(event.target.value);
    }

    return (
        <Card className="mt-3">
            <CardHeader>
                <strong>Historical Energy Consumption</strong>
            </CardHeader>
            <CardBody>
                <Row className="mb-3">
                    <Col md={4}>
                        <FormGroup>
                            <Label for="dateSelect">Select Date:</Label>
                            <Input
                                type="date"
                                id="dateSelect"
                                value={selectedDate}
                                onChange={handleDateChange}
                                max={getTodayDate()}
                            />
                        </FormGroup>
                    </Col>
                </Row>

                {isLoading && (
                    <Alert color="info">Loading consumption data...</Alert>
                )}

                {error && (
                    <Alert color="danger">{error}</Alert>
                )}

                {noData && !isLoading && (
                    <Alert color="warning">
                        No consumption data available for {selectedDate}.

                    </Alert>
                )}

                {chartData && !isLoading && (
                    <div style={{ height: '400px' }}>
                        <Bar data={chartData} options={chartOptions} />
                    </div>
                )}
            </CardBody>
        </Card>
    );
}

export default ConsumptionChart;