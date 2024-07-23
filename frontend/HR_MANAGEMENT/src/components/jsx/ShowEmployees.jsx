import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { EmployeesCard } from "./EmployeesCard";
import "../css/ShowEmployees.css";

export function ShowEmployees() {
  const navigate = useNavigate();
  const token = sessionStorage.getItem("jwt");
  const [employees, setEmployees] = useState([]);

  const pStyle = {
    position: "absolute",
    right: "675px",
    top: "300px",
    fontSize: "20px",
  };
  const imgStyle = {
    width: "250px",
    height: "250px",
    position: "absolute",
    right: "675px",
    top: "50px",
  };

  const container = {
    height: '750px',
    width: '1550px',
    backgroundColor: employees.lenght > 0 ? '#3f72af' : '#DBE2EF',
    position: 'absolute',
    display: 'flex',
    placeItems: 'center',
    flexDirection: 'column',
    placeContent: 'center',
    right: '30px',
    top: '10px',
    borderRadius: '10px',
    border: '1px solid black',
    boxShadow: '-2px 2px 6px #112d4e'
  }

  useEffect(() => {
    if (!token) {
      navigate("/");
    }
    const getEmployees = () => {
      fetch("http://localhost:8080/api/employee/list", {
        method: "GET",
        headers: new Headers({
          Authorization: "Bearer " + token,
          "Content-Type": "application/json",
        }),
      })
        .then((response) => {
          if (response.status == 204) {
            setEmployees([]);
          } else {
            return response.json();
          }
        })
        .then((data) => {
          setEmployees(data.data);
        });
    };

    getEmployees();
  }, [token, navigate]);

  const showEmployeesReload = (employees) => {
    return employees.length > 0 ? (
      employees.map((employee) => (
        <EmployeesCard
          key={employee.id}
          id={employee.id}
          nome={employee.name}
          address={employee.address}
          fiscalCode={employee.fiscalCode}
          hiringDate={employee.hiringDate}
        />
      ))
    ) : (
      <>
        <img src="src\assets\empty.png" alt="" style={imgStyle} />
        <p style={pStyle}>Nessun impiegato presente!</p>
      </>
    );
  };

  

  return (
    <div style={container}>
      <div id="cardContainerEmp">{showEmployeesReload(employees)}</div>
    </div>
  );
}
